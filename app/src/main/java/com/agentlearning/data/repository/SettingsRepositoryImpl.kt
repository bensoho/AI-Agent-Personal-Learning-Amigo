package com.agentlearning.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.agentlearning.domain.model.ThemeMode
import com.agentlearning.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {

    private val store = context.settingsDataStore

    override val dailyGoal: Flow<Int> = store.data.map { it[DAILY_GOAL] ?: DEFAULT_DAILY_GOAL }

    override val bankContentVersion: Flow<Int> = store.data.map { it[BANK_VERSION] ?: 0 }

    override val fontScale: Flow<Float> = store.data.map { it[FONT_SCALE] ?: DEFAULT_FONT_SCALE }

    override val soundEnabled: Flow<Boolean> = store.data.map { it[SOUND_ENABLED] ?: true }

    override val themeMode: Flow<ThemeMode> = store.data.map { prefs ->
        prefs[THEME_MODE]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.SYSTEM
    }

    override suspend fun setDailyGoal(value: Int) {
        store.edit { it[DAILY_GOAL] = value.coerceIn(MIN_DAILY_GOAL, MAX_DAILY_GOAL) }
    }

    override suspend fun setBankContentVersion(value: Int) {
        store.edit { it[BANK_VERSION] = value }
    }

    override suspend fun setFontScale(value: Float) {
        store.edit { it[FONT_SCALE] = value.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE) }
    }

    override suspend fun setSoundEnabled(value: Boolean) {
        store.edit { it[SOUND_ENABLED] = value }
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        store.edit { it[THEME_MODE] = value.name }
    }

    companion object {
        const val DEFAULT_DAILY_GOAL = 10
        const val MIN_DAILY_GOAL = 5
        const val MAX_DAILY_GOAL = 50
        const val DEFAULT_FONT_SCALE = 1.0f
        const val MIN_FONT_SCALE = 0.8f
        const val MAX_FONT_SCALE = 1.4f
        private val DAILY_GOAL = intPreferencesKey("daily_goal")
        private val BANK_VERSION = intPreferencesKey("bank_content_version")
        private val FONT_SCALE = floatPreferencesKey("font_scale")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
