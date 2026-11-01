package com.agentlearning.feature.knowledge.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.domain.repository.ReadKeys
import com.agentlearning.feature.knowledge.ReadCountPill
import com.agentlearning.feature.knowledge.TrackReadingTime
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.AppLinearProgress
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.SpeechActionIcon
import kotlinx.coroutines.launch

/** Content of the "学习计划" tab: the bundled plan grouped by month and week. */
@Composable
fun StudyPlanList(
    query: String,
    listState: LazyListState,
    onDocumentClick: (String) -> Unit,
    onClearQuery: (() -> Unit)? = null,
    viewModel: StudyPlanViewModel = hiltViewModel(),
) {
    LaunchedEffect(query) { viewModel.setQuery(query) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val readCounts by viewModel.readCounts.collectAsStateWithLifecycle()
    when (val s = state) {
        PlanLoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is PlanLoadState.Error -> EmptyHint(s.message)
        is PlanLoadState.Ready -> {
            if (s.value.months.isEmpty() && s.value.guides.isEmpty()) {
                val hasQuery = query.isNotBlank()
                EmptyHint(
                    text = if (hasQuery) "没有匹配的学习内容" else "暂无学习计划",
                    icon = if (hasQuery) Icons.Filled.Search else Icons.Filled.Info,
                    actionText = if (hasQuery) "清除搜索" else null,
                    onAction = onClearQuery,
                )
            } else {
                StudyPlanContent(s.value, readCounts, listState, onDocumentClick)
            }
        }
    }
}

@Composable
private fun StudyPlanContent(
    plan: StudyPlan,
    readCounts: Map<String, Int>,
    listState: LazyListState,
    onDocumentClick: (String) -> Unit,
) {
    fun readCount(documentId: String) = readCounts[ReadKeys.planDocument(documentId)] ?: 0
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (plan.guides.isNotEmpty()) {
            item(key = "guides-header") {
                Text("计划指南", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            }
            plan.guides.forEach { guide ->
                item(key = "guide-${guide.id}") {
                    PlanCard(guide.title, guide.subtitle, pill = null, readCount = readCount(guide.id)) {
                        onDocumentClick(guide.id)
                    }
                }
            }
        }
        plan.months.forEach { month ->
            item(key = "month-${month.number}") {
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Month ${month.number} · ${month.title}", style = MaterialTheme.typography.titleMedium)
                    month.goal?.let {
                        Text(
                            "月度目标：$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            month.weeks.forEach { week ->
                item(key = "plan-week-${week.number}") {
                    PlanCard(
                        title = week.title,
                        subtitle = "课程安排 · 学习讲义 · 代码示例",
                        pill = "Week %02d".format(week.number),
                        readCount = readCount(week.document.id),
                    ) { onDocumentClick(week.document.id) }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(title: String, subtitle: String, pill: String?, readCount: Int, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pill?.let { Pill(it) }
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                ReadCountPill(readCount)
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class TocEntry(val index: Int, val level: Int, val title: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanDetailScreen(
    onBack: () -> Unit,
    viewModel: StudyPlanDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val document = (state as? PlanLoadState.Ready)?.value
    TrackReadingTime(onResume = viewModel::onReadingResumed, onPause = viewModel::onReadingPaused)

    val blocks = remember(document?.markdown) {
        document?.markdown?.let(MarkdownParser::parse).orEmpty()
    }
    val headings = remember(blocks) {
        blocks.mapIndexedNotNull { index, block ->
            val heading = block as? MdBlock.Heading ?: return@mapIndexedNotNull null
            if (heading.level > 3) return@mapIndexedNotNull null
            // Item 0 is the subtitle pill, so block i is rendered at item i + 1.
            TocEntry(index = index + 1, level = heading.level, title = heading.text)
        }
    }
    val listState = rememberLazyListState()
    val readProgress by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val first = info.visibleItemsInfo.firstOrNull()
            if (total <= 1 || first == null) {
                0f
            } else {
                // First visible item plus how far it has scrolled past the top, so the bar starts at 0.
                val within = if (first.size == 0) 0f else (-first.offset.toFloat() / first.size).coerceIn(0f, 1f)
                ((first.index + within) / (total - 1)).coerceIn(0f, 1f)
            }
        }
    }
    var showToc by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            document?.title.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (headings.isNotEmpty()) {
                            IconButton(onClick = { showToc = true }) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "目录")
                            }
                        }
                        document?.let {
                            SpeechActionIcon("${it.title}\n${it.subtitle}\n${it.markdown}")
                        }
                    },
                )
                if (document != null) {
                    AppLinearProgress(
                        progress = readProgress,
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                    )
                }
            }
        },
    ) { padding ->
        when (val s = state) {
            PlanLoadState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is PlanLoadState.Error -> EmptyHint(s.message, Modifier.padding(padding))
            is PlanLoadState.Ready -> SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "subtitle") { Pill(s.value.subtitle) }
                    markdownItems(blocks)
                }
            }
        }
    }

    if (showToc) {
        ModalBottomSheet(onDismissRequest = { showToc = false }) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            ) {
                Text(
                    "目录",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
                headings.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable {
                                showToc = false
                                scope.launch { listState.animateScrollToItem(entry.index) }
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        Text(
                            entry.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = ((entry.level - 1) * 12).dp),
                        )
                    }
                }
            }
        }
    }
}
