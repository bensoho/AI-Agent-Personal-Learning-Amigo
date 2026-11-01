package com.agentlearning.feature.quiz

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

/** Plays short synthesized tones for right and wrong answers, so no audio assets are needed. */
@Singleton
class SoundPlayer @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val correctTone = synth(listOf(Note(523.25, 90), Note(659.25, 90), Note(783.99, 180)), overtone = 0.0)
    private val wrongTone = synth(listOf(Note(233.08, 140), Note(174.61, 260)), overtone = 0.35)

    fun play(correct: Boolean) {
        val pcm = if (correct) correctTone else wrongTone
        scope.launch {
            val track = try {
                buildTrack(pcm)
            } catch (e: Exception) {
                return@launch
            }
            try {
                track.play()
                delay(pcm.size * 1000L / SAMPLE_RATE + 120)
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
            .also { it.write(pcm, 0, pcm.size) }

    private class Note(val frequency: Double, val millis: Int)

    private fun synth(notes: List<Note>, overtone: Double): ShortArray {
        val out = ArrayList<Short>()
        for (note in notes) {
            val samples = SAMPLE_RATE * note.millis / 1000
            val fade = SAMPLE_RATE * FADE_MILLIS / 1000
            for (i in 0 until samples) {
                val t = i.toDouble() / SAMPLE_RATE
                val wave = sin(2 * PI * note.frequency * t) + overtone * sin(4 * PI * note.frequency * t)
                // Short fades remove the click at note boundaries.
                val envelope = minOf(1.0, i.toDouble() / fade, (samples - i).toDouble() / fade)
                out += (wave / (1 + overtone) * envelope * VOLUME * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out.toShortArray()
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val FADE_MILLIS = 8
        const val VOLUME = 0.45
    }
}
