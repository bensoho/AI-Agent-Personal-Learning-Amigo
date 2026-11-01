package com.agentlearning.ui.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class SpeechPlaybackState(
    val isPreparing: Boolean = true,
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val notice: String? = null,
)

/**
 * Process-wide speech playback backed by the platform's default TTS engine,
 * so switching the system voice engine (vivo AIService, Google TTS, ...) is
 * picked up automatically. If the default engine changes while the app is
 * running, the next playback re-binds to it.
 *
 * Utterances are synthesized to short audio files and played with MediaPlayer,
 * because the TTS API itself cannot pause and resume where it left off.
 */
class SpeechSynthesizer private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(SpeechPlaybackState())
    private val runCounter = AtomicLong()
    private val synthesisWaiters = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    val state: StateFlow<SpeechPlaybackState> = _state.asStateFlow()

    @Volatile
    private var systemTts: TextToSpeech? = null

    @Volatile
    private var systemReady = false

    @Volatile
    private var boundEngine: String? = null

    private var pendingStartText: String? = null

    @Volatile
    private var activePlayer: MediaPlayer? = null

    private var sessionJob: Job? = null

    init {
        cleanupAudioFiles()
        initializeSystemTts()
    }

    /** Short press: start, pause, or resume depending on the current state. */
    fun toggle(text: String) {
        val current = _state.value
        if (!current.isReady) return
        when {
            !current.isSpeaking -> start(text)
            current.isPaused -> resume()
            else -> pause()
        }
    }

    /** Long press: stop and reset so the next start begins from the top. */
    fun stop() {
        pendingStartText = null
        cancelSession()
        showNotice("已停止朗读。")
    }

    fun consumeNotice(notice: String) {
        _state.update {
            if (it.notice == notice) it.copy(notice = null) else it
        }
    }

    private fun start(text: String) {
        val speechText = text.trim()
        val engine = systemTts
        if (!systemReady || engine == null || speechText.isEmpty()) return

        val currentDefault = currentDefaultEngine(engine)
        if (boundEngine != null && currentDefault != null && currentDefault != boundEngine) {
            Log.i(TAG, "Default TTS engine changed: $boundEngine -> $currentDefault")
            pendingStartText = text
            rebindSystemTts()
            return
        }

        cancelSession()
        val chunks = speechChunks(speechText)
        if (chunks.isEmpty()) return

        val token = runCounter.get()
        _state.update { it.copy(isSpeaking = true, isPaused = false) }

        sessionJob = scope.launch {
            var prefetch: Deferred<File?>? = null
            try {
                for ((index, chunk) in chunks.withIndex()) {
                    if (token != runCounter.get()) break
                    val file = if (index == 0) {
                        synthesize(engine, chunk, token, index)
                    } else {
                        prefetch?.await()
                    }
                    prefetch = null
                    if (file == null) {
                        Log.e(TAG, "Synthesis failed for chunk $index")
                        if (token == runCounter.get()) {
                            showNotice("语音合成失败，请稍后重试。")
                        }
                        break
                    }

                    if (index + 1 < chunks.size) {
                        prefetch = async {
                            synthesize(engine, chunks[index + 1], token, index + 1)
                        }
                    }

                    playFile(file, token)
                    file.delete()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Speech playback failed", error)
                if (token == runCounter.get()) {
                    showNotice("语音朗读失败，请稍后重试。")
                }
            } finally {
                prefetch?.cancel()
                cleanupAudioFiles()
                if (token == runCounter.get()) {
                    activePlayer?.let { runCatching { it.release() } }
                    activePlayer = null
                    _state.update { it.copy(isSpeaking = false, isPaused = false) }
                }
            }
        }
    }

    private suspend fun synthesize(
        engine: TextToSpeech,
        chunk: String,
        token: Long,
        index: Int,
    ): File? {
        val utteranceId = "speech-$token-$index"
        val file = synthFile(token, index)
        file.parentFile?.mkdirs()
        file.delete()

        val waiter = CompletableDeferred<Boolean>()
        synthesisWaiters[utteranceId] = waiter
        return try {
            val result = engine.synthesizeToFile(chunk, null, file, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                Log.e(TAG, "synthesizeToFile rejected chunk $index: $result")
                file.delete()
                null
            } else {
                val finished = waiter.await()
                if (finished && file.length() > 0) {
                    file
                } else {
                    file.delete()
                    null
                }
            }
        } finally {
            synthesisWaiters.remove(utteranceId)
        }
    }

    private suspend fun playFile(file: File, token: Long) {
        if (token != runCounter.get()) return

        val player = MediaPlayer()
        val completed = suspendCancellableCoroutine { continuation ->
            try {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                player.setDataSource(file.absolutePath)
                player.setOnCompletionListener {
                    if (continuation.isActive) continuation.resume(true)
                }
                player.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error $what/$extra")
                    if (continuation.isActive) continuation.resume(false)
                    true
                }
                player.prepare()
                activePlayer = player
                if (!_state.value.isPaused) player.start()
                continuation.invokeOnCancellation {
                    activePlayer = null
                    runCatching { player.release() }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Cannot play synthesized audio", error)
                runCatching { player.release() }
                if (continuation.isActive) continuation.resume(false)
            }
        }

        activePlayer = null
        runCatching { player.release() }
        if (!completed) Log.w(TAG, "Playback interrupted for ${file.name}")
    }

    private fun pause() {
        _state.update { it.copy(isPaused = true) }
        activePlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.pause()
            }
        }
    }

    private fun resume() {
        _state.update { it.copy(isPaused = false) }
        activePlayer?.let { player ->
            runCatching { player.start() }
        }
    }

    private fun cancelSession() {
        runCounter.incrementAndGet()
        sessionJob?.cancel()
        sessionJob = null
        activePlayer?.let { runCatching { it.release() } }
        activePlayer = null
        systemTts?.stop()
        synthesisWaiters.values.forEach { it.complete(false) }
        synthesisWaiters.clear()
        cleanupAudioFiles()
        _state.update { it.copy(isSpeaking = false, isPaused = false) }
    }

    private fun synthFile(token: Long, index: Int): File =
        File(appContext.cacheDir, "speech-$token-$index.wav")

    private fun cleanupAudioFiles() {
        runCatching {
            appContext.cacheDir.listFiles()
                ?.filter { it.name.startsWith("speech-") && it.name.endsWith(".wav") }
                ?.forEach { it.delete() }
        }
    }

    private fun initializeSystemTts() {
        createSystemTts(null, ::configureSystemTts)
    }

    private fun rebindSystemTts() {
        cancelSession()
        systemReady = false
        systemTts?.let { runCatching { it.shutdown() } }
        systemTts = null
        boundEngine = null
        _state.update { it.copy(isPreparing = true, isReady = false) }
        initializeSystemTts()
    }

    private fun currentDefaultEngine(engine: TextToSpeech): String? =
        runCatching { engine.defaultEngine }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun createSystemTts(
        engineName: String?,
        onInitialized: (TextToSpeech, Int) -> Unit,
    ) {
        val holder = arrayOfNulls<TextToSpeech>(1)
        var pendingStatus: Int? = null
        val listener = TextToSpeech.OnInitListener { status ->
            val created = holder[0]
            if (created == null) {
                pendingStatus = status
            } else {
                onInitialized(created, status)
            }
        }
        val tts = if (engineName != null) {
            TextToSpeech(appContext, listener, engineName)
        } else {
            TextToSpeech(appContext, listener)
        }
        holder[0] = tts
        pendingStatus?.let { onInitialized(tts, it) }
    }

    private fun configureSystemTts(tts: TextToSpeech, status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            systemTts = null
            systemReady = false
            _state.update {
                it.copy(
                    isPreparing = false,
                    isReady = false,
                    notice = "语音朗读初始化失败。",
                )
            }
            return
        }

        val languageStatus = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
        if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
            languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            tts.shutdown()
            systemTts = null
            systemReady = false
            _state.update {
                it.copy(
                    isPreparing = false,
                    isReady = false,
                    notice = "系统语音不支持中文，请安装中文语音数据。",
                )
            }
            return
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                synthesisWaiters[utteranceId]?.complete(true)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                failUtterance(utteranceId)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                failUtterance(utteranceId)
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                synthesisWaiters[utteranceId]?.complete(false)
            }
        })

        systemTts = tts
        systemReady = true
        boundEngine = currentDefaultEngine(tts)
        _state.update { it.copy(isPreparing = false, isReady = true) }
        Log.i(
            TAG,
            "System TTS ready: engine=$boundEngine " +
                "voice=${runCatching { tts.voice?.name }.getOrNull()}",
        )

        pendingStartText?.let { pending ->
            pendingStartText = null
            start(pending)
        }
    }

    private fun failUtterance(utteranceId: String?) {
        synthesisWaiters[utteranceId]?.complete(false)
    }

    private fun showNotice(message: String) {
        _state.update { it.copy(notice = message) }
    }

    companion object {
        private const val TAG = "SpeechSynthesizer"

        @Volatile
        private var instance: SpeechSynthesizer? = null

        fun getInstance(context: Context): SpeechSynthesizer =
            instance ?: synchronized(this) {
                instance ?: SpeechSynthesizer(context).also { instance = it }
            }
    }
}
