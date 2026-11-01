package com.agentlearning.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val monthNumber: Int,
    val sortOrder: Int,
)

@Entity(
    tableName = "topics",
    indices = [Index("courseId")],
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
)
data class TopicEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val weekNumber: Int,
    val weekTitle: String,
    val title: String,
    val description: String?,
    val sortOrder: Int,
    // Study notes shown in the knowledge menu; lines separated by \n.
    val notes: String? = null,
)

@Entity(
    tableName = "questions",
    indices = [Index("topicId")],
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val type: String,
    val difficulty: String,
    val title: String,
    val codeSnippet: String? = null,
    // JSON list of {id, text}
    val optionsJson: String? = null,
    // JSON list of option ids
    val correctAnswerJson: String,
    val explanation: String,
    val tagsJson: String? = null,
    val technology: String? = null,
    val apiVersion: String? = null,
    val sourceUrl: String? = null,
    val contentVersion: Int = 1,
    // Questions removed from the bank stay in the table so answer history keeps its reference.
    val isActive: Boolean = true,
)

@Serializable
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long? = null,
    val mode: String,
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
)

@Serializable
@Entity(
    tableName = "answer_records",
    indices = [Index("questionId"), Index("answeredAt"), Index("sessionId")],
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = StudySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class AnswerRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val sessionId: Long? = null,
    val userAnswerJson: String,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int,
    val answeredAt: Long,
)

/** How many times a piece of reading content was read; [contentKey] is built by ReadKeys. */
@Serializable
@Entity(tableName = "read_counts")
data class ReadCountEntity(
    @PrimaryKey val contentKey: String,
    val count: Int,
    val lastReadAt: Long,
)

@Serializable
@Entity(
    tableName = "review_records",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ReviewRecordEntity(
    @PrimaryKey val questionId: String,
    val repetitionCount: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Float = 2.5f,
    val nextReviewAt: Long,
    val lastReviewedAt: Long? = null,
    val status: String = "NEW",
)
