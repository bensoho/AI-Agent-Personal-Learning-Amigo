package com.agentlearning.feature.quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.ProgressCalculator
import com.agentlearning.domain.model.QuizEngine
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.domain.repository.SettingsRepository
import com.agentlearning.domain.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CancellationException
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
    val saving: Boolean = false,
    val answerSaved: Boolean = false,
    val answerSeconds: Int = 0,
    val wrongQuestionIds: Set<String> = emptySet(),
    val startingReview: Boolean = false,
    val isMistakeReview: Boolean = false,
) {
    val current: Question? get() = questions.getOrNull(index)
    val isLast: Boolean get() = index >= questions.lastIndex
    val accuracy: Float? get() = ProgressCalculator.accuracy(correctCount, questions.size)
    val wrongCount: Int get() = questions.size - correctCount
    val wrongQuestions: List<Question> get() = questions.filter { it.id in wrongQuestionIds }
    val canAdvance: Boolean get() = submitted && answerSaved && !saving && error == null

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
                sessionClosed = _state.value.finished
                if (_state.value.submitted && !_state.value.finished) {
                    _state.update { it.copy(saving = true, error = null) }
                    saveAnswer(_state.value)
                }
            } catch (e: CancellationException) {
                throw e
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
        answerSaved = savedStateHandle.get<Boolean>(KEY_ANSWER_SAVED) ?: false,
        answerSeconds = savedStateHandle.get<Int>(KEY_ANSWER_SECONDS) ?: 0,
        wrongQuestionIds = savedStateHandle.get<ArrayList<String>>(KEY_WRONG_IDS)?.toSet().orEmpty(),
        isMistakeReview = savedStateHandle.get<Boolean>(KEY_MISTAKE_REVIEW) ?: false,
    )

    private fun persist(s: QuizUiState) {
        savedStateHandle[KEY_INDEX] = s.index
        savedStateHandle[KEY_SELECTED] = ArrayList(s.selected)
        savedStateHandle[KEY_SUBMITTED] = s.submitted
        savedStateHandle[KEY_LAST_CORRECT] = s.lastCorrect
        savedStateHandle[KEY_CORRECT_COUNT] = s.correctCount
        savedStateHandle[KEY_FINISHED] = s.finished
        savedStateHandle[KEY_ANSWER_SAVED] = s.answerSaved
        savedStateHandle[KEY_ANSWER_SECONDS] = s.answerSeconds
        savedStateHandle[KEY_WRONG_IDS] = ArrayList(s.wrongQuestionIds)
        savedStateHandle[KEY_MISTAKE_REVIEW] = s.isMistakeReview
    }

    fun toggleOption(optionId: String) {
        _state.update { s ->
            val question = s.current
            if (s.submitted || s.finished || question == null) return@update s
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
        if (s.submitted || s.finished || s.selected.isEmpty()) return

        val isCorrect = engine.grade(question, s.selected)
        val seconds = ((clock.millis() - shownAt) / 1000).toInt().coerceAtLeast(0)
        // Lock the UI immediately so a double tap cannot record the answer twice.
        val submitted = s.copy(
            submitted = true,
            saving = true,
            lastCorrect = isCorrect,
            correctCount = s.correctCount + if (isCorrect) 1 else 0,
            wrongQuestionIds = if (isCorrect) s.wrongQuestionIds else s.wrongQuestionIds + question.id,
            answerSeconds = seconds,
            error = null,
        )
        _state.value = submitted
        saveAnswer(submitted)
        viewModelScope.launch {
            if (settings.soundEnabled.first()) sound.play(isCorrect)
        }
    }

    fun retrySave() {
        val s = _state.value
        if (!s.submitted || s.saving || s.error == null || s.finished) return
        _state.value = s.copy(saving = true, error = null)
        saveAnswer(_state.value)
    }

    private fun saveAnswer(s: QuizUiState) {
        val question = checkNotNull(s.current)
        viewModelScope.launch {
            try {
                if (!s.answerSaved) {
                    study.recordAnswer(question.id, sessionId, s.selected, s.lastCorrect, s.answerSeconds)
                    _state.update { it.copy(answerSaved = true) }
                }
                if (s.isLast) {
                    sessionId?.let { study.finishSession(it) }
                    sessionClosed = true
                }
                _state.update { it.copy(saving = false, error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val prefix = if (_state.value.answerSaved) "答案已保存，但结束练习失败" else "保存答题记录失败"
                _state.update { it.copy(saving = false, error = "$prefix：${e.message ?: "请重试"}") }
            }
        }
    }

    fun next() {
        val s = _state.value
        if (!s.canAdvance || s.finished) return
        if (s.isLast) {
            _state.value = s.copy(finished = true)
        } else {
            shownAt = clock.millis()
            _state.value = s.copy(
                index = s.index + 1,
                selected = emptySet(),
                submitted = false,
                lastCorrect = false,
                answerSaved = false,
                answerSeconds = 0,
            )
        }
    }

    fun reviewMistakes() {
        val s = _state.value
        val mistakes = s.wrongQuestions
        if (!s.finished || s.startingReview || mistakes.isEmpty()) return
        _state.value = s.copy(startingReview = true, error = null)
        viewModelScope.launch {
            try {
                val id = study.startSession(StudyMode.REVIEW_RECENT_WRONG)
                sessionId = id
                sessionClosed = false
                savedStateHandle[KEY_SESSION_ID] = id
                savedStateHandle[KEY_QUESTION_IDS] = ArrayList(mistakes.map { it.id })
                shownAt = clock.millis()
                val fresh = QuizUiState(loading = false, questions = mistakes, isMistakeReview = true)
                persist(fresh)
                _state.value = fresh
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = s.copy(error = "开始错题复习失败：${e.message ?: "请重试"}")
            }
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
                .onFailure { Log.e("QuizViewModel", "Failed to finish study session $id", it) }
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
        const val KEY_ANSWER_SAVED = "quiz_answer_saved"
        const val KEY_ANSWER_SECONDS = "quiz_answer_seconds"
        const val KEY_WRONG_IDS = "quiz_wrong_ids"
        const val KEY_MISTAKE_REVIEW = "quiz_mistake_review"
    }
}