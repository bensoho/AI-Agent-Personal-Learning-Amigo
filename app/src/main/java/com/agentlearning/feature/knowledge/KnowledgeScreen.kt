package com.agentlearning.feature.knowledge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.feature.curriculum.TopicDetailViewModel
import com.agentlearning.feature.knowledge.plan.StudyPlanList
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.SpeechActionIcon

private val knowledgeTabs = listOf("题库知识点", "学习计划")

@Composable
fun KnowledgeScreen(
    onTopicClick: (String) -> Unit,
    onPlanDocumentClick: (String) -> Unit,
    viewModel: KnowledgeViewModel = hiltViewModel(),
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filterQuery by viewModel.filterQuery.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var searchVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(query) { if (query.isNotEmpty()) searchVisible = true }

    // Each tab keeps its own scroll position.
    val topicListState = rememberLazyListState()
    val planListState = rememberLazyListState()
    val listState = if (selectedTab == 0) topicListState else planListState
    val latestQuery by rememberUpdatedState(query)
    val revealThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    val pullToReveal = Modifier.pointerInput(listState, revealThreshold) {
        awaitEachGesture {
            var pull = 0f
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: break
                if (!change.pressed) break
                val dy = change.position.y - change.previousPosition.y
                when {
                    dy > 0f && !listState.canScrollBackward -> {
                        if (!searchVisible) {
                            pull += dy
                            if (pull >= revealThreshold) {
                                searchVisible = true
                                pull = 0f
                            }
                        }
                    }
                    dy < 0f -> {
                        pull = 0f
                        if (searchVisible && latestQuery.isBlank()) searchVisible = false
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("知识点", style = MaterialTheme.typography.headlineMedium)
            AnimatedVisibility(
                visible = searchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                SearchField(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    placeholder = if (selectedTab == 0) "搜索知识点或关键词" else "搜索学习计划内容",
                )
            }
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

        Box(Modifier.weight(1f).then(pullToReveal)) {
            if (selectedTab == 1) {
                StudyPlanList(
                    query = filterQuery,
                    listState = listState,
                    onDocumentClick = onPlanDocumentClick,
                )
            } else {
                TopicList(
                    courses = courses,
                    query = filterQuery,
                    listState = listState,
                    onTopicClick = onTopicClick,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().height(40.dp),
        shape = RoundedCornerShape(50),
        color = colors.surfaceContainerHigh,
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                },
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "清除", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TopicList(
    courses: List<CourseOutline>,
    query: String,
    listState: LazyListState,
    onTopicClick: (String) -> Unit,
) {
    if (courses.isEmpty()) {
        EmptyHint(if (query.isBlank()) "暂无知识点" else "没有匹配的知识点")
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            courses.forEach { course ->
                item(key = "course-${course.course.id}") {
                    Text(
                        "Month ${course.course.monthNumber} · ${course.course.title}",
                        style = MaterialTheme.typography.titleMedium,
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
                actions = {
                    topic?.let {
                        SpeechActionIcon(
                            buildString {
                                append(it.title)
                                it.description?.let { description -> append("\n").append(description) }
                                it.notes?.let { notes -> append("\n").append(notes) }
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Only text is selectable, so long-pressing the button never starts a text selection.
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
