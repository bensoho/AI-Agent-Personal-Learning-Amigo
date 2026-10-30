package com.agentlearning.domain.repository

import com.agentlearning.domain.model.BackupPreview
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.model.StatsSummary
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.domain.model.Topic
import com.agentlearning.domain.model.WeeklyStat
import kotlinx.coroutines.flow.Flow

interface CurriculumRepository {
    fun observeOutline(): Flow<List<CourseOutline>>
    fun observeTopic(topicId: String): Flow<Topic?>
    fun observeQuestions(topicId: String): Flow<List<Question>>
}

interface StudyRepository {
    fun observeQuestions(filter: ReviewFilter): Flow<List<Question>>
    suspend fun loadQuizQuestions(mode: StudyMode, topicId: String?, dailyGoal: Int): List<Question>
    suspend fun startSession(mode: StudyMode): Long
    suspend fun finishSession(sessionId: Long)

    /** Saves the answer, updates the review schedule and session counters atomically. */
    suspend fun recordAnswer(
        questionId: String,
        sessionId: Long?,
        userAnswer: Set<String>,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
    )
}

interface StatsRepository {
    fun observeSummary(): Flow<StatsSummary>
    fun observeWrongCount(): Flow<Int>
    fun observeDailyStats(days: Int): Flow<List<DailyStat>>
    fun observeWeeklyStats(weeks: Int): Flow<List<WeeklyStat>>
}

interface SettingsRepository {
    val dailyGoal: Flow<Int>
    val bankContentVersion: Flow<Int>
    val fontScale: Flow<Float>
    val soundEnabled: Flow<Boolean>
    suspend fun setDailyGoal(value: Int)
    suspend fun setBankContentVersion(value: Int)
    suspend fun setFontScale(value: Float)
    suspend fun setSoundEnabled(value: Boolean)
}

class BackupException(message: String) : Exception(message)

/** A validated backup that is ready to be restored. */
interface ParsedBackup {
    val preview: BackupPreview
}

interface BackupRepository {
    suspend fun exportJson(): String

    /** Parses and validates a backup; throws [BackupException] when it cannot be restored. */
    suspend fun parse(json: String): ParsedBackup
    suspend fun restore(backup: ParsedBackup)
    suspend fun resetUserData()
}
