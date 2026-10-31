package com.agentlearning.domain.model

enum class QuestionType { SINGLE_CHOICE, MULTIPLE_CHOICE, CODE_READING, CODE_COMPLETION, DEBUGGING }

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

enum class ReviewStatus { NEW, LEARNING, REVIEWING, MASTERED }

enum class LearningStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

data class Option(val id: String, val text: String)

data class Question(
    val id: String,
    val topicId: String,
    val type: QuestionType,
    val difficulty: Difficulty,
    val title: String,
    val codeSnippet: String?,
    val options: List<Option>,
    val correctAnswer: Set<String>,
    val explanation: String,
    val tags: List<String>,
    val technology: String?,
    val apiVersion: String?,
    val sourceUrl: String?,
) {
    val isMultiSelect: Boolean
        get() = type == QuestionType.MULTIPLE_CHOICE || correctAnswer.size > 1
}

data class Course(val id: String, val title: String, val monthNumber: Int)

data class Topic(
    val id: String,
    val courseId: String,
    val weekNumber: Int,
    val weekTitle: String,
    val title: String,
    val description: String?,
    val notes: String? = null,
)

data class TopicProgress(
    val topic: Topic,
    val total: Int,
    val answered: Int,
    val mastered: Int,
    val reviewing: Int,
    val learning: Int,
) {
    val status: LearningStatus get() = ProgressCalculator.status(total, answered)
}

data class WeekOutline(val weekNumber: Int, val title: String, val topics: List<TopicProgress>) {
    val total: Int get() = topics.sumOf { it.total }
    val answered: Int get() = topics.sumOf { it.answered }
    val status: LearningStatus get() = ProgressCalculator.status(total, answered)
}

data class CourseOutline(val course: Course, val weeks: List<WeekOutline>) {
    val total: Int get() = weeks.sumOf { it.total }
    val answered: Int get() = weeks.sumOf { it.answered }
    val mastery: Float
        get() = ProgressCalculator.masteryScore(
            total = total,
            mastered = weeks.sumOf { w -> w.topics.sumOf { it.mastered } },
            reviewing = weeks.sumOf { w -> w.topics.sumOf { it.reviewing } },
            learning = weeks.sumOf { w -> w.topics.sumOf { it.learning } },
        )
}

data class ReviewState(
    val repetitionCount: Int,
    val intervalDays: Int,
    val easeFactor: Float,
    val nextReviewAt: Long,
    val lastReviewedAt: Long?,
    val status: ReviewStatus,
)

enum class StudyMode { TOPIC, DAILY, REVIEW_DUE, REVIEW_RECENT_WRONG, REVIEW_REPEATED_WRONG, REVIEW_MASTERED }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Maximum number of questions in one review-mode quiz. */
const val REVIEW_SESSION_LIMIT = 20

enum class ReviewFilter(val label: String, val mode: StudyMode) {
    DUE("今日待复习", StudyMode.REVIEW_DUE),
    RECENT_WRONG("最近答错", StudyMode.REVIEW_RECENT_WRONG),
    REPEATED_WRONG("多次答错", StudyMode.REVIEW_REPEATED_WRONG),
    MASTERED("已掌握", StudyMode.REVIEW_MASTERED),
}

data class StatsSummary(
    val totalAnswers: Int,
    val correctAnswers: Int,
    val todayAnswers: Int,
    val streakDays: Int,
    val dueCount: Int,
    val currentWeek: Int?,
) {
    val accuracy: Float? get() = ProgressCalculator.accuracy(correctAnswers, totalAnswers)
}

data class DailyStat(val date: java.time.LocalDate, val total: Int, val correct: Int)

data class WeeklyStat(val label: String, val total: Int, val correct: Int) {
    val accuracy: Float? get() = ProgressCalculator.accuracy(correct, total)
}

data class BackupPreview(
    val exportedAt: Long,
    val bankContentVersion: Int,
    val sessionCount: Int,
    val answerCount: Int,
    val reviewCount: Int,
)
