package com.agentlearning.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.Pill
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        if (state.needsExitConfirm) {
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
                    Text(if (state.questions.isEmpty() || state.finished) "练习" else "${state.index + 1} / ${state.questions.size}")
                },
                navigationIcon = {
                    IconButton(onClick = requestExit) { Icon(Icons.Filled.Close, contentDescription = "退出练习") }
                },
            )
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

            state.finished -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("练习完成", style = MaterialTheme.typography.headlineMedium)
                Text("答对 ${state.correctCount} / ${state.questions.size} 题")
                Button(onClick = exit) { Text("返回") }
            }

            question != null -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppLinearProgress(
                    progress = (state.index + 1).toFloat() / state.questions.size,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.isLast) "完成" else "下一题")
                    }
                } else {
                    Button(
                        onClick = viewModel::submit,
                        enabled = state.selected.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("提交答案") }
                }
            }
        }
    }
}
