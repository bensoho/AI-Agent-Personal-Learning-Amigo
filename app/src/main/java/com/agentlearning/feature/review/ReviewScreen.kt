package com.agentlearning.feature.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.REVIEW_SESSION_LIMIT
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.model.ReviewItem
import com.agentlearning.domain.model.ReviewStatus
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ReviewScreen(
    onStartQuiz: (StudyMode) -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val reviewItems by viewModel.items.collectAsStateWithLifecycle()
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("错题与复习", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ReviewFilter.entries) { option ->
                val selected = option == filter
                FilterChip(
                    selected = selected,
                    onClick = {
                        expandedId = null
                        viewModel.select(option)
                    },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        Box(Modifier.weight(1f)) {
            if (reviewItems.isEmpty()) {
                EmptyHint(
                    text = emptyText(filter),
                    icon = if (filter == ReviewFilter.MASTERED) Icons.Filled.Info else Icons.Filled.CheckCircle,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(reviewItems, key = { it.question.id }) { item ->
                        ReviewCard(
                            item = item,
                            expanded = item.question.id == expandedId,
                            onToggle = {
                                expandedId = if (expandedId == item.question.id) null else item.question.id
                            },
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onStartQuiz(filter.mode) },
            enabled = reviewItems.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        ) {
            Text("开始练习（${minOf(reviewItems.size, REVIEW_SESSION_LIMIT)} 题）")
        }
    }
}

@Composable
private fun ReviewCard(item: ReviewItem, expanded: Boolean, onToggle: () -> Unit) {
    val question = item.question
    AppCard(Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.fillMaxWidth()) {
                SelectionContainer(Modifier.fillMaxWidth().padding(end = 28.dp)) {
                    Text(question.title, style = MaterialTheme.typography.bodyLarge)
                }
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "收起解析" else "展开解析",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp),
                )
            }
            Text(
                "${question.type.displayName()} · ${question.difficulty.label()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (item.status != null || item.wrongCount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    item.status?.let { StatusPill(it) }
                    if (item.wrongCount > 0) {
                        Pill(
                            "答错 ${item.wrongCount} 次",
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            if (item.status != ReviewStatus.MASTERED) {
                item.nextReviewAt?.let { next ->
                    Text(
                        nextReviewLabel(next),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                val correctTexts = question.options
                    .filter { it.id in question.correctAnswer }
                    .map { it.text }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("正确答案：${correctTexts.joinToString("；")}")
                    Text(question.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: ReviewStatus) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        ReviewStatus.NEW -> colors.surfaceVariant to colors.onSurfaceVariant
        ReviewStatus.LEARNING -> colors.tertiaryContainer to colors.onTertiaryContainer
        ReviewStatus.REVIEWING -> colors.primaryContainer to colors.onPrimaryContainer
        ReviewStatus.MASTERED -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    Pill(status.label(), container = container, content = content)
}

private fun ReviewStatus.label(): String = when (this) {
    ReviewStatus.NEW -> "新题"
    ReviewStatus.LEARNING -> "学习中"
    ReviewStatus.REVIEWING -> "复习中"
    ReviewStatus.MASTERED -> "已掌握"
}

private fun nextReviewLabel(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when {
        !date.isAfter(today) -> "今天复习"
        date == today.plusDays(1) -> "明天复习"
        date == today.plusDays(2) -> "后天复习"
        else -> "%d月%d日复习".format(date.monthValue, date.dayOfMonth)
    }
}

private fun emptyText(filter: ReviewFilter): String = when (filter) {
    ReviewFilter.DUE -> "今天没有待复习的题目"
    ReviewFilter.RECENT_WRONG -> "最近没有答错的题目"
    ReviewFilter.REPEATED_WRONG -> "没有多次答错的题目"
    ReviewFilter.MASTERED -> "还没有已掌握的题目"
}
