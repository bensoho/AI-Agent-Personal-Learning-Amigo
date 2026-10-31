package com.agentlearning.feature.speech

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

class SpeechException(message: String) : Exception(message)

/** Minimal client for the Azure Speech text-to-speech REST API. */
@Singleton
class AzureTtsClient @Inject constructor() {

    /** Returns MP3 audio for [text]. */
    suspend fun synthesize(config: SpeechConfig, text: String): ByteArray = withContext(Dispatchers.IO) {
        val connection = open(config.region, "cognitiveservices/v1", config.key).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/ssml+xml")
            setRequestProperty("X-Microsoft-OutputFormat", OUTPUT_FORMAT)
        }
        try {
            connection.outputStream.use { it.write(ssml(config.voice.id, text).toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw SpeechException(describe(code))
            connection.inputStream.use { it.readBytes() }
        } catch (e: IOException) {
            throw SpeechException("网络连接失败，请检查网络后重试")
        } finally {
            connection.disconnect()
        }
    }

    /** Checks the key and region without consuming any characters. */
    suspend fun verify(region: String, key: String) = withContext(Dispatchers.IO) {
        val connection = open(region, "cognitiveservices/voices/list", key)
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw SpeechException(describe(code))
        } catch (e: IOException) {
            throw SpeechException("无法连接 Azure，请检查网络和区域是否正确")
        } finally {
            connection.disconnect()
        }
    }

    private fun open(region: String, path: String, key: String): HttpURLConnection {
        if (!REGION_PATTERN.matches(region)) throw SpeechException("区域格式不正确，例如 eastasia")
        val url = URL("https://$region.tts.speech.microsoft.com/$path")
        return (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            setRequestProperty("Ocp-Apim-Subscription-Key", key)
            setRequestProperty("User-Agent", USER_AGENT)
        }
    }

    private fun describe(code: Int): String = when (code) {
        400 -> "请求无效（400），所选音色可能在该区域不可用"
        401, 403 -> "Azure Key 无效，或与所填区域不匹配"
        429 -> "请求过于频繁或已超出额度，请稍后再试"
        in 500..599 -> "Azure 服务暂时不可用（$code），请稍后再试"
        else -> "语音合成失败（HTTP $code）"
    }

    private fun ssml(voice: String, text: String): String =
        "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='zh-CN'>" +
            "<voice name='$voice'>${escapeXml(text)}</voice></speak>"

    private fun escapeXml(text: String): String = buildString(text.length) {
        for (c in text) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(c)
            }
        }
    }

    private companion object {
        const val OUTPUT_FORMAT = "audio-24khz-48kbitrate-mono-mp3"
        const val USER_AGENT = "AgentLearning-Android"
        const val TIMEOUT_MILLIS = 20_000
        val REGION_PATTERN = Regex("^[a-z0-9]+$")
    }
}
