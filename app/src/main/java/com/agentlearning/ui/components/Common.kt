package com.agentlearning.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.domain.model.Difficulty
import com.agentlearning.domain.model.QuestionType
import java.time.LocalDate

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
fun EmptyHint(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actionText != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionText) }
            }
        }
    }
}

/** Progress bar shared by every screen: neutral track, no mint gap, no stop dot. */
@Composable
fun AppLinearProgress(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}

@Composable
fun DailyBars(
    daily: List<DailyStat>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    barMaxHeight: Dp = 90.dp,
) {
    if (daily.isEmpty() || daily.all { it.total == 0 }) {
        Box(modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
            Text(
                "最近 7 天还没有答题记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val max = (daily.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    val today = LocalDate.now()
    val colors = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        daily.forEach { day ->
            val isToday = day.date == today
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Text("${day.total}", style = MaterialTheme.typography.labelSmall)
                Box(
                    Modifier
                        .width(24.dp)
                        .height((barMaxHeight * (day.total.toFloat() / max)).coerceAtLeast(2.dp))
                        .background(
                            color = if (isToday) colors.primary else colors.primary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        )
                )
                Text(
                    "${day.date.monthValue}/${day.date.dayOfMonth}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) colors.primary else colors.onSurfaceVariant,
                )
            }
        }
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
