package com.agentlearning.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.agentlearning.feature.quiz.components.AnswerExplanation
import com.agentlearning.feature.quiz.components.ChoiceOptions
import com.agentlearning.feature.quiz.components.CodeBlock
import com.agentlearning.ui.components.EmptyHint
import com.agentlearning.ui.components.displayName
import com.agentlearning.ui.components.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onExit: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val question = state.current

    BackHandler(onBack = onExit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.questions.isEmpty() || state.finished) "练习" else "${state.index + 1} / ${state.questions.size}")
                },
                navigationIcon = {
                    IconButton(onClick = onExit) { Icon(Icons.Filled.Close, contentDescription = "退出练习") }
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
                Button(onClick = onExit) { Text("返回") }
            }

            question != null -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LinearProgressIndicator(
                    progress = { (state.index + 1).toFloat() / state.questions.size },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "${question.type.displayName()} · ${question.difficulty.label()}" +
                        if (question.isMultiSelect) " · 多选" else "",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(question.title, style = MaterialTheme.typography.titleMedium)
                question.codeSnippet?.let { CodeBlock(it) }
                ChoiceOptions(
                    question = question,
                    selected = state.selected,
                    submitted = state.submitted,
                    onToggle = viewModel::toggleOption,
                )
                if (state.submitted) {
                    AnswerExplanation(question, state.lastCorrect)
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
