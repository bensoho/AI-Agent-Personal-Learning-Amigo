package com.agentlearning.data.repository

import androidx.room.withTransaction
import com.agentlearning.data.local.AppDatabase
import com.agentlearning.data.local.dao.AnswerRecordDao
import com.agentlearning.data.local.dao.QuestionDao
import com.agentlearning.data.local.dao.ReviewRecordDao
import com.agentlearning.data.local.dao.StudySessionDao
import com.agentlearning.data.local.entity.AnswerRecordEntity
import com.agentlearning.data.local.entity.QuestionEntity
import com.agentlearning.data.local.entity.ReviewRecordEntity
import com.agentlearning.data.local.entity.StudySessionEntity
import com.agentlearning.data.mapper.QuestionMapper
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.REVIEW_SESSION_LIMIT
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.model.ReviewScheduler
import com.agentlearning.domain.model.ReviewState
import com.agentlearning.domain.model.ReviewStatus
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.domain.repository.StudyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class StudyRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerRecordDao,
    private val reviewDao: ReviewRecordDao,
    private val sessionDao: StudySessionDao,
    private val mapper: QuestionMapper,
    private val scheduler: ReviewScheduler,
    private val json: Json,
    private val clock: Clock,
) : StudyRepository {

    // Re-subscribes at midnight so the due list follows the date; JSON decoding stays off the main thread.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeQuestions(filter: ReviewFilter): Flow<List<Question>> =
        clock.dayChanges()
            .flatMapLatest { observeEntities(filter, NO_LIMIT) }
            .map { list -> list.map(mapper::toDomain) }
            .flowOn(Dispatchers.Default)

    override suspend fun loadQuizQuestions(
        mode: StudyMode,
        topicId: String?,
        dailyGoal: Int,
    ): List<Question> {
        val entities: List<QuestionEntity> = when (mode) {
            StudyMode.TOPIC -> questionDao.observeByTopic(requireNotNull(topicId) { "topicId required" }).first()
            StudyMode.DAILY -> loadDailyEntities(dailyGoal)
            StudyMode.REVIEW_DUE -> observeEntities(ReviewFilter.DUE, REVIEW_SESSION_LIMIT).first()
            StudyMode.REVIEW_RECENT_WRONG -> observeEntities(ReviewFilter.RECENT_WRONG, REVIEW_SESSION_LIMIT).first()
            StudyMode.REVIEW_REPEATED_WRONG -> observeEntities(ReviewFilter.REPEATED_WRONG, REVIEW_SESSION_LIMIT).first()
            StudyMode.REVIEW_MASTERED -> observeEntities(ReviewFilter.MASTERED, REVIEW_SESSION_LIMIT).first()
        }
        return withContext(Dispatchers.Default) { entities.map(mapper::toDomain) }
    }

    override suspend fun loadQuestionsByIds(ids: List<String>): List<Question> {
        if (ids.isEmpty()) return emptyList()
        val byId = questionDao.getByIds(ids).associateBy { it.id }
        return withContext(Dispatchers.Default) { ids.mapNotNull { byId[it] }.map(mapper::toDomain) }
    }

    /**
     * Today's task is the part of the daily goal not answered yet; once the goal is met a full set
     * is offered as extra practice. Part of the set is reserved for new questions so a large review
     * backlog cannot stall progress through the curriculum.
     */
    private suspend fun loadDailyEntities(dailyGoal: Int): List<QuestionEntity> {
        val now = clock.millis()
        val startOfToday = LocalDate.now(clock).atStartOfDay(clock.zone).toInstant().toEpochMilli()
        val answeredToday = answerDao.observeCountSince(startOfToday).first()
        val size = (dailyGoal - answeredToday).takeIf { it > 0 } ?: dailyGoal

        val due = questionDao.observeDue(now, size).first()
        val fresh = questionDao.observeUnanswered(size).first()
        val newQuota = minOf(fresh.size, ceil(size * NEW_QUESTION_RATIO).toInt())
        val dueTake = minOf(due.size, size - newQuota)
        val newTake = minOf(fresh.size, size - dueTake)
        return due.take(dueTake) + fresh.take(newTake)
    }

    override suspend fun startSession(mode: StudyMode): Long =
        sessionDao.insert(StudySessionEntity(startedAt = clock.millis(), mode = mode.name))

    override suspend fun finishSession(sessionId: Long) {
        sessionDao.finish(sessionId, clock.millis())
    }

    override suspend fun recordAnswer(
        questionId: String,
        sessionId: Long?,
        userAnswer: Set<String>,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
    ) {
        val now = clock.millis()
        val answerJson = json.encodeToString(ListSerializer(String.serializer()), userAnswer.sorted())
        database.withTransaction {
            answerDao.insert(
                AnswerRecordEntity(
                    questionId = questionId,
                    sessionId = sessionId,
                    userAnswerJson = answerJson,
                    isCorrect = isCorrect,
                    timeSpentSeconds = timeSpentSeconds,
                    answeredAt = now,
                )
            )
            val next = scheduler.schedule(reviewDao.get(questionId)?.toState(), isCorrect, now)
            reviewDao.upsert(next.toEntity(questionId))
            if (sessionId != null) sessionDao.addAnswer(sessionId, if (isCorrect) 1 else 0)
        }
    }

    private fun observeEntities(filter: ReviewFilter, limit: Int): Flow<List<QuestionEntity>> = when (filter) {
        ReviewFilter.DUE -> questionDao.observeDue(clock.millis(), limit)
        ReviewFilter.RECENT_WRONG -> questionDao.observeRecentWrong(limit)
        ReviewFilter.REPEATED_WRONG -> questionDao.observeRepeatedWrong(limit)
        ReviewFilter.MASTERED -> questionDao.observeMastered(limit)
    }

    private fun ReviewRecordEntity.toState() = ReviewState(
        repetitionCount = repetitionCount,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        nextReviewAt = nextReviewAt,
        lastReviewedAt = lastReviewedAt,
        status = ReviewStatus.valueOf(status),
    )

    private fun ReviewState.toEntity(questionId: String) = ReviewRecordEntity(
        questionId = questionId,
        repetitionCount = repetitionCount,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        nextReviewAt = nextReviewAt,
        lastReviewedAt = lastReviewedAt,
        status = status.name,
    )

    private companion object {
        const val NO_LIMIT = -1
        const val NEW_QUESTION_RATIO = 0.3
    }
}
