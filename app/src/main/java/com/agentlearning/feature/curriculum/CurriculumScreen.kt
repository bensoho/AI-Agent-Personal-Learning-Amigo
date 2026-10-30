package com.agentlearning.feature.curriculum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.LearningStatus
import com.agentlearning.domain.model.TopicProgress
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.SectionTitle
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label

private fun LearningStatus.label() = when (this) {
    LearningStatus.NOT_STARTED -> "未开始"
    LearningStatus.IN_PROGRESS -> "进行中"
    LearningStatus.COMPLETED -> "已完成"
}

@Composable
fun CurriculumScreen(
    onTopicClick: (String) -> Unit,
    viewModel: CurriculumViewModel = hiltViewModel(),
) {
    val outline by viewModel.outline.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        outline.forEach { course ->
            item(key = "course-${course.course.id}") {
                Text(
                    "Month ${course.course.monthNumber} - ${course.course.title}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            course.weeks.forEach { week ->
                item(key = "week-${week.weekNumber}") {
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(vertical = 12.dp)) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SectionTitle("Week %02d  %s".format(week.weekNumber, week.title), Modifier.weight(1f))
                                Pill(week.status.label())
                            }
                            if (week.total > 0) {
                                LinearProgressIndicator(
                                    progress = { week.answered.toFloat() / week.total },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                            week.topics.forEach { progress ->
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                TopicRow(progress, onClick = { onTopicClick(progress.topic.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicRow(progress: TopicProgress, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(progress.topic.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            if (progress.total == 0) "暂无题目" else "${progress.answered}/${progress.total}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    onBack: () -> Unit,
    onStartQuiz: () -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.topic?.title.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        if (state.questions.isEmpty()) {
            EmptyHint("该知识点暂无题目", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.topic?.description?.let { item { Text(it) } }
                item {
                    Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                        Text("开始练习（${state.questions.size} 题）")
                    }
                }
                items(state.questions, key = { it.id }) { question ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(question.title, style = MaterialTheme.typography.bodyLarge)
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
}
