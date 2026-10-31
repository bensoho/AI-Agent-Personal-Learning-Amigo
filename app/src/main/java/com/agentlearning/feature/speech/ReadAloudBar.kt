package com.agentlearning.feature.speech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.agentlearning.ui.components.MediaIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ReadAloudUiState(
    val playback: ReadAloudState = ReadAloudState(),
    val configured: Boolean = true,
    val preferredSpeed: Float = SpeechPreferences.DEFAULT_SPEED,
)

@HiltViewModel
class ReadAloudViewModel @Inject constructor(
    private val controller: ReadAloudController,
    preferences: SpeechPreferences,
) : ViewModel() {

    val uiState: StateFlow<ReadAloudUiState> =
        combine(controller.state, preferences.config) { playback, config ->
            ReadAloudUiState(playback, config.isConfigured, config.speed)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReadAloudUiState())

    fun play(document: ReadingDocument) = controller.play(document)
    fun pause() = controller.pause()
    fun resume() = controller.resume()
    fun next() = controller.next()
    fun previous() = controller.previous()
    fun stop() = controller.stop()
    fun setSpeed(speed: Float) = controller.setSpeed(speed)
}

private val speedSteps = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

private fun Float.speedLabel(): String = if (this % 1f == 0f) "${toInt()}.0x" else "${this}x"

/** Block index highlighted on the page showing [documentId], or null when that page is not being read. */
fun ReadAloudUiState.highlightFor(documentId: String): Int? =
    playback.takeIf { it.isActiveFor(documentId) }?.blockIndex?.takeIf { it >= 0 }

/** Bottom bar to read [document] aloud; it controls whatever is playing once this page is being read. */
@Composable
fun ReadAloudBar(
    document: ReadingDocument?,
    state: ReadAloudUiState,
    viewModel: ReadAloudViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (document == null || document.segments.isEmpty()) return
    var askToConfigure by rememberSaveable { mutableStateOf(false) }
    val playback = state.playback
    val active = playback.isActiveFor(document.id)
    val speed = if (active) playback.speed else state.preferredSpeed
    val nextSpeed = { speedSteps.firstOrNull { it > speed + 0.01f } ?: speedSteps.first() }

    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!active) {
                    FilledTonalButton(
                        onClick = { if (state.configured) viewModel.play(document) else askToConfigure = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(MediaIcons.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("朗读本页", modifier = Modifier.padding(start = 8.dp))
                    }
                } else {
                    IconButton(onClick = viewModel::previous, enabled = playback.segmentIndex > 0) {
                        Icon(MediaIcons.SkipPrevious, contentDescription = "上一段")
                    }
                    PlayPauseButton(playback.status, onPause = viewModel::pause, onResume = viewModel::resume)
                    IconButton(onClick = viewModel::next, enabled = playback.segmentIndex < playback.segmentCount - 1) {
                        Icon(MediaIcons.SkipNext, contentDescription = "下一段")
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        Text(
                            "第 ${playback.segmentIndex + 1} / ${playback.segmentCount} 段",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        val detail = when (playback.status) {
                            ReadAloudStatus.PREPARING -> "正在生成语音…"
                            ReadAloudStatus.ERROR -> playback.error ?: "朗读出错"
                            ReadAloudStatus.PAUSED -> "已暂停"
                            else -> null
                        }
                        detail?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (playback.status == ReadAloudStatus.ERROR) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                TextButton(onClick = { viewModel.setSpeed(nextSpeed()) }) { Text(speed.speedLabel()) }
                if (active) {
                    IconButton(onClick = viewModel::stop) { Icon(MediaIcons.Stop, contentDescription = "停止朗读") }
                }
            }
        }
    }

    if (askToConfigure) {
        AlertDialog(
            onDismissRequest = { askToConfigure = false },
            title = { Text("需要配置语音服务") },
            text = { Text("朗读使用微软 Azure 语音合成。请先在设置中填写 Azure 语音资源的密钥和区域，免费层每月可合成 50 万字符。") },
            confirmButton = {
                TextButton(onClick = {
                    askToConfigure = false
                    onOpenSettings()
                }) { Text("去设置") }
            },
            dismissButton = { TextButton(onClick = { askToConfigure = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun PlayPauseButton(status: ReadAloudStatus, onPause: () -> Unit, onResume: () -> Unit) {
    when (status) {
        ReadAloudStatus.PREPARING -> IconButton(onClick = onPause) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        }
        ReadAloudStatus.PLAYING -> IconButton(onClick = onPause) {
            Icon(MediaIcons.Pause, contentDescription = "暂停")
        }
        else -> IconButton(onClick = onResume) {
            Icon(Icons.Filled.PlayArrow, contentDescription = if (status == ReadAloudStatus.ERROR) "重试" else "继续")
        }
    }
}
