package com.agentlearning.data.importer

import kotlinx.serialization.Serializable

@Serializable
data class ManifestDto(
    val schemaVersion: Int,
    val contentVersion: Int,
    val courses: List<CourseDto>,
    val weeks: List<ManifestWeekDto>,
)

@Serializable
data class CourseDto(val id: String, val title: String, val monthNumber: Int, val sortOrder: Int)

@Serializable
data class ManifestWeekDto(
    val week: Int,
    val title: String,
    val courseId: String,
    val file: String,
    // Additional files whose topics and questions are merged into this week.
    val extraFiles: List<String> = emptyList(),
)

@Serializable
data class WeekFileDto(
    val schemaVersion: Int,
    val week: Int,
    val module: String = "",
    val topics: List<TopicDto> = emptyList(),
    val questions: List<QuestionDto> = emptyList(),
)

@Serializable
data class TopicDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val notes: List<String> = emptyList(),
)

@Serializable
data class OptionDto(val id: String, val text: String)

@Serializable
data class QuestionDto(
    val id: String,
    val topicId: String,
    val type: String,
    val difficulty: String,
    val title: String,
    val codeSnippet: String? = null,
    val options: List<OptionDto> = emptyList(),
    val correctAnswer: List<String>,
    val explanation: String,
    val tags: List<String> = emptyList(),
    val technology: String? = null,
    val apiVersion: String? = null,
    val sourceUrl: String? = null,
)

data class QuestionBank(val manifest: ManifestDto, val weeks: List<WeekFileDto>)
