package com.agentlearning.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.data.repository.SettingsRepositoryImpl
import com.agentlearning.ui.components.AppCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

private const val GOAL_STEP = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmReset by remember { mutableStateOf(false) }
    var fontSlider by remember(state.fontScale) { mutableFloatStateOf(state.fontScale) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::export) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::prepareRestore) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsCard("字体大小") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("小", style = MaterialTheme.typography.labelMedium)
                    Text("${(fontSlider * 100).roundToInt()}%", style = MaterialTheme.typography.titleSmall)
                    Text("大", style = MaterialTheme.typography.titleMedium)
                }
                Slider(
                    value = fontSlider,
                    onValueChange = { fontSlider = it },
                    onValueChangeFinished = { viewModel.setFontScale(fontSlider) },
                    valueRange = SettingsRepositoryImpl.MIN_FONT_SCALE..SettingsRepositoryImpl.MAX_FONT_SCALE,
                    steps = 5,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { viewModel.setFontScale(SettingsRepositoryImpl.DEFAULT_FONT_SCALE) },
                        enabled = state.fontScale != SettingsRepositoryImpl.DEFAULT_FONT_SCALE,
                    ) { Text("恢复默认") }
                }
            }

            SettingsCard("答题音效") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("答对、答错时播放提示音")
                    Switch(checked = state.soundEnabled, onCheckedChange = viewModel::setSoundEnabled)
                }
            }

            SettingsCard("每日学习目标") {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
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
            }

            SettingsCard("题库") {
                Text("题库内容版本：${state.bankVersion}")
            }

            SettingsCard("数据备份") {
                Text(
                    "备份文件包含答题历史、复习进度和学习会话。卸载 App 会清除本地数据，请定期导出。",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = { exportLauncher.launch("agent-learning-backup.json") }, modifier = Modifier.fillMaxWidth()) {
                    Text("导出学习数据")
                }
                OutlinedButton(
                    onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("从备份恢复") }
            }

            SettingsCard("危险操作") {
                OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("重置本地学习数据", color = MaterialTheme.colorScheme.error)
                }
            }
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
