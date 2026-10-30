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
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.model.ReviewScheduler
import com.agentlearning.domain.model.ReviewState
import com.agentlearning.domain.model.ReviewStatus
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

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

    override fun observeQuestions(filter: ReviewFilter): Flow<List<Question>> =
        observeEntities(filter, NO_LIMIT).map { list -> list.map(mapper::toDomain) }

    override suspend fun loadQuizQuestions(
        mode: StudyMode,
        topicId: String?,
        dailyGoal: Int,
    ): List<Question> {
        val entities: List<QuestionEntity> = when (mode) {
            StudyMode.TOPIC -> questionDao.observeByTopic(requireNotNull(topicId) { "topicId required" }).first()
            StudyMode.DAILY -> {
                val due = questionDao.observeDue(clock.millis(), dailyGoal).first()
                val fresh = questionDao.observeUnanswered((dailyGoal - due.size).coerceAtLeast(0)).first()
                due + fresh
            }
            StudyMode.REVIEW_DUE -> observeEntities(ReviewFilter.DUE, REVIEW_SESSION_LIMIT).first()
            StudyMode.REVIEW_RECENT_WRONG -> observeEntities(ReviewFilter.RECENT_WRONG, REVIEW_SESSION_LIMIT).first()
            StudyMode.REVIEW_REPEATED_WRONG -> observeEntities(ReviewFilter.REPEATED_WRONG, REVIEW_SESSION_LIMIT).first()
            StudyMode.REVIEW_MASTERED -> observeEntities(ReviewFilter.MASTERED, REVIEW_SESSION_LIMIT).first()
        }
        return entities.map(mapper::toDomain)
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
        const val REVIEW_SESSION_LIMIT = 20
    }
}
