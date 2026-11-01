package com.agentlearning.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.agentlearning.ui.theme.LocalDarkTheme

@Composable
fun HomeScreen(
    onStartQuiz: (StudyMode) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
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
            // Dark scheme primaries are light tints; use the deep containers there so white text stays readable.
            val darkTheme = LocalDarkTheme.current
            val gradient = Brush.linearGradient(
                // Single-hue indigo ramp; every stop keeps 4.5:1+ contrast with white text.
                if (darkTheme) {
                    listOf(MaterialTheme.colorScheme.primaryContainer, Color(0xFF26226E))
                } else {
                    listOf(Color(0xFF4F46E5), Color(0xFF3730A3))
                }
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(gradient)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("今日学习任务", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        val weekPrefix = summary?.currentWeek?.let { "Week %02d · ".format(it) }.orEmpty()
                        Text(
                            weekPrefix + if (progress >= 1f) "今日目标已达成" else "还差 ${(state.dailyGoal - today).coerceAtLeast(0)} 题",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
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
                Button(
                    onClick = { onStartQuiz(StudyMode.DAILY) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF3730A3),
                    ),
                ) {
                    Text(if (progress >= 1f) "继续练习" else "开始今日任务")
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("待复习题目", "${summary?.dueCount ?: 0}", Modifier.weight(1f))
                    StatCard("连续学习", "${summary?.streakDays ?: 0} 天", Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = { onStartQuiz(StudyMode.REVIEW_DUE) },
                    enabled = (summary?.dueCount ?: 0) > 0,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(if ((summary?.dueCount ?: 0) > 0) "开始今日复习" else "今天暂无待复习题目")
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("最近 7 天完成题数")
                DailyBars(state.daily)
            }
        }
    }
}
