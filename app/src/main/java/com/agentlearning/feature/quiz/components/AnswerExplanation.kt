package com.agentlearning.feature.quiz.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agentlearning.domain.model.Question
import com.agentlearning.ui.components.AppCard

@Composable
fun AnswerExplanation(question: Question, isCorrect: Boolean, modifier: Modifier = Modifier) {
    AppCard(
        modifier.fillMaxWidth(),
        containerColor = if (isCorrect) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (isCorrect) "回答正确" else "回答错误",
                style = MaterialTheme.typography.titleMedium,
            )
            Text("正确答案：" + question.correctLabels().joinToString(", "))
            Text(question.explanation)
            val baseline = listOfNotNull(question.technology, question.apiVersion).joinToString(" ")
            if (baseline.isNotBlank()) {
                Text("适用版本：$baseline", style = MaterialTheme.typography.labelMedium)
            }
            question.sourceUrl?.let { Text("参考：$it", style = MaterialTheme.typography.labelMedium) }
        }
    }
}
