package com.agentlearning.feature.knowledge.plan

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.repository.ReadKeys
import com.agentlearning.domain.repository.ReadingRepository
import com.agentlearning.feature.knowledge.ReadTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlanLoadState<out T> {
    data object Loading : PlanLoadState<Nothing>
    data class Ready<T>(val value: T) : PlanLoadState<T>
    data class Error(val message: String) : PlanLoadState<Nothing>
}

@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    private val repository: StudyPlanRepository,
    readingRepository: ReadingRepository,
) : ViewModel() {

    val readCounts: StateFlow<Map<String, Int>> = readingRepository.observeReadCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val query = MutableStateFlow("")
    private val plan = MutableStateFlow<StudyPlan?>(null)
    private val error = MutableStateFlow<String?>(null)

    // Scanning the whole plan markdown per query is kept off the main thread.
    val state: StateFlow<PlanLoadState<StudyPlan>> =
        combine(plan, error, query) { plan, error, query ->
            when {
                plan != null -> PlanLoadState.Ready(plan.filter(query))
                error != null -> PlanLoadState.Error(error)
                else -> PlanLoadState.Loading
            }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanLoadState.Loading)

    init {
        viewModelScope.launch {
            runCatching { repository.load() }
                .onSuccess { plan.value = it }
                .onFailure { error.value = it.message ?: "学习计划加载失败" }
        }
    }

    fun setQuery(value: String) {
        query.value = value.trim()
    }
}

@HiltViewModel
class StudyPlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StudyPlanRepository,
    private val readingRepository: ReadingRepository,
) : ViewModel() {

    private val documentId: String = checkNotNull(savedStateHandle["documentId"])
    private val readTimer = ReadTimer(SystemClock::elapsedRealtime)

    private val _state = MutableStateFlow<PlanLoadState<PlanDocument>>(PlanLoadState.Loading)
    val state: StateFlow<PlanLoadState<PlanDocument>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = runCatching { repository.load().document(documentId) }.fold(
                { doc -> if (doc != null) PlanLoadState.Ready(doc) else PlanLoadState.Error("未找到该章节") },
                { PlanLoadState.Error(it.message ?: "学习计划加载失败") },
            )
        }
    }

    fun onReadingResumed() = readTimer.resume()

    fun onReadingPaused() = readTimer.pause()

    // Cleared when the detail page leaves the back stack, i.e. the user exits it.
    override fun onCleared() {
        if (readTimer.finish() && _state.value is PlanLoadState.Ready) {
            readingRepository.recordRead(ReadKeys.planDocument(documentId))
        }
    }
}
