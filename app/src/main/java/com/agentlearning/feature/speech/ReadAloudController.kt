package com.agentlearning.feature.speech

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class ReadAloudStatus { IDLE, PREPARING, PLAYING, PAUSED, ERROR }

data class ReadAloudState(
    val documentId: String? = null,
    val title: String = "",
    val segmentIndex: Int = 0,
    val segmentCount: Int = 0,
    val blockIndex: Int = -1,
    val status: ReadAloudStatus = ReadAloudStatus.IDLE,
    val error: String? = null,
    val speed: Float = SpeechPreferences.DEFAULT_SPEED,
) {
    fun isActiveFor(id: String): Boolean = documentId == id && status != ReadAloudStatus.IDLE
}

/**
 * Reads a [ReadingDocument] aloud. Segments are synthesized a few steps ahead of playback and appended
 * to one ExoPlayer playlist, so playlist index = segment index - [startSegment]. The player is shared
 * with [ReadAloudService], which exposes it to the notification and lock screen.
 * Must be used from the main thread.
 */
@Singleton
class ReadAloudController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cache: SpeechCache,
    private val preferences: SpeechPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(ReadAloudState())
    val state: StateFlow<ReadAloudState> = _state.asStateFlow()

    private var player: ExoPlayer? = null
    private var document: ReadingDocument? = null
    private var startSegment = 0
    private var feedJob: Job? = null
    private var feedComplete = false
    private val playingSegment = MutableStateFlow(0)

    // A synthesis failure is only surfaced once the already-queued audio has finished playing.
    private var pendingError: String? = null
    private var failedSegment = -1

    fun obtainPlayer(): ExoPlayer = player ?: ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        // Keeps Wi-Fi and CPU awake with the screen off, since upcoming segments are still downloading.
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()
        .also {
            it.addListener(listener)
            player = it
        }

    fun play(document: ReadingDocument, fromSegment: Int = 0) {
        if (document.segments.isEmpty()) return
        scope.launch {
            val config = preferences.current()
            if (!config.isConfigured) {
                _state.value = ReadAloudState(
                    documentId = document.id,
                    title = document.title,
                    status = ReadAloudStatus.ERROR,
                    error = NOT_CONFIGURED,
                )
                return@launch
            }
            this@ReadAloudController.document = document
            startFrom(fromSegment.coerceIn(0, document.segments.lastIndex), config)
        }
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        val current = _state.value
        if (current.status == ReadAloudStatus.ERROR) {
            val from = if (failedSegment >= 0) failedSegment else current.segmentIndex
            document?.let { play(it, from) }
        } else {
            player?.play()
        }
    }

    fun next() = jump(_state.value.segmentIndex + 1)

    fun previous() = jump(_state.value.segmentIndex - 1)

    fun setSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
        scope.launch { preferences.setSpeed(speed) }
    }

    fun stop() {
        feedJob?.cancel()
        feedJob = null
        feedComplete = false
        pendingError = null
        failedSegment = -1
        document = null
        player?.run {
            stop()
            clearMediaItems()
        }
        _state.value = ReadAloudState(speed = _state.value.speed)
        context.stopService(Intent(context, ReadAloudService::class.java))
    }

    private fun jump(segment: Int) {
        val doc = document ?: return
        if (segment !in doc.segments.indices) return
        val p = player ?: return
        val playlistIndex = segment - startSegment
        if (playlistIndex in 0 until p.mediaItemCount) {
            // Seeking back into queued audio: drop a displayed error; a pending one returns once that audio ends.
            _state.update { if (it.status == ReadAloudStatus.ERROR) it.copy(status = ReadAloudStatus.PREPARING, error = null) else it }
            p.seekTo(playlistIndex, 0)
            // After a playback error the player is idle and must be prepared again.
            if (p.playbackState == Player.STATE_IDLE) p.prepare()
            p.play()
        } else {
            play(doc, segment)
        }
    }

    private fun startFrom(segment: Int, config: SpeechConfig) {
        val doc = document ?: return
        // Reset first: clearing the old playlist can report STATE_ENDED, which must not finish the new one.
        feedJob?.cancel()
        feedComplete = false
        pendingError = null
        failedSegment = -1
        startSegment = segment
        playingSegment.value = segment
        val p = obtainPlayer()
        p.stop()
        p.clearMediaItems()
        p.setPlaybackSpeed(config.speed)
        p.playWhenReady = true
        _state.value = ReadAloudState(
            documentId = doc.id,
            title = doc.title,
            segmentIndex = segment,
            segmentCount = doc.segments.size,
            blockIndex = doc.segments[segment].blockIndex,
            status = ReadAloudStatus.PREPARING,
            speed = config.speed,
        )
        context.startService(Intent(context, ReadAloudService::class.java))
        feedJob = scope.launch { feed(doc, config, segment) }
    }

    private suspend fun feed(doc: ReadingDocument, config: SpeechConfig, from: Int) {
        for (index in from until doc.segments.size) {
            // Only synthesize a little ahead of playback, so skipping or stopping wastes no quota.
            playingSegment.first { current -> shouldSynthesize(doc, current, index) }
            val file = try {
                cache.obtain(config, doc.segments[index].text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail(e.message ?: "语音合成失败", index)
                return
            }
            append(doc, index, file)
        }
        feedComplete = true
        if (player?.playbackState == Player.STATE_ENDED) finish()
    }

    /**
     * Stays at least [MIN_PREFETCH_SEGMENTS] and about [PREFETCH_CHARS] characters (roughly a minute of
     * speech) ahead. Counting characters keeps short headings and list items from draining the playlist
     * while a request is in flight; an empty playlist would drop the foreground service and wake lock.
     * The limit also keeps skipped or stopped content from consuming much quota.
     */
    private fun shouldSynthesize(doc: ReadingDocument, current: Int, index: Int): Boolean {
        if (index - current <= MIN_PREFETCH_SEGMENTS) return true
        var queuedChars = 0
        for (i in current + 1 until index) queuedChars += doc.segments[i].text.length
        return queuedChars < PREFETCH_CHARS
    }

    private fun append(doc: ReadingDocument, index: Int, file: File) {
        val p = obtainPlayer()
        val item = MediaItem.Builder()
            .setMediaId(index.toString())
            .setUri(Uri.fromFile(file))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(doc.title)
                    .setArtist("朗读 ${index + 1}/${doc.segments.size}")
                    .build()
            )
            .build()
        val wasWaiting = p.playbackState == Player.STATE_ENDED
        p.addMediaItem(item)
        when {
            p.mediaItemCount == 1 -> p.prepare()
            // Playback caught up with synthesis; continue with the segment that just arrived.
            wasWaiting -> p.seekTo(p.mediaItemCount - 1, 0)
        }
    }

    /** Records a failure; it is shown once the audio queued before [segment] has finished playing. */
    private fun fail(message: String, segment: Int) {
        pendingError = message
        failedSegment = segment
        val p = player
        if (p == null || p.mediaItemCount == 0 || p.playbackState == Player.STATE_ENDED || p.playbackState == Player.STATE_IDLE) {
            showError()
        }
    }

    private fun showError() {
        val doc = document
        _state.update {
            val segment = if (failedSegment >= 0) failedSegment else it.segmentIndex
            it.copy(
                status = ReadAloudStatus.ERROR,
                error = pendingError,
                segmentIndex = segment,
                blockIndex = doc?.segments?.getOrNull(segment)?.blockIndex ?: it.blockIndex,
            )
        }
    }

    private fun finish() = stop()

    private fun refreshStatus() {
        val p = player ?: return
        _state.update { current ->
            if (current.documentId == null || current.status == ReadAloudStatus.ERROR) return@update current
            val status = when {
                !p.playWhenReady -> ReadAloudStatus.PAUSED
                p.isPlaying -> ReadAloudStatus.PLAYING
                else -> ReadAloudStatus.PREPARING
            }
            current.copy(status = status)
        }
    }

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val segment = mediaItem?.mediaId?.toIntOrNull() ?: return
            val doc = document ?: return
            playingSegment.value = segment
            _state.update {
                it.copy(segmentIndex = segment, blockIndex = doc.segments.getOrNull(segment)?.blockIndex ?: -1)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                when {
                    feedComplete -> {
                        finish()
                        return
                    }
                    pendingError != null -> {
                        showError()
                        return
                    }
                }
            }
            refreshStatus()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) = refreshStatus()

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = refreshStatus()

        override fun onPlayerError(error: PlaybackException) {
            pendingError = "播放失败：${error.errorCodeName}"
            failedSegment = _state.value.segmentIndex
            showError()
        }
    }

    companion object {
        const val NOT_CONFIGURED = "尚未配置 Azure 语音服务"
        private const val MIN_PREFETCH_SEGMENTS = 2
        private const val PREFETCH_CHARS = 300
    }
}
