package com.agentlearning.feature.knowledge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.feature.curriculum.TopicDetailViewModel
import com.agentlearning.feature.knowledge.plan.StudyPlanList
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill

private val knowledgeTabs = listOf("题库知识点", "学习计划")

@Composable
fun KnowledgeScreen(
    onTopicClick: (String) -> Unit,
    onPlanDocumentClick: (String) -> Unit,
    viewModel: KnowledgeViewModel = hiltViewModel(),
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("知识点", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(if (selectedTab == 0) "搜索知识点或关键词" else "搜索学习计划内容") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "清除")
                        }
                    }
                },
                shape = MaterialTheme.shapes.large,
            )
            TabRow(selectedTabIndex = selectedTab) {
                knowledgeTabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                    )
                }
            }
        }

        if (selectedTab == 1) {
            StudyPlanList(query = query, onDocumentClick = onPlanDocumentClick)
        } else {
            TopicList(courses = courses, query = query, onTopicClick = onTopicClick)
        }
    }
}

@Composable
private fun TopicList(
    courses: List<CourseOutline>,
    query: String,
    onTopicClick: (String) -> Unit,
) {
    if (courses.isEmpty()) {
        EmptyHint(if (query.isBlank()) "暂无知识点" else "没有匹配的知识点")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            courses.forEach { course ->
                item(key = "course-${course.course.id}") {
                    Text(
                        "Month ${course.course.monthNumber} · ${course.course.title}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                course.weeks.forEach { week ->
                    item(key = "week-${week.weekNumber}") {
                        Text(
                            "Week %02d  %s".format(week.weekNumber, week.title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    week.topics.forEach { progress ->
                        val topic = progress.topic
                        item(key = "topic-${topic.id}") {
                            AppCard(Modifier.fillMaxWidth(), onClick = { onTopicClick(topic.id) }) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(topic.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        if (topic.notes != null) {
                                            Pill("笔记")
                                        } else {
                                            Pill(
                                                "整理中",
                                                container = MaterialTheme.colorScheme.surfaceVariant,
                                                content = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    topic.description?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeDetailScreen(
    onBack: () -> Unit,
    onStartQuiz: () -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val topic = state.topic

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.title.orEmpty(), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        SelectionContainer {
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            topic?.let {
                Pill("Week %02d · %s".format(it.weekNumber, it.weekTitle))
                it.description?.let { text ->
                    Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (it.notes != null) {
                    NoteContent(it.notes)
                } else {
                    Text("该知识点的笔记整理中。", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (state.questions.isNotEmpty()) {
                Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                    Text("练习本知识点（${state.questions.size} 题）")
                }
            }
        }
        }
    }
}
