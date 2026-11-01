package com.agentlearning.feature.curriculum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.LearningStatus
import com.agentlearning.domain.model.TopicProgress
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.AppLinearProgress
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.SectionTitle
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label
import kotlinx.coroutines.launch

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
    val currentWeek by viewModel.currentWeek.collectAsStateWithLifecycle()
    var expandedWeeks by rememberSaveable { mutableStateOf<List<Int>?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val weekNumbers = outline.flatMap { course -> course.weeks.map { it.weekNumber } }
    val effectiveExpanded = expandedWeeks ?: listOfNotNull(currentWeek)
    val itemKeys = outline.flatMap { course ->
        listOf("course-${course.course.id}") + course.weeks.map { "week-${it.weekNumber}" }
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("课程", style = MaterialTheme.typography.headlineMedium)
            Row(Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        val index = itemKeys.indexOf("week-$currentWeek")
                        if (index >= 0) {
                            expandedWeeks = (effectiveExpanded + listOfNotNull(currentWeek)).distinct()
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    },
                    enabled = currentWeek != null && currentWeek in weekNumbers,
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) { Text("定位当前周") }
                TextButton(
                    onClick = { expandedWeeks = weekNumbers },
                    enabled = weekNumbers.any { it !in effectiveExpanded },
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) { Text("全部展开") }
                TextButton(
                    onClick = { expandedWeeks = emptyList() },
                    enabled = effectiveExpanded.isNotEmpty(),
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) { Text("全部收起") }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            outline.forEach { course ->
                item(key = "course-${course.course.id}") {
                    Text(
                        "Month ${course.course.monthNumber} - ${course.course.title}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                course.weeks.forEach { week ->
                    item(key = "week-${week.weekNumber}") {
                        val expanded = week.weekNumber in effectiveExpanded
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(vertical = 12.dp)) {
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        expandedWeeks = if (expanded) effectiveExpanded - week.weekNumber else effectiveExpanded + week.weekNumber
                                    }.padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f).padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            "Week %02d".format(week.weekNumber) + if (week.weekNumber == currentWeek) " · 当前学习" else "",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (week.weekNumber == currentWeek) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        SectionTitle(week.title)
                                        Text(
                                            "${week.status.label()} · ${week.answered}/${week.total} 题",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = if (expanded) "收起知识点" else "展开知识点",
                                    )
                                }
                                if (week.total > 0) {
                                    AppLinearProgress(
                                        progress = week.answered.toFloat() / week.total,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    )
                                }
                                if (expanded) {
                                    week.topics.forEach { progress ->
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                                        TopicRow(progress, onClick = { onTopicClick(progress.topic.id) })
                                    }
                                }
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
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(progress.topic.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 12.dp))
        Text(
            if (progress.total == 0) "暂无题目" else "${progress.answered}/${progress.total}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}
