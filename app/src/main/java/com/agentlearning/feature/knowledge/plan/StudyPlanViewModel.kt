package com.agentlearning.feature.knowledge.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {

    private val _state = MutableStateFlow<PlanLoadState<StudyPlan>>(PlanLoadState.Loading)
    val state: StateFlow<PlanLoadState<StudyPlan>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = runCatching { repository.load() }
                .fold({ PlanLoadState.Ready(it) }, { PlanLoadState.Error(it.message ?: "学习计划加载失败") })
        }
    }
}

@HiltViewModel
class StudyPlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StudyPlanRepository,
) : ViewModel() {

    private val documentId: String = checkNotNull(savedStateHandle["documentId"])

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
}
