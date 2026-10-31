package com.agentlearning.feature.speech

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synthesized audio per (voice, text). Kept in app files rather than the cache dir so the system does
 * not purge it, since re-synthesizing costs Azure quota. Oldest files are evicted above [MAX_BYTES].
 */
@Singleton
class SpeechCache @Inject constructor(
    @ApplicationContext context: Context,
    private val client: AzureTtsClient,
    private val preferences: SpeechPreferences,
) {
    private val dir = File(context.filesDir, "speech-cache")

    fun cachedFile(voiceId: String, text: String): File? = fileFor(voiceId, text).takeIf { it.isFile }

    /** Returns the audio file for [text], synthesizing and caching it on a miss. */
    suspend fun obtain(config: SpeechConfig, text: String): File {
        cachedFile(config.voice.id, text)?.let { file ->
            file.setLastModified(System.currentTimeMillis())
            return file
        }
        val audio = client.synthesize(config, text)
        preferences.addUsage(text.length)
        return withContext(Dispatchers.IO) {
            dir.mkdirs()
            val target = fileFor(config.voice.id, text)
            val partial = File(dir, target.name + ".part")
            partial.writeBytes(audio)
            if (!partial.renameTo(target)) {
                partial.delete()
                throw SpeechException("无法写入语音缓存")
            }
            evictIfNeeded()
            target
        }
    }

    suspend fun sizeBytes(): Long = withContext(Dispatchers.IO) {
        dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        dir.listFiles()?.forEach { it.delete() }
        Unit
    }

    private fun evictIfNeeded() {
        val files = dir.listFiles()?.sortedBy { it.lastModified() } ?: return
        var total = files.sumOf { it.length() }
        for (file in files) {
            if (total <= MAX_BYTES) break
            total -= file.length()
            file.delete()
        }
    }

    private fun fileFor(voiceId: String, text: String): File {
        val digest = MessageDigest.getInstance("SHA-256").digest("$voiceId\n$text".toByteArray(Charsets.UTF_8))
        return File(dir, digest.joinToString("") { "%02x".format(it) } + ".mp3")
    }

    private companion object {
        const val MAX_BYTES = 300L * 1024 * 1024
    }
}
