package com.agentlearning.feature.curriculum

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.Topic
import com.agentlearning.domain.repository.CurriculumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CurriculumViewModel @Inject constructor(
    repository: CurriculumRepository,
) : ViewModel() {
    val outline: StateFlow<List<CourseOutline>> = repository.observeOutline()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

data class TopicDetailUiState(val topic: Topic? = null, val questions: List<Question> = emptyList())

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CurriculumRepository,
) : ViewModel() {
    private val topicId: String = checkNotNull(savedStateHandle["topicId"])

    val uiState: StateFlow<TopicDetailUiState> = combine(
        repository.observeTopic(topicId),
        repository.observeQuestions(topicId),
    ) { topic, questions -> TopicDetailUiState(topic, questions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TopicDetailUiState())
}
