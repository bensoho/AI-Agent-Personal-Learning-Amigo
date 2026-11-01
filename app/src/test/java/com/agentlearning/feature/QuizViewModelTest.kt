package com.agentlearning.feature

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.Difficulty
import com.agentlearning.domain.model.Option
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.QuestionType
import com.agentlearning.domain.model.QuizEngine
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.model.ReviewMeta
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.domain.model.ThemeMode
import com.agentlearning.domain.repository.SettingsRepository
import com.agentlearning.domain.repository.StudyRepository
import com.agentlearning.feature.quiz.QuizViewModel
import com.agentlearning.feature.quiz.SoundPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    private val models = mutableListOf<QuizViewModel>()
    private val clock = Clock.fixed(Instant.parse("2026-11-01T06:00:00Z"), ZoneOffset.UTC)
    private val questions = (1..3).map { number ->
        Question(
            id = "q$number", topicId = "t", type = QuestionType.SINGLE_CHOICE, difficulty = Difficulty.BEGINNER,
            title = "Question $number", codeSnippet = null,
            options = listOf(Option("A", "Correct"), Option("B", "Wrong")),
            correctAnswer = setOf("A"), explanation = "Explanation", tags = emptyList(),
            technology = null, apiVersion = null, sourceUrl = null,
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun create(study: FakeStudy, handle: SavedStateHandle = SavedStateHandle(mapOf("mode" to "DAILY"))): QuizViewModel =
        QuizViewModel(handle, study, QuietSettings(), QuizEngine(), SoundPlayer(), clock).also { models += it }

    private fun copyHandle(handle: SavedStateHandle) =
        SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })

    @Test
    fun completion_reviewsOnlyThisRoundsMistakesInANewSession() = runTest {
        val study = FakeStudy(questions)
        val model = create(study)
        advanceUntilIdle()
        listOf("A", "B", "B").forEach { answer ->
            model.toggleOption(answer)
            model.submit()
            advanceUntilIdle()
            model.next()
        }
        assertTrue(model.state.value.finished)
        assertEquals(1, model.state.value.correctCount)
        assertEquals(2, model.state.value.wrongCount)
        assertEquals(listOf("q2", "q3"), model.state.value.wrongQuestions.map { it.id })
        assertEquals(1f / 3, model.state.value.accuracy!!, 0.0001f)

        model.reviewMistakes()
        model.reviewMistakes()
        advanceUntilIdle()
        assertEquals(listOf(StudyMode.DAILY, StudyMode.REVIEW_RECENT_WRONG), study.modes)
        assertEquals(listOf("q2", "q3"), model.state.value.questions.map { it.id })
        assertTrue(model.state.value.isMistakeReview)
        assertEquals(0, model.state.value.correctCount)
        repeat(2) {
            model.toggleOption("A")
            model.submit()
            advanceUntilIdle()
            model.next()
        }
        assertTrue(model.state.value.finished)
        assertEquals(1f, model.state.value.accuracy!!, 0f)
        assertTrue(model.state.value.wrongQuestions.isEmpty())
        assertEquals(5, study.records.size)
        model.reviewMistakes()
        model.onExit()
        advanceUntilIdle()
        assertEquals(2, study.modes.size)
        assertEquals(listOf(1L, 2L), study.finished)
    }

    @Test
    fun saving_blocksDoubleSubmitAndAdvancing() = runTest {
        val study = FakeStudy(questions.take(1))
        val gate = CompletableDeferred<Unit>()
        study.recordGate = gate
        val model = create(study)
        advanceUntilIdle()
        model.toggleOption("B")
        model.submit()
        model.submit()
        model.next()
        advanceUntilIdle()
        assertTrue(model.state.value.saving)
        assertFalse(model.state.value.finished)
        assertEquals(1, study.recordCalls)
        assertEquals(1, model.state.value.wrongQuestionIds.size)
        gate.complete(Unit)
        advanceUntilIdle()
        model.next()
        assertTrue(model.state.value.finished)
        assertEquals(1, study.records.size)
    }

    @Test
    fun writeFailure_canRetryWithoutRegrading() = runTest {
        val study = FakeStudy(questions.take(1))
        study.failRecord = true
        val model = create(study)
        advanceUntilIdle()
        model.toggleOption("A")
        model.submit()
        advanceUntilIdle()
        assertNotNull(model.state.value.error)
        model.next()
        assertFalse(model.state.value.finished)
        model.retrySave()
        advanceUntilIdle()
        model.next()
        assertTrue(model.state.value.finished)
        assertEquals(1, model.state.value.correctCount)
        assertEquals(1, study.records.size)
        assertEquals(2, study.recordCalls)
    }

    @Test
    fun sessionFinishFailure_retriesOnlyFinishNotAnswer() = runTest {
        val study = FakeStudy(questions.take(1))
        study.failFinish = true
        val model = create(study)
        advanceUntilIdle()
        model.toggleOption("B")
        model.submit()
        advanceUntilIdle()
        assertTrue(model.state.value.answerSaved)
        assertNotNull(model.state.value.error)
        model.next()
        assertFalse(model.state.value.finished)
        model.retrySave()
        advanceUntilIdle()
        model.next()
        assertTrue(model.state.value.finished)
        assertEquals(1, study.recordCalls)
        assertEquals(1, study.records.size)
        assertEquals(2, study.finishCalls)
    }

    @Test
    fun reviewStartFailure_keepsResultsAndCanRetry() = runTest {
        val study = FakeStudy(questions.take(1))
        val model = create(study)
        advanceUntilIdle()
        model.toggleOption("B")
        model.submit()
        advanceUntilIdle()
        model.next()
        study.failStart = true
        model.reviewMistakes()
        advanceUntilIdle()
        assertTrue(model.state.value.finished)
        assertFalse(model.state.value.startingReview)
        assertEquals(listOf("q1"), model.state.value.wrongQuestions.map { it.id })
        assertNotNull(model.state.value.error)
        model.reviewMistakes()
        advanceUntilIdle()
        assertFalse(model.state.value.finished)
        assertTrue(model.state.value.isMistakeReview)
        assertEquals(2, study.modes.size)
    }

    @Test
    fun restoration_preservesMistakeResultsAndReviewOptionOrder() = runTest {
        val study = FakeStudy(questions.take(2))
        val handle = SavedStateHandle(mapOf("mode" to "DAILY"))
        val original = create(study, handle)
        advanceUntilIdle()
        original.toggleOption("A")
        original.submit()
        advanceUntilIdle()
        original.next()
        original.toggleOption("B")
        original.submit()
        advanceUntilIdle()
        original.next()
        advanceUntilIdle()
        original.viewModelScope.cancel()

        val restoredHandle = copyHandle(handle)
        val restored = create(study, restoredHandle)
        advanceUntilIdle()
        assertTrue(restored.state.value.finished)
        assertEquals(1, restored.state.value.correctCount)
        assertEquals(listOf("q2"), restored.state.value.wrongQuestions.map { it.id })
        restored.reviewMistakes()
        advanceUntilIdle()
        val optionOrder = restored.state.value.current!!.options
        restored.viewModelScope.cancel()

        val review = create(study, copyHandle(restoredHandle))
        advanceUntilIdle()
        assertTrue(review.state.value.isMistakeReview)
        assertEquals(listOf("q2"), review.state.value.questions.map { it.id })
        assertEquals(optionOrder, review.state.value.current!!.options)
        assertEquals(2, study.modes.size)
    }

    @Test
    fun restoration_resumesInterruptedSaveWithoutCountingGradeAgain() = runTest {
        val study = FakeStudy(questions.take(1))
        study.recordGate = CompletableDeferred()
        val handle = SavedStateHandle(mapOf("mode" to "DAILY"))
        val original = create(study, handle)
        advanceUntilIdle()
        original.toggleOption("A")
        original.submit()
        advanceUntilIdle()
        original.viewModelScope.cancel()
        advanceUntilIdle()
        study.recordGate = null
        val restored = create(study, copyHandle(handle))
        advanceUntilIdle()
        assertTrue(restored.state.value.canAdvance)
        assertEquals(1, restored.state.value.correctCount)
        assertEquals(1, study.records.size)
        restored.next()
        assertTrue(restored.state.value.finished)
    }

    private class FakeStudy(private val questions: List<Question>) : StudyRepository {
        val modes = mutableListOf<StudyMode>()
        val finished = mutableListOf<Long>()
        val records = mutableSetOf<Pair<Long?, String>>()
        var recordCalls = 0
        var finishCalls = 0
        var failStart = false
        var failRecord = false
        var failFinish = false
        var recordGate: CompletableDeferred<Unit>? = null

        override fun observeQuestions(filter: ReviewFilter): Flow<List<Question>> = flowOf(questions)
        override fun observeReviewMeta(): Flow<List<ReviewMeta>> = flowOf(emptyList())
        override suspend fun loadQuizQuestions(mode: StudyMode, topicId: String?, dailyGoal: Int) = questions
        override suspend fun loadQuestionsByIds(ids: List<String>) = ids.mapNotNull { id -> questions.find { it.id == id } }
        override suspend fun startSession(mode: StudyMode): Long {
            if (failStart) {
                failStart = false
                error("start failed")
            }
            modes += mode
            return modes.size.toLong()
        }
        override suspend fun finishSession(sessionId: Long) {
            finishCalls++
            if (failFinish) {
                failFinish = false
                error("finish failed")
            }
            finished += sessionId
        }
        override suspend fun recordAnswer(
            questionId: String, sessionId: Long?, userAnswer: Set<String>, isCorrect: Boolean, timeSpentSeconds: Int,
        ) {
            recordCalls++
            recordGate?.await()
            if (failRecord) {
                failRecord = false
                error("write failed")
            }
            records += sessionId to questionId
        }
    }

    private class QuietSettings : SettingsRepository {
        override val dailyGoal = flowOf(10)
        override val bankContentVersion = flowOf(1)
        override val fontScale = flowOf(1f)
        override val soundEnabled = flowOf(false)
        override val themeMode = flowOf(ThemeMode.SYSTEM)
        override suspend fun setDailyGoal(value: Int) = Unit
        override suspend fun setBankContentVersion(value: Int) = Unit
        override suspend fun setFontScale(value: Float) = Unit
        override suspend fun setSoundEnabled(value: Boolean) = Unit
        override suspend fun setThemeMode(value: ThemeMode) = Unit
    }
}
