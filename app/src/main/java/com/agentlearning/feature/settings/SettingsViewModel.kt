package com.agentlearning.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.BackupPreview
import com.agentlearning.domain.model.ThemeMode
import com.agentlearning.domain.repository.BackupException
import com.agentlearning.domain.repository.BackupRepository
import com.agentlearning.domain.repository.ParsedBackup
import com.agentlearning.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val dailyGoal: Int = 10,
    val bankVersion: Int = 0,
    val fontScale: Float = 1f,
    val soundEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val message: String? = null,
    val pendingRestore: BackupPreview? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val backup: BackupRepository,
) : ViewModel() {

    private val transient = MutableStateFlow(Ephemeral())
    private var pending: ParsedBackup? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            settings.dailyGoal,
            settings.bankContentVersion,
            settings.fontScale,
            settings.soundEnabled,
            settings.themeMode,
        ) { goal, version, fontScale, sound, theme ->
            SettingsUiState(goal, version, fontScale, sound, theme)
        },
        transient,
    ) { prefs, t ->
        prefs.copy(message = t.message, pendingRestore = t.preview)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(value) }
    }

    fun setDailyGoal(value: Int) {
        viewModelScope.launch { settings.setDailyGoal(value) }
    }

    fun setFontScale(value: Float) {
        // Slider steps produce values like 0.90000004; round so the default compares equal to 1.0.
        viewModelScope.launch { settings.setFontScale(Math.round(value * 100) / 100f) }
    }

    fun setSoundEnabled(value: Boolean) {
        viewModelScope.launch { settings.setSoundEnabled(value) }
    }

    fun export(uri: Uri) = launchWithMessage {
        val json = backup.exportJson()
        withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openOutputStream(uri, "wt") ?: error("无法写入所选文件")
            stream.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
        }
        "已导出学习数据"
    }

    fun prepareRestore(uri: Uri) = launchWithMessage {
        val text = withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openInputStream(uri) ?: error("无法读取所选文件")
            stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        val parsed = backup.parse(text)
        pending = parsed
        transient.update { it.copy(preview = parsed.preview) }
        null
    }

    fun confirmRestore() {
        val parsed = pending ?: return
        clearPending()
        launchWithMessage {
            backup.restore(parsed)
            "已从备份恢复"
        }
    }

    fun cancelRestore() = clearPending()

    fun resetData() = launchWithMessage {
        backup.resetUserData()
        "本地学习数据已重置"
    }

    fun dismissMessage() = transient.update { it.copy(message = null) }

    private fun clearPending() {
        pending = null
        transient.update { it.copy(preview = null) }
    }

    private fun launchWithMessage(block: suspend () -> String?) {
        viewModelScope.launch {
            val message = try {
                block()
            } catch (e: BackupException) {
                e.message
            } catch (e: Exception) {
                "操作失败：${e.message ?: e.javaClass.simpleName}"
            }
            if (message != null) transient.update { it.copy(message = message) }
        }
    }

    private data class Ephemeral(val message: String? = null, val preview: BackupPreview? = null)
}
