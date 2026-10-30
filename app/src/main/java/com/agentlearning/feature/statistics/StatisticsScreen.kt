package com.agentlearning.feature.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.SectionTitle
import com.agentlearning.ui.components.StatCard
import com.agentlearning.ui.components.asPercent

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("学习统计", style = MaterialTheme.typography.headlineMedium) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("累计正确率", summary?.accuracy.asPercent(), Modifier.weight(1f))
                StatCard("连续学习", "${summary?.streakDays ?: 0} 天", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("累计答题", "${summary?.totalAnswers ?: 0}", Modifier.weight(1f))
                StatCard("当前错题", "${state.wrongCount}", Modifier.weight(1f))
            }
        }

        item { SectionTitle("最近 7 天完成题数") }
        item { DailyBars(state.daily) }

        item { SectionTitle("每周正确率") }
        if (state.weekly.isEmpty()) {
            item { Text("暂无数据") }
        } else {
            items(state.weekly.size) { index ->
                val week = state.weekly[index]
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${week.label}  ${week.accuracy.asPercent()}（${week.correct}/${week.total}）")
                    LinearProgressIndicator(progress = { week.accuracy ?: 0f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item { SectionTitle("课程模块") }
        items(state.courses.size) { index ->
            val course = state.courses[index]
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(course.course.title, style = MaterialTheme.typography.titleSmall)
                    val completion = if (course.total == 0) 0f else course.answered.toFloat() / course.total
                    Text("课程完成 ${completion.asPercent()}（${course.answered}/${course.total} 题）")
                    LinearProgressIndicator(progress = { completion }, modifier = Modifier.fillMaxWidth())
                    Text("掌握度 ${course.mastery.asPercent()}")
                    LinearProgressIndicator(progress = { course.mastery }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun DailyBars(daily: List<DailyStat>) {
    val max = (daily.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    Row(
        Modifier.fillMaxWidth().height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        daily.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Text("${day.total}", style = MaterialTheme.typography.labelSmall)
                Box(
                    Modifier
                        .width(24.dp)
                        .height((90 * day.total / max).dp.coerceAtLeast(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text("${day.date.monthValue}/${day.date.dayOfMonth}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
