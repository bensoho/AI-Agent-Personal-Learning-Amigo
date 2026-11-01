package com.agentlearning.feature.quiz.components

import com.agentlearning.domain.model.Question

// Options are shuffled per quiz, so labels follow the displayed position rather than the stored option id.
fun optionLabel(index: Int): Char = 'A' + index

fun Question.correctLabels(): List<String> =
    options.mapIndexedNotNull { index, option ->
        if (option.id in correctAnswer) optionLabel(index).toString() else null
    }
