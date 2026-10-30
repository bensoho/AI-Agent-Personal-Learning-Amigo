package com.agentlearning.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agentlearning.domain.model.Difficulty
import com.agentlearning.domain.model.QuestionType

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colors, border = border, content = content)
    } else {
        Card(modifier = modifier, colors = colors, border = border, content = content)
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, container: Color = MaterialTheme.colorScheme.primaryContainer, content: Color = MaterialTheme.colorScheme.onPrimaryContainer) {
    Surface(modifier, color = container, shape = RoundedCornerShape(50)) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

fun Float?.asPercent(): String = if (this == null) "--" else "${(this * 100).toInt()}%"

fun Difficulty.label(): String = when (this) {
    Difficulty.BEGINNER -> "入门"
    Difficulty.INTERMEDIATE -> "进阶"
    Difficulty.ADVANCED -> "高级"
}

fun QuestionType.displayName(): String = when (this) {
    QuestionType.SINGLE_CHOICE -> "单选题"
    QuestionType.MULTIPLE_CHOICE -> "多选题"
    QuestionType.CODE_READING -> "代码阅读"
    QuestionType.CODE_COMPLETION -> "代码补全"
    QuestionType.DEBUGGING -> "找 Bug"
}
