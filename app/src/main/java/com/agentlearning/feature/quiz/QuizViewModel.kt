package com.agentlearning.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.QuizEngine
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.domain.repository.SettingsRepository
import com.agentlearning.domain.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class QuizUiState(
    val loading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val index: Int = 0,
    val selected: Set<String> = emptySet(),
    val submitted: Boolean = false,
    val lastCorrect: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val error: String? = null,
) {
    val current: Question? get() = questions.getOrNull(index)
    val isLast: Boolean get() = index >= questions.lastIndex
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val study: StudyRepository,
    private val settings: SettingsRepository,
    private val engine: QuizEngine,
    private val clock: Clock,
) : ViewModel() {

    private val mode = StudyMode.valueOf(checkNotNull(savedStateHandle["mode"]))
    private val topicId: String? = savedStateHandle["topicId"]

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var sessionId: Long? = null
    private var shownAt = 0L

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val goal = settings.dailyGoal.first()
                val questions = study.loadQuizQuestions(mode, topicId, goal)
                sessionId = if (questions.isEmpty()) null else study.startSession(mode)
                shownAt = clock.millis()
                _state.value = QuizUiState(loading = false, questions = questions)
            } catch (e: Exception) {
                _state.value = QuizUiState(loading = false, error = e.message ?: "加载题目失败")
            }
        }
    }

    fun toggleOption(optionId: String) {
        _state.update { s ->
            val question = s.current
            if (s.submitted || question == null) return@update s
            val next = when {
                !question.isMultiSelect -> setOf(optionId)
                optionId in s.selected -> s.selected - optionId
                else -> s.selected + optionId
            }
            s.copy(selected = next)
        }
    }

    fun submit() {
        val s = _state.value
        val question = s.current ?: return
        if (s.submitted || s.selected.isEmpty()) return

        val isCorrect = engine.grade(question, s.selected)
        val seconds = ((clock.millis() - shownAt) / 1000).toInt().coerceAtLeast(0)
        // Lock the UI immediately so a double tap cannot record the answer twice.
        _state.update { it.copy(submitted = true, lastCorrect = isCorrect, correctCount = it.correctCount + if (isCorrect) 1 else 0) }

        viewModelScope.launch {
            try {
                study.recordAnswer(question.id, sessionId, s.selected, isCorrect, seconds)
                if (s.isLast) sessionId?.let { study.finishSession(it) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "保存答题记录失败") }
            }
        }
    }

    fun next() {
        _state.update { s ->
            if (!s.submitted) return@update s
            if (s.isLast) {
                s.copy(finished = true)
            } else {
                shownAt = clock.millis()
                s.copy(index = s.index + 1, selected = emptySet(), submitted = false, lastCorrect = false)
            }
        }
    }
}
