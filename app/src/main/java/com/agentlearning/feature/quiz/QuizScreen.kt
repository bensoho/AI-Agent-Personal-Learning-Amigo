package com.agentlearning.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.feature.quiz.components.AnswerExplanation
import com.agentlearning.feature.quiz.components.ChoiceOptions
import com.agentlearning.feature.quiz.components.CodeBlock
import com.agentlearning.ui.components.AppLinearProgress
import com.agentlearning.ui.components.AppCard
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label
import com.agentlearning.ui.components.asPercent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    onExit: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val question = state.current
    val scrollState = rememberScrollState()
    val explanationRequester = remember { BringIntoViewRequester() }
    val haptics = LocalHapticFeedback.current
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val exit: () -> Unit = {
        viewModel.onExit()
        onExit()
    }
    val requestExit: () -> Unit = {
        if (state.saving || state.startingReview) {
            // The footer shows the pending operation; do not cancel a record write by navigating away.
        } else if (state.needsExitConfirm) {
            confirmExit = true
        } else {
            exit()
        }
    }

    // Reveal the verdict even when long options pushed it below the fold.
    LaunchedEffect(question?.id, state.submitted) {
        if (question != null && state.submitted) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            withFrameNanos { }
            explanationRequester.bringIntoView()
        } else {
            scrollState.scrollTo(0)
        }
    }

    BackHandler(onBack = requestExit)

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("退出练习？") },
            text = { Text("已提交的答案已保存，剩余题目不会保留进度。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmExit = false
                    exit()
                }) { Text("退出") }
            },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("继续练习") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.questions.isEmpty() || state.finished) "练习"
                        else (if (state.isMistakeReview) "错题复习 · " else "") + "${state.index + 1} / ${state.questions.size}"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = requestExit, enabled = !state.saving && !state.startingReview) {
                        Icon(Icons.Filled.Close, contentDescription = "退出练习")
                    }
                },
            )
        },
        bottomBar = {
            if (!state.loading && state.questions.isNotEmpty()) {
                QuizActions(
                    state = state,
                    onSubmit = viewModel::submit,
                    onNext = viewModel::next,
                    onRetrySave = viewModel::retrySave,
                    onReviewMistakes = viewModel::reviewMistakes,
                    onExit = exit,
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }

            state.error != null && question == null -> EmptyHint(state.error.orEmpty(), Modifier.padding(padding))

            state.questions.isEmpty() -> EmptyHint("没有可练习的题目", Modifier.padding(padding))

            state.finished -> QuizResults(state, Modifier.padding(padding))

            question != null -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppLinearProgress(
                    progress = (state.index + 1).toFloat() / state.questions.size,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(question.type.displayName())
                    Pill(
                        question.difficulty.label(),
                        container = MaterialTheme.colorScheme.tertiaryContainer,
                        content = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    if (question.isMultiSelect) {
                        Pill(
                            "多选",
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            content = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                // Selection is limited to text so it cannot interfere with tapping options or buttons.
                SelectionContainer { Text(question.title, style = MaterialTheme.typography.titleMedium) }
                question.codeSnippet?.let { CodeBlock(it) }
                ChoiceOptions(
                    question = question,
                    selected = state.selected,
                    submitted = state.submitted,
                    onToggle = viewModel::toggleOption,
                )
                if (state.submitted) {
                    SelectionContainer(Modifier.bringIntoViewRequester(explanationRequester)) {
                        AnswerExplanation(question, state.lastCorrect)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizActions(
    state: QuizUiState,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onRetrySave: () -> Unit,
    onReviewMistakes: () -> Unit,
    onExit: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Column(
            Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            if (state.finished) {
                if (state.wrongQuestions.isNotEmpty()) {
                    Button(
                        onClick = onReviewMistakes,
                        enabled = !state.startingReview,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(if (state.startingReview) "正在开始复习..." else "复习本轮错题（${state.wrongQuestions.size} 题）")
                    }
                }
                TextButton(onClick = onExit, enabled = !state.startingReview, modifier = Modifier.fillMaxWidth()) {
                    Text("返回")
                }
            } else {
                Button(
                    onClick = when {
                        state.submitted && state.error != null -> onRetrySave
                        state.submitted -> onNext
                        else -> onSubmit
                    },
                    enabled = !state.saving && (if (state.submitted) state.canAdvance || state.error != null else state.selected.isNotEmpty()),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(
                        when {
                            state.saving -> "正在保存..."
                            state.submitted && state.error != null -> if (state.answerSaved) "重试完成" else "重试保存"
                            state.submitted -> if (state.isLast) "查看结果" else "下一题"
                            else -> "提交答案"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResults(state: QuizUiState, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(if (state.isMistakeReview) "错题复习完成" else "练习完成", style = MaterialTheme.typography.headlineMedium)
        AppCard(Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("本轮正确率", style = MaterialTheme.typography.titleMedium)
                Text(state.accuracy.asPercent(), style = MaterialTheme.typography.headlineLarge)
                Text("答对 ${state.correctCount} / ${state.questions.size} 题", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("答对", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${state.correctCount} 题", style = MaterialTheme.typography.headlineSmall)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("错题", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${state.wrongCount} 题", style = MaterialTheme.typography.headlineSmall)
            }
        }
        Text(
            if (state.wrongCount == 0) "本轮全部答对，继续按计划学习吧。"
            else "答题记录已保存。可只练习本轮错题，新的作答也会更新复习进度。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
