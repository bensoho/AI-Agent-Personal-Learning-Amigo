package com.agentlearning.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.data.repository.SettingsRepositoryImpl
import com.agentlearning.ui.components.SectionTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GOAL_STEP = 5

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmReset by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::export) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::prepareRestore) }

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)

        SectionTitle("每日学习目标")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { viewModel.setDailyGoal(state.dailyGoal - GOAL_STEP) },
                enabled = state.dailyGoal > SettingsRepositoryImpl.MIN_DAILY_GOAL,
            ) { Text("-") }
            Text("${state.dailyGoal} 题", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { viewModel.setDailyGoal(state.dailyGoal + GOAL_STEP) },
                enabled = state.dailyGoal < SettingsRepositoryImpl.MAX_DAILY_GOAL,
            ) { Text("+") }
        }

        SectionTitle("题库")
        Text("题库内容版本：${state.bankVersion}")

        SectionTitle("数据备份")
        Text("备份文件包含答题历史、复习进度和学习会话。卸载 App 会清除本地数据，请定期导出。", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { exportLauncher.launch("agent-learning-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text("导出学习数据")
        }
        OutlinedButton(
            onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("从备份恢复") }

        SectionTitle("危险操作")
        OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
            Text("重置本地学习数据", color = MaterialTheme.colorScheme.error)
        }
    }

    state.pendingRestore?.let { preview ->
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(preview.exportedAt))
        AlertDialog(
            onDismissRequest = viewModel::cancelRestore,
            title = { Text("确认恢复") },
            text = {
                Text(
                    "备份时间：$date\n题库版本：${preview.bankContentVersion}\n" +
                        "答题记录：${preview.answerCount}\n复习记录：${preview.reviewCount}\n学习会话：${preview.sessionCount}\n\n" +
                        "恢复将覆盖当前所有学习记录。"
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmRestore) { Text("覆盖并恢复") } },
            dismissButton = { TextButton(onClick = viewModel::cancelRestore) { Text("取消") } },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("重置本地数据") },
            text = { Text("将删除所有答题历史、复习进度和学习会话，且无法撤销。建议先导出备份。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    viewModel.resetData()
                }) { Text("确认重置", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("取消") } },
        )
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMessage,
            text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::dismissMessage) { Text("好的") } },
        )
    }
}
