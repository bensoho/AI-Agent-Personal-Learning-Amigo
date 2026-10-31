package com.agentlearning.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.agentlearning.feature.speech.AzureTtsClient
import com.agentlearning.feature.speech.AzureVoice
import com.agentlearning.feature.speech.AzureVoices
import com.agentlearning.feature.speech.ReadAloudController
import com.agentlearning.feature.speech.ReadingDocument
import com.agentlearning.feature.speech.SpeechCache
import com.agentlearning.feature.speech.SpeechException
import com.agentlearning.feature.speech.SpeechPreferences
import com.agentlearning.feature.speech.SpeechSegment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class SpeechSettingsUiState(
    val region: String = SpeechPreferences.DEFAULT_REGION,
    val hasKey: Boolean = false,
    val voice: AzureVoice = AzureVoices.default,
    val usedCharacters: Long = 0,
    val cacheBytes: Long = 0,
    val busy: Boolean = false,
    val message: String? = null,
    /** Increments after credentials are saved, so the screen can clear the key field. */
    val savedCount: Int = 0,
)

@HiltViewModel
class SpeechSettingsViewModel @Inject constructor(
    private val preferences: SpeechPreferences,
    private val client: AzureTtsClient,
    private val cache: SpeechCache,
    private val controller: ReadAloudController,
) : ViewModel() {

    private data class Transient(
        val cacheBytes: Long = 0,
        val busy: Boolean = false,
        val message: String? = null,
        val savedCount: Int = 0,
    )

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<SpeechSettingsUiState> =
        combine(preferences.config, preferences.usage, transient) { config, usage, t ->
            SpeechSettingsUiState(
                region = config.region,
                hasKey = config.key.isNotBlank(),
                voice = config.voice,
                usedCharacters = usage.characters,
                cacheBytes = t.cacheBytes,
                busy = t.busy,
                message = t.message,
                savedCount = t.savedCount,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpeechSettingsUiState())

    init {
        refreshCacheSize()
    }

    /** Verifies and saves the credentials; a blank [key] keeps the stored one, e.g. when only the region changes. */
    fun saveCredentials(region: String, key: String) {
        viewModelScope.launch {
            transient.update { it.copy(busy = true) }
            val effectiveKey = key.trim().ifBlank { preferences.current().key }
            val normalizedRegion = region.trim().lowercase()
            val message = try {
                client.verify(normalizedRegion, effectiveKey)
                preferences.setCredentials(normalizedRegion, effectiveKey)
                transient.update { it.copy(savedCount = it.savedCount + 1) }
                "已保存，Azure 连接正常"
            } catch (e: SpeechException) {
                e.message
            }
            transient.update { it.copy(busy = false, message = message) }
        }
    }

    fun removeKey() {
        viewModelScope.launch {
            controller.stop()
            preferences.setCredentials(preferences.current().region, "")
        }
    }

    fun selectVoice(voice: AzureVoice) {
        viewModelScope.launch { preferences.setVoice(voice) }
    }

    fun preview() {
        val voice = uiState.value.voice
        controller.play(
            ReadingDocument(
                id = "preview:${voice.id}",
                title = "试听：${voice.label}",
                segments = listOf(SpeechSegment(-1, PREVIEW_TEXT)),
            )
        )
    }

    fun clearCache() {
        viewModelScope.launch {
            controller.stop()
            cache.clear()
            refreshCacheSize()
        }
    }

    fun dismissMessage() = transient.update { it.copy(message = null) }

    private fun refreshCacheSize() {
        viewModelScope.launch {
            val size = cache.sizeBytes()
            transient.update { it.copy(cacheBytes = size) }
        }
    }

    private companion object {
        const val PREVIEW_TEXT = "你好，我是你的学习助手。今天我们一起学习 LangGraph 中的 StateGraph，以及如何用 tool calling 让 Agent 调用工具。"
    }
}

@Composable
internal fun SpeechSettingsCard(viewModel: SpeechSettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var region by rememberSaveable(state.region) { mutableStateOf(state.region) }
    // The stored key is never shown; the field is only for entering a new one.
    var key by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.savedCount) { key = "" }

    SettingsCard("朗读（Azure 语音）") {
        Text(
            "在 Azure 门户创建“语音服务”资源（定价层选 Free F0，每月免费 50 万字符），" +
                "然后在“密钥和终结点”页面复制密钥和位置/区域。密钥加密保存在本机。",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("区域，例如 eastasia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text(if (state.hasKey) "密钥（已保存，留空则不修改）" else "密钥") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.saveCredentials(region, key) },
                enabled = !state.busy && region.isNotBlank() && (key.isNotBlank() || state.hasKey),
            ) { Text(if (state.busy) "正在验证…" else "保存并测试") }
            if (state.hasKey) {
                TextButton(onClick = viewModel::removeKey) { Text("删除密钥", color = MaterialTheme.colorScheme.error) }
            }
        }

        Text("音色", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
        AzureVoices.all.forEach { voice ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = voice == state.voice,
                        onClick = { viewModel.selectVoice(voice) },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = voice == state.voice, onClick = null)
                Text(voice.label, modifier = Modifier.padding(start = 8.dp))
            }
        }
        OutlinedButton(onClick = viewModel::preview, enabled = state.hasKey, modifier = Modifier.fillMaxWidth()) {
            Text("试听当前音色")
        }

        val quota = SpeechPreferences.FREE_MONTHLY_CHARACTERS
        Text(
            "本月已合成 %,d / %,d 字符（免费额度）".format(Locale.CHINA, state.usedCharacters, quota),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
        LinearProgressIndicator(
            progress = { (state.usedCharacters.toFloat() / quota).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "只统计本机用量，实际以 Azure 门户为准。已朗读过的段落会缓存，重复收听不再计费。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("语音缓存 %.1f MB".format(Locale.CHINA, state.cacheBytes / 1_048_576.0))
            TextButton(onClick = viewModel::clearCache, enabled = state.cacheBytes > 0) { Text("清除缓存") }
        }
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMessage,
            text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::dismissMessage) { Text("好的") } },
        )
    }
}
