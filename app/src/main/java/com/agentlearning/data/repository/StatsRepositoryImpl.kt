package com.agentlearning.data.repository

import com.agentlearning.data.local.dao.AnswerRecordDao
import com.agentlearning.data.local.dao.QuestionDao
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.domain.model.ProgressCalculator
import com.agentlearning.domain.model.StatsSummary
import com.agentlearning.domain.model.WeeklyStat
import com.agentlearning.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val answerDao: AnswerRecordDao,
    private val questionDao: QuestionDao,
    private val clock: Clock,
) : StatsRepository {

    private val zone: ZoneId get() = clock.zone

    override fun observeSummary(): Flow<StatsSummary> {
        val today = LocalDate.now(clock)
        val totals = combine(answerDao.observeTotal(), answerDao.observeCorrect()) { total, correct ->
            total to correct
        }
        return combine(
            totals,
            answerDao.observeCountSince(startOfDayMillis(today)),
            answerDao.observeStudyDays(),
            questionDao.observeDueCount(clock.millis()),
            questionDao.observeCurrentWeek(),
        ) { (total, correct), todayCount, days, due, week ->
            StatsSummary(
                totalAnswers = total,
                correctAnswers = correct,
                todayAnswers = todayCount,
                streakDays = ProgressCalculator.streakDays(days.map { LocalDate.parse(it) }.toSet(), today),
                dueCount = due,
                currentWeek = week,
            )
        }
    }

    override fun observeWrongCount(): Flow<Int> = questionDao.observeRecentWrongCount()

    override fun observeDailyStats(days: Int): Flow<List<DailyStat>> {
        val today = LocalDate.now(clock)
        val start = today.minusDays((days - 1).toLong())
        return answerDao.observeDailyStats(startOfDayMillis(start)).map { rows ->
            val byDay = rows.associateBy { LocalDate.parse(it.day) }
            (0 until days).map { offset ->
                val date = start.plusDays(offset.toLong())
                val row = byDay[date]
                DailyStat(date, row?.total ?: 0, row?.correct ?: 0)
            }
        }
    }

    override fun observeWeeklyStats(weeks: Int): Flow<List<WeeklyStat>> =
        answerDao.observeWeeklyStats(weeks).map { rows ->
            rows.reversed().map { WeeklyStat(it.week, it.total, it.correct) }
        }

    private fun startOfDayMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()
}
