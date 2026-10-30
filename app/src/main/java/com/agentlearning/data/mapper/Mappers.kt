package com.agentlearning.data.mapper

import com.agentlearning.data.importer.OptionDto
import com.agentlearning.data.importer.QuestionBank
import com.agentlearning.data.importer.QuestionDto
import com.agentlearning.data.local.entity.CourseEntity
import com.agentlearning.data.local.entity.QuestionEntity
import com.agentlearning.data.local.entity.TopicEntity
import com.agentlearning.domain.model.Course
import com.agentlearning.domain.model.Difficulty
import com.agentlearning.domain.model.Option
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.QuestionType
import com.agentlearning.domain.model.Topic
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class BankEntities(
    val courses: List<CourseEntity>,
    val topics: List<TopicEntity>,
    val questions: List<QuestionEntity>,
)

class QuestionMapper @Inject constructor(private val json: Json) {

    private val optionsSerializer = ListSerializer(OptionDto.serializer())
    private val stringsSerializer = ListSerializer(String.serializer())

    fun toEntities(bank: QuestionBank): BankEntities {
        val manifest = bank.manifest
        val courses = manifest.courses.map { CourseEntity(it.id, it.title, it.monthNumber, it.sortOrder) }

        val topics = mutableListOf<TopicEntity>()
        val questions = mutableListOf<QuestionEntity>()
        bank.weeks.forEach { week ->
            val meta = manifest.weeks.first { it.week == week.week }
            week.topics.forEachIndexed { index, topic ->
                topics += TopicEntity(
                    id = topic.id,
                    courseId = meta.courseId,
                    weekNumber = week.week,
                    weekTitle = meta.title,
                    title = topic.name,
                    description = topic.description,
                    sortOrder = index,
                )
            }
            week.questions.forEach { questions += toEntity(it, manifest.contentVersion) }
        }
        return BankEntities(courses, topics, questions)
    }

    private fun toEntity(q: QuestionDto, contentVersion: Int) = QuestionEntity(
        id = q.id,
        topicId = q.topicId,
        type = q.type,
        difficulty = q.difficulty,
        title = q.title,
        codeSnippet = q.codeSnippet,
        optionsJson = json.encodeToString(optionsSerializer, q.options),
        correctAnswerJson = json.encodeToString(stringsSerializer, q.correctAnswer),
        explanation = q.explanation,
        tagsJson = json.encodeToString(stringsSerializer, q.tags),
        technology = q.technology,
        apiVersion = q.apiVersion,
        sourceUrl = q.sourceUrl,
        contentVersion = contentVersion,
    )

    fun toDomain(e: QuestionEntity) = Question(
        id = e.id,
        topicId = e.topicId,
        type = QuestionType.valueOf(e.type),
        difficulty = Difficulty.valueOf(e.difficulty),
        title = e.title,
        codeSnippet = e.codeSnippet,
        options = e.optionsJson?.let { json.decodeFromString(optionsSerializer, it) }.orEmpty()
            .map { Option(it.id, it.text) },
        correctAnswer = json.decodeFromString(stringsSerializer, e.correctAnswerJson).toSet(),
        explanation = e.explanation,
        tags = e.tagsJson?.let { json.decodeFromString(stringsSerializer, it) }.orEmpty(),
        technology = e.technology,
        apiVersion = e.apiVersion,
        sourceUrl = e.sourceUrl,
    )
}

fun CourseEntity.toDomain() = Course(id, title, monthNumber)

fun TopicEntity.toDomain() = Topic(id, courseId, weekNumber, weekTitle, title, description)
