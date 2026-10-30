package com.agentlearning.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    override suspend fun setDailyGoal(value: Int) {
        store.edit { it[DAILY_GOAL] = value.coerceIn(MIN_DAILY_GOAL, MAX_DAILY_GOAL) }
    }

    override suspend fun setBankContentVersion(value: Int) {
        store.edit { it[BANK_VERSION] = value }
    }

    companion object {
        const val DEFAULT_DAILY_GOAL = 10
        const val MIN_DAILY_GOAL = 5
        const val MAX_DAILY_GOAL = 50
        private val DAILY_GOAL = intPreferencesKey("daily_goal")
        private val BANK_VERSION = intPreferencesKey("bank_content_version")
    }
}
