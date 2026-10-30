package com.agentlearning.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.ui.components.StatCard
import com.agentlearning.ui.components.asPercent

@Composable
fun HomeScreen(
    onStartQuiz: (StudyMode) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Agent Learning", style = MaterialTheme.typography.headlineMedium) }

        item {
            val today = summary?.todayAnswers ?: 0
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("今日学习任务", style = MaterialTheme.typography.titleMedium)
                    Text("已完成 $today / ${state.dailyGoal} 题")
                    LinearProgressIndicator(
                        progress = { (today.toFloat() / state.dailyGoal).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("待复习题目", "${summary?.dueCount ?: 0}", Modifier.weight(1f))
                StatCard(
                    "当前学习周次",
                    summary?.currentWeek?.let { "Week %02d".format(it) } ?: "已完成",
                    Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("累计正确率", summary?.accuracy.asPercent(), Modifier.weight(1f))
                StatCard("连续学习", "${summary?.streakDays ?: 0} 天", Modifier.weight(1f))
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStartQuiz(StudyMode.DAILY) }, modifier = Modifier.fillMaxWidth()) {
                    Text("开始今日任务")
                }
                OutlinedButton(
                    onClick = { onStartQuiz(StudyMode.REVIEW_DUE) },
                    enabled = (summary?.dueCount ?: 0) > 0,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("开始今日复习")
                }
            }
        }
    }
}
