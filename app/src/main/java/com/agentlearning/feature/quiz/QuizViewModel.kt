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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import kotlin.random.Random

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

    /** True once something has been answered and the quiz is not finished, so leaving should be confirmed. */
    val needsExitConfirm: Boolean get() = !finished && questions.isNotEmpty() && (index > 0 || submitted)
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val study: StudyRepository,
    private val settings: SettingsRepository,
    private val engine: QuizEngine,
    private val sound: SoundPlayer,
    private val clock: Clock,
) : ViewModel() {

    private val mode = StudyMode.valueOf(checkNotNull(savedStateHandle["mode"]))
    private val topicId: String? = savedStateHandle["topicId"]

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var sessionId: Long? = savedStateHandle.get<Long>(KEY_SESSION_ID)
    private var sessionClosed = false
    private var shownAt = 0L

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val savedIds = savedStateHandle.get<ArrayList<String>>(KEY_QUESTION_IDS)
                val seed = savedStateHandle.get<Long>(KEY_SEED)
                    ?: Random.nextLong().also { savedStateHandle[KEY_SEED] = it }
                val raw = if (savedIds != null) {
                    study.loadQuestionsByIds(savedIds)
                } else {
                    study.loadQuizQuestions(mode, topicId, settings.dailyGoal.first())
                }
                // A saved seed keeps the option order identical when the quiz is restored after process death.
                val questions = raw.map { q -> q.copy(options = q.options.shuffled(Random(seed xor q.id.hashCode().toLong()))) }
                if (savedIds == null) {
                    sessionId = if (questions.isEmpty()) null else study.startSession(mode)
                    savedStateHandle[KEY_SESSION_ID] = sessionId
                    savedStateHandle[KEY_QUESTION_IDS] = ArrayList(questions.map { it.id })
                }
                shownAt = clock.millis()
                _state.value = if (savedIds != null) restoredState(questions) else QuizUiState(loading = false, questions = questions)
                sessionClosed = _state.value.let { it.finished || (it.submitted && it.isLast) }
            } catch (e: Exception) {
                _state.value = QuizUiState(loading = false, error = e.message ?: "加载题目失败")
                return@launch
            }
            _state.collect { persist(it) }
        }
    }

    private fun restoredState(questions: List<Question>) = QuizUiState(
        loading = false,
        questions = questions,
        index = (savedStateHandle.get<Int>(KEY_INDEX) ?: 0).coerceIn(0, questions.lastIndex.coerceAtLeast(0)),
        selected = savedStateHandle.get<ArrayList<String>>(KEY_SELECTED)?.toSet().orEmpty(),
        submitted = savedStateHandle.get<Boolean>(KEY_SUBMITTED) ?: false,
        lastCorrect = savedStateHandle.get<Boolean>(KEY_LAST_CORRECT) ?: false,
        correctCount = savedStateHandle.get<Int>(KEY_CORRECT_COUNT) ?: 0,
        finished = savedStateHandle.get<Boolean>(KEY_FINISHED) ?: false,
    )

    private fun persist(s: QuizUiState) {
        savedStateHandle[KEY_INDEX] = s.index
        savedStateHandle[KEY_SELECTED] = ArrayList(s.selected)
        savedStateHandle[KEY_SUBMITTED] = s.submitted
        savedStateHandle[KEY_LAST_CORRECT] = s.lastCorrect
        savedStateHandle[KEY_CORRECT_COUNT] = s.correctCount
        savedStateHandle[KEY_FINISHED] = s.finished
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
        if (s.isLast) sessionClosed = true

        viewModelScope.launch {
            if (settings.soundEnabled.first()) sound.play(isCorrect)
            try {
                study.recordAnswer(question.id, sessionId, s.selected, isCorrect, seconds)
                if (s.isLast) sessionId?.let { study.finishSession(it) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "保存答题记录失败") }
            }
        }
    }

    fun next() {
        val s = _state.value
        if (!s.submitted) return
        if (s.isLast) {
            _state.value = s.copy(finished = true)
        } else {
            shownAt = clock.millis()
            _state.value = s.copy(index = s.index + 1, selected = emptySet(), submitted = false, lastCorrect = false)
        }
    }

    /** Marks an unfinished session as ended when the user leaves the quiz early. */
    fun onExit() {
        val id = sessionId ?: return
        if (sessionClosed) return
        sessionClosed = true
        // NonCancellable: viewModelScope is cancelled as soon as the screen leaves the back stack.
        viewModelScope.launch(NonCancellable) {
            runCatching { study.finishSession(id) }
        }
    }

    private companion object {
        const val KEY_QUESTION_IDS = "quiz_question_ids"
        const val KEY_SEED = "quiz_option_seed"
        const val KEY_SESSION_ID = "quiz_session_id"
        const val KEY_INDEX = "quiz_index"
        const val KEY_SELECTED = "quiz_selected"
        const val KEY_SUBMITTED = "quiz_submitted"
        const val KEY_LAST_CORRECT = "quiz_last_correct"
        const val KEY_CORRECT_COUNT = "quiz_correct_count"
        const val KEY_FINISHED = "quiz_finished"
    }
}