package com.agentlearning.feature.knowledge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.repository.ReadKeys
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
    val outline by viewModel.outline.collectAsStateWithLifecycle()
    val scope by viewModel.scope.collectAsStateWithLifecycle()
    val readCounts by viewModel.readCounts.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filterQuery by viewModel.filterQuery.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current

    // Each tab keeps its own scroll position.
    val topicListState = rememberLazyListState()
    val planListState = rememberLazyListState()
    val listState = if (selectedTab == 0) topicListState else planListState
    var previousSearch by remember { mutableStateOf(filterQuery to scope) }
    LaunchedEffect(filterQuery, scope) {
        if (previousSearch != (filterQuery to scope)) {
            topicListState.scrollToItem(0)
            planListState.scrollToItem(0)
            previousSearch = filterQuery to scope
        }
    }
    val onMonthChange: (Int?) -> Unit = {
        focusManager.clearFocus()
        viewModel.selectMonth(it)
    }
    val onWeekChange: (Int?) -> Unit = {
        focusManager.clearFocus()
        viewModel.selectWeek(it)
    }

    Column(Modifier.fillMaxSize().imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("知识点", style = MaterialTheme.typography.headlineMedium)
            SearchField(
                query = query,
                onQueryChange = viewModel::setQuery,
                placeholder = if (selectedTab == 0) "搜索知识点或关键词" else "搜索学习计划内容",
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

        Box(Modifier.weight(1f)) {
            if (selectedTab == 1) {
                StudyPlanList(
                    query = filterQuery,
                    scope = scope,
                    onMonthChange = onMonthChange,
                    onWeekChange = onWeekChange,
                    listState = listState,
                    onDocumentClick = onPlanDocumentClick,
                    onResetFilters = viewModel::resetFilters,
                )
            } else {
                Column {
                    KnowledgeFilters(
                        months = outline.map { it.course.monthNumber },
                        weeks = outline.filter { scope.monthNumber == null || it.course.monthNumber == scope.monthNumber }
                            .flatMap { course -> course.weeks.map { it.weekNumber } },
                        scope = scope,
                        onMonthChange = onMonthChange,
                        onWeekChange = onWeekChange,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    TopicList(
                        courses = courses,
                        readCounts = readCounts,
                        query = filterQuery,
                        hasFilters = scope.isActive,
                        listState = listState,
                        onTopicClick = onTopicClick,
                        onResetFilters = viewModel::resetFilters,
                    )
                }
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
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(50),
        color = colors.surfaceContainerHigh,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "清除", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TopicList(
    courses: List<CourseOutline>,
    readCounts: Map<String, Int>,
    query: String,
    hasFilters: Boolean,
    listState: LazyListState,
    onTopicClick: (String) -> Unit,
    onResetFilters: () -> Unit,
) {
    if (courses.isEmpty()) {
        val hasQuery = query.isNotBlank() || hasFilters
        EmptyHint(
            text = if (hasQuery) "当前筛选下没有匹配的知识点" else "暂无知识点",
            icon = if (hasQuery) Icons.Filled.Search else Icons.Filled.Info,
            actionText = if (hasQuery) "重置筛选" else null,
            onAction = onResetFilters,
        )
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            courses.forEach { course ->
                item(key = "course-${course.course.id}") {
                    SearchResultText(
                        "Month ${course.course.monthNumber} · ${course.course.title}",
                        query = query,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                course.weeks.forEach { week ->
                    item(key = "week-${week.weekNumber}") {
                        SearchResultText(
                            "Week %02d  %s".format(week.weekNumber, week.title),
                            query = query,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    week.topics.forEach { progress ->
                        val topic = progress.topic
                        item(key = "topic-${topic.id}") {
                            AppCard(Modifier.fillMaxWidth(), onClick = { onTopicClick(topic.id) }) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SearchResultText(topic.title, query, style = MaterialTheme.typography.titleMedium)
                                    topic.description?.let {
                                        SearchResultText(it, query, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    val excerpt = remember(topic.notes, query) {
                                        if (query.isNotBlank() && !topic.title.contains(query, ignoreCase = true) &&
                                            topic.description?.contains(query, ignoreCase = true) != true
                                        ) topic.notes?.let { searchExcerpt(it, query) } else null
                                    }
                                    excerpt?.let {
                                        SearchResultText(
                                            it, query, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            if (topic.notes != null) "笔记" else "整理中",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        ReadCountPill(readCounts[ReadKeys.topic(topic.id)] ?: 0)
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
    TrackReadingTime(onResume = viewModel::onReadingResumed, onPause = viewModel::onReadingPaused)

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
        bottomBar = {
            // Kept on screen: the practice entry used to sit at the end of long notes.
            if (state.questions.isNotEmpty()) {
                Surface(tonalElevation = 2.dp) {
                    Button(
                        onClick = onStartQuiz,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text("练习本知识点（${state.questions.size} 题）")
                    }
                }
            }
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
        }
    }
}
