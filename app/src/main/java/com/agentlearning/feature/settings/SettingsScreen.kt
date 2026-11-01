package com.agentlearning.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.data.repository.SettingsRepositoryImpl
import com.agentlearning.domain.model.ThemeMode
import com.agentlearning.ui.components.AppCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

/** One-line label on the left with its control on the right. */
@Composable
private fun SettingRow(label: String, control: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically, content = control)
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(32.dp),
        contentPadding = PaddingValues(0.dp),
    ) { Text(symbol, style = MaterialTheme.typography.titleSmall) }
}

private const val GOAL_STEP = 5
private const val AUTHOR_EMAIL = "bensoho.liu@gmail.com"

private val themeOptions = listOf(
    ThemeMode.SYSTEM to "跟随系统",
    ThemeMode.LIGHT to "浅色",
    ThemeMode.DARK to "深色",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmReset by remember { mutableStateOf(false) }
    var fontSlider by remember(state.fontScale) { mutableFloatStateOf(state.fontScale) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "-"
    }

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
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsCard("外观") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    themeOptions.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                            modifier = Modifier.height(36.dp),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text(label, style = MaterialTheme.typography.labelLarge) }
                    }
                }
                SettingRow("字体大小 ${(fontSlider * 100).roundToInt()}%") {
                    TextButton(
                        onClick = { viewModel.setFontScale(SettingsRepositoryImpl.DEFAULT_FONT_SCALE) },
                        enabled = state.fontScale != SettingsRepositoryImpl.DEFAULT_FONT_SCALE,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(32.dp),
                    ) { Text("恢复默认", style = MaterialTheme.typography.labelLarge) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val thumbInteraction = remember { MutableInteractionSource() }
                    Text("小", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = fontSlider,
                        onValueChange = { fontSlider = it },
                        onValueChangeFinished = { viewModel.setFontScale(fontSlider) },
                        valueRange = SettingsRepositoryImpl.MIN_FONT_SCALE..SettingsRepositoryImpl.MAX_FONT_SCALE,
                        steps = 5,
                        interactionSource = thumbInteraction,
                        modifier = Modifier.weight(1f).height(32.dp).padding(horizontal = 4.dp),
                        thumb = { SliderDefaults.Thumb(thumbInteraction, thumbSize = DpSize(18.dp, 18.dp)) },
                        track = { state ->
                            SliderDefaults.Track(
                                sliderState = state,
                                modifier = Modifier.height(4.dp),
                                colors = SliderDefaults.colors(
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    inactiveTickColor = MaterialTheme.colorScheme.outline,
                                    activeTickColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                thumbTrackGapSize = 0.dp,
                                trackInsideCornerSize = 0.dp,
                            )
                        },
                    )
                    Text("大", style = MaterialTheme.typography.titleSmall)
                }
            }

            SettingsCard("学习") {
                SettingRow("答题音效") {
                    // Switch is 52x32dp; scaling keeps it from dominating a one-line row.
                    Switch(
                        checked = state.soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled,
                        modifier = Modifier.scale(0.8f),
                    )
                }
                SettingRow("每日目标") {
                    StepperButton("−", enabled = state.dailyGoal > SettingsRepositoryImpl.MIN_DAILY_GOAL) {
                        viewModel.setDailyGoal(state.dailyGoal - GOAL_STEP)
                    }
                    Text(
                        "${state.dailyGoal} 题",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 56.dp),
                    )
                    StepperButton("+", enabled = state.dailyGoal < SettingsRepositoryImpl.MAX_DAILY_GOAL) {
                        viewModel.setDailyGoal(state.dailyGoal + GOAL_STEP)
                    }
                }
            }

            SettingsCard("数据") {
                SettingRow("题库版本") {
                    Text(
                        state.bankVersion.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "备份包含答题历史、复习进度和学习会话。卸载 App 会清除本地数据，请定期导出。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("agent-learning-backup.json") },
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) { Text("导出数据", style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) { Text("从备份恢复", style = MaterialTheme.typography.labelLarge) }
                }
                TextButton(
                    onClick = { confirmReset = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(36.dp),
                ) {
                    Text("重置本地学习数据", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
            }

            SettingsCard("关于") {
                SettingRow("Agent Learning") {
                    Text(
                        "v$versionName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SettingRow("Author") {
                    Text(
                        AUTHOR_EMAIL,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            // No mail app installed makes openUri throw; ignore it.
                            runCatching { uriHandler.openUri("mailto:$AUTHOR_EMAIL") }
                        },
                    )
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
