package com.agentlearning.domain

import com.agentlearning.domain.model.Difficulty
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.QuestionType
import com.agentlearning.domain.model.QuizEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizEngineTest {
    private val engine = QuizEngine()

    private fun question(type: QuestionType, vararg correct: String) = Question(
        id = "q", topicId = "t", type = type, difficulty = Difficulty.BEGINNER, title = "t",
        codeSnippet = null, options = emptyList(), correctAnswer = correct.toSet(), explanation = "e",
        tags = emptyList(), technology = null, apiVersion = null, sourceUrl = null,
    )

    @Test
    fun singleChoice_matchesOnlyCorrectOption() {
        val q = question(QuestionType.SINGLE_CHOICE, "B")
        assertTrue(engine.grade(q, setOf("B")))
        assertFalse(engine.grade(q, setOf("A")))
        assertFalse(engine.grade(q, setOf("A", "B")))
    }

    @Test
    fun multipleChoice_requiresExactSetRegardlessOfOrder() {
        val q = question(QuestionType.MULTIPLE_CHOICE, "A", "C")
        assertTrue(engine.grade(q, linkedSetOf("C", "A")))
        assertFalse(engine.grade(q, setOf("A")))
        assertFalse(engine.grade(q, setOf("A", "B", "C")))
    }

    @Test
    fun emptyAnswer_isIncorrect() {
        assertFalse(engine.grade(question(QuestionType.SINGLE_CHOICE, "A"), emptySet()))
    }

    @Test
    fun codeQuestionWithTwoAnswers_isMultiSelect() {
        assertTrue(question(QuestionType.DEBUGGING, "A", "B").isMultiSelect)
        assertFalse(question(QuestionType.DEBUGGING, "A").isMultiSelect)
    }
}
