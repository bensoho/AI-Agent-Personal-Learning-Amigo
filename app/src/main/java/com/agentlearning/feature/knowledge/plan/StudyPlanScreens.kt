package com.agentlearning.feature.knowledge.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.SpeechActionIcon

/** Content of the "学习计划" tab: the bundled plan grouped by month and week. */
@Composable
fun StudyPlanList(
    query: String,
    listState: LazyListState,
    onDocumentClick: (String) -> Unit,
    viewModel: StudyPlanViewModel = hiltViewModel(),
) {
    LaunchedEffect(query) { viewModel.setQuery(query) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        PlanLoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is PlanLoadState.Error -> EmptyHint(s.message)
        is PlanLoadState.Ready -> {
            if (s.value.months.isEmpty() && s.value.guides.isEmpty()) {
                EmptyHint(if (query.isBlank()) "暂无学习计划" else "没有匹配的学习内容")
            } else {
                StudyPlanContent(s.value, listState, onDocumentClick)
            }
        }
    }
}

@Composable
private fun StudyPlanContent(plan: StudyPlan, listState: LazyListState, onDocumentClick: (String) -> Unit) {
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
                    PlanCard(guide.title, guide.subtitle, pill = null) { onDocumentClick(guide.id) }
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
                    ) { onDocumentClick(week.document.id) }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(title: String, subtitle: String, pill: String?, onClick: () -> Unit) {
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
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanDetailScreen(
    onBack: () -> Unit,
    viewModel: StudyPlanDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val document = (state as? PlanLoadState.Ready)?.value

    Scaffold(
        topBar = {
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
                    document?.let {
                        SpeechActionIcon("${it.title}\n${it.subtitle}\n${it.markdown}")
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            PlanLoadState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is PlanLoadState.Error -> EmptyHint(s.message, Modifier.padding(padding))
            is PlanLoadState.Ready -> SelectionContainer {
                val blocks = remember(s.value.markdown) { MarkdownParser.parse(s.value.markdown) }
                LazyColumn(
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
}
