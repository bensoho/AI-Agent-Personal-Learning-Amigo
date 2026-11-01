package com.agentlearning.domain.model

import javax.inject.Inject

/** Grades answers by exact set match; shared by every question type. */
class QuizEngine @Inject constructor() {

    fun grade(question: Question, userAnswer: Set<String>): Boolean {
        if (userAnswer.isEmpty()) return false
        return userAnswer == question.correctAnswer
    }
}
