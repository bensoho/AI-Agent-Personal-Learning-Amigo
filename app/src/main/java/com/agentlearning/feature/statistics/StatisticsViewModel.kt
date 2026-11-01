package com.agentlearning.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.domain.model.StatsSummary
import com.agentlearning.domain.model.WeeklyStat
import com.agentlearning.domain.repository.CurriculumRepository
import com.agentlearning.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class StatisticsUiState(
    val summary: StatsSummary? = null,
    val wrongCount: Int = 0,
    val daily: List<DailyStat> = emptyList(),
    val weekly: List<WeeklyStat> = emptyList(),
    val courses: List<CourseOutline> = emptyList(),
    val today: LocalDate = LocalDate.now(),
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    stats: StatsRepository,
    curriculum: CurriculumRepository,
    clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        stats.observeSummary(),
        stats.observeWrongCount(),
        stats.observeDailyStats(DAYS),
        stats.observeWeeklyStats(WEEKS),
        curriculum.observeOutline(),
    ) { summary, wrong, daily, weekly, courses ->
        StatisticsUiState(summary, wrong, daily, weekly, courses, LocalDate.now(clock))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    private companion object {
        const val DAYS = 7
        const val WEEKS = 8
    }
}
