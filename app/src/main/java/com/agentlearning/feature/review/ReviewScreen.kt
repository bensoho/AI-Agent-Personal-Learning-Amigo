package com.agentlearning.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.REVIEW_SESSION_LIMIT
import com.agentlearning.domain.model.ReviewFilter
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label

@Composable
fun ReviewScreen(
    onStartQuiz: (StudyMode) -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().padding(top = 16.dp),
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
                    onClick = { viewModel.select(option) },
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
            if (questions.isEmpty()) {
                EmptyHint("当前筛选下没有题目")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(questions, key = { it.id }) { question ->
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                SelectionContainer { Text(question.title, style = MaterialTheme.typography.bodyLarge) }
                                Text(
                                    "${question.type.displayName()} · ${question.difficulty.label()}",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onStartQuiz(filter.mode) },
            enabled = questions.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        ) {
            Text("开始练习（${minOf(questions.size, REVIEW_SESSION_LIMIT)} 题）")
        }
    }
}
