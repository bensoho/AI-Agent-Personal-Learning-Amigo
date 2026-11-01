package com.agentlearning.feature.curriculum

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.Topic
import com.agentlearning.domain.repository.CurriculumRepository
import com.agentlearning.domain.repository.ReadKeys
import com.agentlearning.domain.repository.ReadingRepository
import com.agentlearning.feature.knowledge.ReadTimer
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
    private val readingRepository: ReadingRepository,
) : ViewModel() {
    private val topicId: String = checkNotNull(savedStateHandle["topicId"])
    private val readTimer = ReadTimer(SystemClock::elapsedRealtime)

    val uiState: StateFlow<TopicDetailUiState> = combine(
        repository.observeTopic(topicId),
        repository.observeQuestions(topicId),
    ) { topic, questions -> TopicDetailUiState(topic, questions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TopicDetailUiState())

    fun onReadingResumed() = readTimer.resume()

    fun onReadingPaused() = readTimer.pause()

    // Cleared when the detail page leaves the back stack, i.e. the user exits it.
    override fun onCleared() {
        if (readTimer.finish()) readingRepository.recordRead(ReadKeys.topic(topicId))
    }
}
