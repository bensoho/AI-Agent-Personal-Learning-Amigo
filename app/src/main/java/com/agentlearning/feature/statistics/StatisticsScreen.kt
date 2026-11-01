package com.agentlearning.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
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
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.AppLinearProgress
import com.agentlearning.ui.components.DailyBars
import com.agentlearning.ui.components.SectionTitle
import com.agentlearning.ui.components.asPercent

@Composable
fun StatisticsScreen(
    onStartQuiz: (StudyMode) -> Unit,
    onPracticeTopic: (String) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary
    if (summary == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val week = weekActivity(state.daily, state.today)
    val topics = consolidationTopics(state.courses)
    var historyExpanded by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Text("学习统计", style = MaterialTheme.typography.headlineMedium) }

        item {
            AppCard(Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("本周学习")
                    Text("${week.answers} 题", style = MaterialTheme.typography.headlineLarge)
                    Text("周一至今 · 学习 ${week.activeDays} 天", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (week.answers == 0) "本周还没有答题记录，完成练习后会显示正确率。"
                        else "本周正确率 ${week.accuracy.asPercent()} · 答对 ${week.correct}/${week.answers} 题",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (summary.dueCount > 0 || state.wrongCount > 0) {
            item {
                Button(
                    onClick = {
                        onStartQuiz(if (summary.dueCount > 0) StudyMode.REVIEW_DUE else StudyMode.REVIEW_RECENT_WRONG)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (summary.dueCount > 0) "开始今日复习（${summary.dueCount} 题待复习）"
                        else "练习当前错题（${state.wrongCount} 题）"
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("优先巩固")
                Text(
                    "按复习状态中的「学习中」题目数量排序，最多展示 3 个知识点。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (topics.isEmpty()) {
                    Text(
                        if (summary.totalAnswers == 0) "完成练习后，这里会显示需要巩固的知识点。"
                        else "当前没有「学习中」题目，可继续按课程计划学习。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(topics.size) { index ->
            val topic = topics[index]
            AppCard(Modifier.fillMaxWidth(), onClick = { onPracticeTopic(topic.topic.id) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(topic.topic.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Week %02d · 学习中 %d 题".format(topic.topic.weekNumber, topic.learning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("练习本知识点", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("最近 7 天完成题数")
                DailyBars(state.daily)
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionTitle("累计概览")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatisticsMetric("累计答题", "${summary.totalAnswers} 题", Modifier.weight(1f))
                        StatisticsMetric("累计正确率", summary.accuracy.asPercent(), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatisticsMetric("连续学习", "${summary.streakDays} 天", Modifier.weight(1f))
                        StatisticsMetric("当前错题", "${state.wrongCount} 题", Modifier.weight(1f))
                    }
                    Text(
                        "答题数按作答次数统计，包含重复练习；当前错题按最近一次答错的题目去重。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.weekly.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { historyExpanded = !historyExpanded }) {
                        Text(if (historyExpanded) "收起历史周统计" else "查看历史周统计")
                    }
                    if (historyExpanded) {
                        state.weekly.forEach { history ->
                            Text(
                                "${history.label} · ${history.accuracy.asPercent()}（${history.correct}/${history.total}）",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }

        item { SectionTitle("课程进度") }
        items(state.courses.size) { index ->
            val course = state.courses[index]
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(course.course.title, style = MaterialTheme.typography.titleSmall)
                    val completion = if (course.total == 0) 0f else course.answered.toFloat() / course.total
                    Text("已练习 ${course.answered}/${course.total} 题 · ${completion.asPercent()}")
                    AppLinearProgress(progress = completion, modifier = Modifier.fillMaxWidth())
                    Text(
                        "掌握度 ${course.mastery.asPercent()}（按复习状态估算）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}
