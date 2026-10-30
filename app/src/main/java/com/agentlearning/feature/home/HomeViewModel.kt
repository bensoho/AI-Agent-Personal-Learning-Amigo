package com.agentlearning.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.StatsSummary
import com.agentlearning.domain.repository.SettingsRepository
import com.agentlearning.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val dailyGoal: Int = 10,
    val summary: StatsSummary? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    settings: SettingsRepository,
    stats: StatsRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(settings.dailyGoal, stats.observeSummary()) { goal, summary ->
            HomeUiState(dailyGoal = goal, summary = summary)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
