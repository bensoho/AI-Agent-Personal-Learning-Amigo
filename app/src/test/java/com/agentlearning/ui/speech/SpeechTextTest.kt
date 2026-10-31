package com.agentlearning.ui.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechTextTest {
    @Test
    fun removesMarkdownWithoutDroppingReadableText() {
        val markdown = """
            # LangChain
            **Agent** 会调用 [工具](https://example.com)。
            ```kotlin
            println("不会朗读")
            ```
            - `Prompt` 很重要
        """.trimIndent()

        assertEquals(
            "LangChain\nAgent 会调用 工具。\nPrompt 很重要",
            textForSpeech(markdown),
        )
    }

    @Test
    fun splitsLongTextAtSentenceBoundaries() {
        val text = "甲".repeat(300) + "。" + "乙".repeat(300) + "。" + "丙".repeat(100)

        val chunks = speechChunks(text, maxLength = 320)

        assertEquals(3, chunks.size)
        assertTrue(chunks[0].endsWith("。"))
        assertTrue(chunks[1].endsWith("。"))
        assertTrue(chunks.all { it.length <= 320 })
    }

    @Test
    fun normalizesSymbolsThatTheChineseVoiceCannotPronounce() {
        assertEquals(
            "Tokens 和 Context Window，附带说明。",
            textForSpeech("Tokens & Context Window（附带说明）"),
        )
    }

    @Test
    fun keepsSentencesApartSoPlaybackCanStartEarly() {
        val chunks = speechChunks("第一句。第二句！第三句", maxLength = 4)

        assertEquals(listOf("第一句。", "第二句！", "第三句"), chunks)
    }

    @Test
    fun startsWithTheShortHeadingLine() {
        val text = textForSpeech(
            "Tokens & Context Window\nHow LLMs read text and what limits a single request.",
        )

        assertEquals(
            listOf(
                "Tokens 和 Context Window",
                "How LLMs read text and what limits a single request.",
            ),
            speechChunks(text),
        )
    }

    @Test
    fun splitsOverlongSentencesAtClauses() {
        val sentence = "甲".repeat(20) + "，" + "乙".repeat(20) + "。"

        val chunks = speechChunks(sentence, maxLength = 30)

        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= 30 })
        assertEquals(sentence, chunks.joinToString(""))
    }
}
