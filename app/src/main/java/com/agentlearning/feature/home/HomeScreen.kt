package com.agentlearning.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.ui.components.DailyBars
import com.agentlearning.ui.components.SectionTitle
import com.agentlearning.ui.components.StatCard
import com.agentlearning.ui.components.asPercent

@Composable
fun HomeScreen(
    onStartQuiz: (StudyMode) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Agent Learning", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "LangChain · LangGraph · LangSmith",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
                    }
                }
            }

            item {
                val today = summary?.todayAnswers ?: 0
                val progress = (today.toFloat() / state.dailyGoal).coerceIn(0f, 1f)
                val gradient = Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(gradient)
                        .padding(20.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("今日学习任务", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                if (progress >= 1f) "今日目标已达成" else "还差 ${(state.dailyGoal - today).coerceAtLeast(0)} 题",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(72.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f),
                                strokeWidth = 7.dp,
                            )
                            Text("$today/${state.dailyGoal}", style = MaterialTheme.typography.titleSmall, color = Color.White)
                        }
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
                    SectionTitle("最近 7 天完成题数")
                    DailyBars(state.daily)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = { onStartQuiz(StudyMode.DAILY) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("开始今日任务")
            }
            OutlinedButton(
                onClick = { onStartQuiz(StudyMode.REVIEW_DUE) },
                enabled = (summary?.dueCount ?: 0) > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("开始今日复习")
            }
        }
    }
}
