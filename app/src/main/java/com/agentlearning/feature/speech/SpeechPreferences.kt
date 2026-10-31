package com.agentlearning.feature.speech

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.time.Clock
import java.time.YearMonth
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class AzureVoice(val id: String, val label: String)

object AzureVoices {
    val all = listOf(
        AzureVoice("zh-CN-XiaoxiaoMultilingualNeural", "晓晓 · 女声（中英混读更自然）"),
        AzureVoice("zh-CN-YunyiMultilingualNeural", "云逸 · 男声（中英混读更自然）"),
        AzureVoice("zh-CN-XiaoxiaoNeural", "晓晓 · 女声"),
        AzureVoice("zh-CN-YunxiNeural", "云希 · 男声"),
        AzureVoice("zh-CN-XiaochenNeural", "晓辰 · 女声（轻松）"),
        AzureVoice("zh-CN-YunjianNeural", "云健 · 男声（沉稳）"),
    )
    val default = all.first()

    fun byId(id: String?): AzureVoice = all.firstOrNull { it.id == id } ?: default
}

data class SpeechConfig(
    val region: String,
    val key: String,
    val voice: AzureVoice,
    val speed: Float,
) {
    val isConfigured: Boolean get() = region.isNotBlank() && key.isNotBlank()
}

data class SpeechUsage(val month: YearMonth, val characters: Long)

private val Context.speechDataStore by preferencesDataStore(name = "speech")

/** Read-aloud settings. The Azure key is encrypted with an Android Keystore key before it is stored. */
@Singleton
class SpeechPreferences @Inject constructor(
    @ApplicationContext context: Context,
    private val clock: Clock,
) {
    private val store = context.speechDataStore

    val config: Flow<SpeechConfig> = store.data.map { prefs ->
        SpeechConfig(
            region = prefs[REGION] ?: DEFAULT_REGION,
            key = prefs[ENCRYPTED_KEY]?.let(KeyCipher::decryptOrNull).orEmpty(),
            voice = AzureVoices.byId(prefs[VOICE]),
            speed = prefs[SPEED] ?: DEFAULT_SPEED,
        )
    }

    /** Characters synthesized in the current month, to compare against Azure's free monthly quota. */
    val usage: Flow<SpeechUsage> = store.data.map { prefs ->
        val current = YearMonth.now(clock)
        val stored = prefs[USAGE_MONTH]?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
        SpeechUsage(current, if (stored == current) prefs[USAGE_CHARS] ?: 0L else 0L)
    }

    suspend fun current(): SpeechConfig = config.first()

    suspend fun setCredentials(region: String, key: String) {
        store.edit {
            it[REGION] = region.trim().lowercase()
            val trimmed = key.trim()
            if (trimmed.isEmpty()) it.remove(ENCRYPTED_KEY) else it[ENCRYPTED_KEY] = KeyCipher.encrypt(trimmed)
        }
    }

    suspend fun setVoice(voice: AzureVoice) {
        store.edit { it[VOICE] = voice.id }
    }

    suspend fun setSpeed(value: Float) {
        store.edit { it[SPEED] = value.coerceIn(MIN_SPEED, MAX_SPEED) }
    }

    suspend fun addUsage(characters: Int) {
        val month = YearMonth.now(clock).toString()
        store.edit {
            val previous = if (it[USAGE_MONTH] == month) it[USAGE_CHARS] ?: 0L else 0L
            it[USAGE_MONTH] = month
            it[USAGE_CHARS] = previous + characters
        }
    }

    companion object {
        const val DEFAULT_REGION = "eastasia"
        const val DEFAULT_SPEED = 1.0f
        const val MIN_SPEED = 0.5f
        const val MAX_SPEED = 2.0f
        const val FREE_MONTHLY_CHARACTERS = 500_000L

        private val REGION = stringPreferencesKey("azure_region")
        private val ENCRYPTED_KEY = stringPreferencesKey("azure_key_encrypted")
        private val VOICE = stringPreferencesKey("voice")
        private val SPEED = floatPreferencesKey("speed")
        private val USAGE_MONTH = stringPreferencesKey("usage_month")
        private val USAGE_CHARS = longPreferencesKey("usage_characters")
    }
}

/** AES-GCM with a non-exportable key kept in the Android Keystore. */
private object KeyCipher {
    private const val ALIAS = "speech_api_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val payload = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    /** Returns null when the stored value cannot be decrypted, e.g. after the Keystore entry was lost. */
    fun decryptOrNull(encoded: String): String? = runCatching {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, payload, 0, IV_BYTES))
        String(cipher.doFinal(payload, IV_BYTES, payload.size - IV_BYTES), Charsets.UTF_8)
    }.getOrNull()

    @Synchronized
    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
