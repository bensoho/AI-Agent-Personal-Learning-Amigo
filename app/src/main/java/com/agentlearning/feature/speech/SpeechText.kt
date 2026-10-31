package com.agentlearning.feature.speech

import com.agentlearning.feature.knowledge.plan.MdBlock

/** One synthesis request; [blockIndex] is the rendered block to highlight, or -1 for none. */
data class SpeechSegment(val blockIndex: Int, val text: String)

/** Everything read aloud for one page. [id] lets a page tell whether it is the one being read. */
data class ReadingDocument(val id: String, val title: String, val segments: List<SpeechSegment>)

/** Turns rendered content into speakable segments: code, tables and links are skipped, markup removed. */
object SpeechText {

    private const val MAX_SEGMENT_CHARS = 280
    private val markdownLink = Regex("""\[([^\]]+)]\([^)]*\)""")
    private val url = Regex("""https?://\S+""")
    private val whitespace = Regex("""\s+""")
    private val sentenceEnd = Regex("""(?<=[。！？；!?;])|(?<=\.)(?=\s)""")
    private const val CLOSING_PUNCTUATION = "。！？；：，、.!?;:,"

    fun fromMarkdown(blocks: List<MdBlock>): List<SpeechSegment> =
        blocks.flatMapIndexed { index, block ->
            val text = when (block) {
                is MdBlock.Heading -> withPause(block.text)
                is MdBlock.Paragraph -> block.text
                is MdBlock.ListItem -> withPause(block.text)
                is MdBlock.Quote -> block.text
                is MdBlock.Code, is MdBlock.Preformatted, is MdBlock.Table, MdBlock.Divider -> null
            }
            segments(index, text)
        }

    /** Builds segments from pre-indexed text blocks; pass a null text to skip a block. */
    fun fromBlocks(blocks: List<Pair<Int, String?>>): List<SpeechSegment> =
        blocks.flatMap { (index, text) -> segments(index, text?.let { withPause(it) }) }

    fun clean(text: String): String = text
        .replace(markdownLink) { it.groupValues[1] }
        .replace(url, "")
        .replace("**", "")
        .replace("`", "")
        .replace(whitespace, " ")
        .trim()

    private fun segments(index: Int, text: String?): List<SpeechSegment> =
        text?.let { split(clean(it)) }.orEmpty().map { SpeechSegment(index, it) }

    // Headings and list items often lack final punctuation; adding one gives a natural pause.
    private fun withPause(text: String): String {
        val trimmed = text.trimEnd()
        return if (trimmed.isEmpty() || trimmed.last() in CLOSING_PUNCTUATION) trimmed else "$trimmed。"
    }

    /** Splits at sentence boundaries so each request stays short and the first audio arrives quickly. */
    private fun split(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        if (text.length <= MAX_SEGMENT_CHARS) return listOf(text)
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        for (sentence in text.split(sentenceEnd).filter { it.isNotBlank() }) {
            if (current.isNotEmpty() && current.length + sentence.length > MAX_SEGMENT_CHARS) {
                parts += current.toString().trim()
                current.clear()
            }
            if (sentence.length > MAX_SEGMENT_CHARS) {
                sentence.chunked(MAX_SEGMENT_CHARS).forEach { parts += it.trim() }
            } else {
                current.append(sentence)
            }
        }
        if (current.isNotBlank()) parts += current.toString().trim()
        return parts.filter { it.isNotBlank() }
    }
}
