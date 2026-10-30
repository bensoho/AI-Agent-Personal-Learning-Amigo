package com.agentlearning.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    study: StudyRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(ReviewFilter.DUE)
    val filter: StateFlow<ReviewFilter> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val questions: StateFlow<List<Question>> = _filter
        .flatMapLatest { study.observeQuestions(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun select(filter: ReviewFilter) {
        _filter.value = filter
    }
}
