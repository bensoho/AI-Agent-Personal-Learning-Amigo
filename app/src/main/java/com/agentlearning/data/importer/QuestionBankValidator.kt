package com.agentlearning.data.importer

import com.agentlearning.domain.model.Difficulty
import com.agentlearning.domain.model.QuestionType
import javax.inject.Inject

class QuestionBankException(val errors: List<String>) :
    Exception("Invalid question bank:\n" + errors.joinToString("\n") { "- $it" })

class QuestionBankValidator @Inject constructor() {

    fun validate(bank: QuestionBank): List<String> {
        val errors = mutableListOf<String>()
        val manifest = bank.manifest

        if (manifest.schemaVersion != SUPPORTED_SCHEMA) {
            errors += "manifest: unsupported schemaVersion ${manifest.schemaVersion}"
        }
        if (manifest.contentVersion < 1) errors += "manifest: contentVersion must be >= 1"

        val courseIds = manifest.courses.map { it.id }
        courseIds.duplicates().forEach { errors += "manifest: duplicate course id '$it'" }

        manifest.weeks.map { it.week }.duplicates().forEach { errors += "manifest: duplicate week $it" }
        manifest.weeks.filter { it.courseId !in courseIds }.forEach {
            errors += "manifest: week ${it.week} references unknown course '${it.courseId}'"
        }

        // Week files are loaded in manifest order, so they must line up one to one.
        if (manifest.weeks.size != bank.weeks.size) {
            errors += "manifest lists ${manifest.weeks.size} weeks but ${bank.weeks.size} files were loaded"
        }
        manifest.weeks.zip(bank.weeks).filter { (entry, file) -> entry.week != file.week }.forEach { (entry, file) ->
            errors += "manifest: file '${entry.file}' declares week ${file.week}, expected ${entry.week}"
        }

        val allTopics = bank.weeks.flatMap { it.topics }
        allTopics.map { it.id }.duplicates().forEach { errors += "duplicate topic id '$it'" }
        val topicIds = allTopics.map { it.id }.toSet()

        bank.weeks.forEach { week ->
            if (week.schemaVersion != SUPPORTED_SCHEMA) {
                errors += "week ${week.week}: unsupported schemaVersion ${week.schemaVersion}"
            }
        }

        val allQuestions = bank.weeks.flatMap { it.questions }
        allQuestions.map { it.id }.duplicates().forEach { errors += "duplicate question id '$it'" }

        bank.weeks.forEach { week ->
            val weekTopicIds = week.topics.map { it.id }.toSet()
            week.questions.forEach { question ->
                if (question.topicId !in topicIds) {
                    errors += "${question.id}: unknown topicId '${question.topicId}'"
                } else if (question.topicId !in weekTopicIds) {
                    errors += "${question.id}: topic '${question.topicId}' belongs to a different week file"
                }
                validateQuestion(question, errors)
            }
        }
        return errors
    }

    private fun validateQuestion(q: QuestionDto, errors: MutableList<String>) {
        fun err(message: String) {
            errors += "${q.id}: $message"
        }

        if (q.id.isBlank()) errors += "question with blank id"
        if (q.title.isBlank()) err("blank title")
        if (q.explanation.isBlank()) err("blank explanation")
        if (Difficulty.entries.none { it.name == q.difficulty }) err("unknown difficulty '${q.difficulty}'")

        val type = QuestionType.entries.firstOrNull { it.name == q.type }
        if (type == null) {
            err("unknown type '${q.type}'")
            return
        }

        val optionIds = q.options.map { it.id }
        if (optionIds.size < 2) err("needs at least 2 options")
        optionIds.duplicates().forEach { err("duplicate option id '$it'") }

        if (q.correctAnswer.isEmpty()) err("correctAnswer is empty")
        q.correctAnswer.duplicates().forEach { err("duplicate correct answer '$it'") }
        q.correctAnswer.filter { it !in optionIds }.forEach { err("correct answer '$it' is not an option") }

        when (type) {
            QuestionType.SINGLE_CHOICE ->
                if (q.correctAnswer.size != 1) err("SINGLE_CHOICE needs exactly one correct answer")
            QuestionType.MULTIPLE_CHOICE ->
                if (q.correctAnswer.size < 2) err("MULTIPLE_CHOICE needs at least two correct answers")
            QuestionType.CODE_READING, QuestionType.CODE_COMPLETION, QuestionType.DEBUGGING ->
                if (q.codeSnippet.isNullOrBlank()) err("$type needs a codeSnippet")
        }
    }

    private fun <T> List<T>.duplicates(): Set<T> =
        groupingBy { it }.eachCount().filterValues { it > 1 }.keys

    companion object {
        const val SUPPORTED_SCHEMA = 1
    }
}
