package com.agentlearning.data

import com.agentlearning.data.importer.CourseDto
import com.agentlearning.data.importer.ManifestDto
import com.agentlearning.data.importer.ManifestWeekDto
import com.agentlearning.data.importer.OptionDto
import com.agentlearning.data.importer.QuestionBank
import com.agentlearning.data.importer.QuestionDto
import com.agentlearning.data.importer.QuestionBankParser
import com.agentlearning.data.importer.QuestionBankValidator
import com.agentlearning.data.importer.TopicDto
import com.agentlearning.data.importer.WeekFileDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuestionBankValidatorTest {
    private val validator = QuestionBankValidator()

    private fun question(
        id: String = "q1",
        type: String = "SINGLE_CHOICE",
        correct: List<String> = listOf("A"),
        code: String? = null,
        topicId: String = "t1",
    ) = QuestionDto(
        id = id, topicId = topicId, type = type, difficulty = "BEGINNER", title = "title",
        codeSnippet = code, options = listOf(OptionDto("A", "a"), OptionDto("B", "b")),
        correctAnswer = correct, explanation = "why",
    )

    private fun bank(vararg questions: QuestionDto) = QuestionBank(
        ManifestDto(
            1, 1,
            listOf(CourseDto("c1", "Course", 1, 1)),
            listOf(ManifestWeekDto(1, "Week", "c1", "week01.json")),
        ),
        listOf(WeekFileDto(1, 1, "M", listOf(TopicDto("t1", "Topic")), questions.toList())),
    )

    @Test
    fun validBank_hasNoErrors() {
        assertTrue(validator.validate(bank(question())).isEmpty())
    }

    @Test
    fun duplicateQuestionIds_areReported() {
        val errors = validator.validate(bank(question("q1"), question("q1")))
        assertTrue(errors.any { "duplicate question id 'q1'" in it })
    }

    @Test
    fun unknownTopic_isReported() {
        val errors = validator.validate(bank(question(topicId = "missing")))
        assertTrue(errors.any { "unknown topicId" in it })
    }

    @Test
    fun correctAnswerMustBeAnOption() {
        val errors = validator.validate(bank(question(correct = listOf("Z"))))
        assertTrue(errors.any { "not an option" in it })
    }

    @Test
    fun singleChoiceNeedsExactlyOneAnswer() {
        val errors = validator.validate(bank(question(correct = listOf("A", "B"))))
        assertTrue(errors.any { "SINGLE_CHOICE" in it })
    }

    @Test
    fun codeQuestionNeedsSnippet() {
        val errors = validator.validate(bank(question(type = "DEBUGGING")))
        assertTrue(errors.any { "codeSnippet" in it })
    }

    @Test
    fun bundledQuestionBank_parsesAndValidates() {
        val dir = File("src/main/assets/questions")
        val parser = QuestionBankParser(Json { ignoreUnknownKeys = true })
        val manifest = parser.parseManifest(File(dir, "manifest.json").readText())
        val weeks = manifest.weeks.map { parser.parseWeek(File(dir, it.file).readText()) }

        assertEquals(16, manifest.weeks.size)
        val errors = validator.validate(QuestionBank(manifest, weeks))
        assertTrue(errors.joinToString("\n"), errors.isEmpty())
    }
}
