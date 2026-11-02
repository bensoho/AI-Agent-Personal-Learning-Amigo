package com.agentlearning.feature.quiz

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/** Plays short synthesized tones for right and wrong answers, so no audio assets are needed. */
@Singleton
class SoundPlayer @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val correctTone = synthBell()
    private val wrongTone = synth(listOf(Note(233.08, 140), Note(174.61, 260)))

    fun play(correct: Boolean) {
        val pcm = if (correct) correctTone else wrongTone
        scope.launch {
            val track = try {
                buildTrack(pcm)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid answer sound configuration", e)
                return@launch
            } catch (e: UnsupportedOperationException) {
                Log.e(TAG, "Answer sound playback is unavailable", e)
                return@launch
            }
            try {
                check(track.write(pcm, 0, pcm.size) == pcm.size) { "Failed to write answer sound samples" }
                track.play()
                delay((pcm.size * 1000L / SAMPLE_RATE + 120).milliseconds)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to play answer sound", e)
            } finally {
                track.release()
            }
        }
    }

    private fun buildTrack(pcm: ShortArray): AudioTrack =
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

    private class Note(val frequency: Double, val millis: Int)

    private fun synthBell(): ShortArray {
        val samples = SAMPLE_RATE * 650 / 1000
        val attack = SAMPLE_RATE * 2 / 1000
        val fade = SAMPLE_RATE * FADE_MILLIS / 1000
        return ShortArray(samples) { i ->
            val t = i.toDouble() / SAMPLE_RATE
            val phase = 2 * PI * 1567.98 * t
            // Inharmonic partials decay faster than the ringing fundamental.
            val wave = sin(phase) * exp(-7 * t) +
                0.45 * sin(2.76 * phase) * exp(-13 * t) +
                0.2 * sin(5.4 * phase) * exp(-22 * t)
            val envelope = minOf(1.0, i.toDouble() / attack, (samples - 1 - i).toDouble() / fade)
            (wave / 1.65 * envelope * VOLUME * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun synth(notes: List<Note>): ShortArray {
        val out = ArrayList<Short>()
        for (note in notes) {
            val samples = SAMPLE_RATE * note.millis / 1000
            val fade = SAMPLE_RATE * FADE_MILLIS / 1000
            for (i in 0 until samples) {
                val t = i.toDouble() / SAMPLE_RATE
                val wave = sin(2 * PI * note.frequency * t) + OVERTONE * sin(4 * PI * note.frequency * t)
                // Short fades remove the click at note boundaries.
                val envelope = minOf(1.0, i.toDouble() / fade, (samples - i).toDouble() / fade)
                out += (wave / (1 + OVERTONE) * envelope * VOLUME * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out.toShortArray()
    }

    private companion object {
        const val TAG = "SoundPlayer"
        const val SAMPLE_RATE = 44_100
        const val FADE_MILLIS = 8
        const val VOLUME = 0.45
        const val OVERTONE = 0.35
    }
}
