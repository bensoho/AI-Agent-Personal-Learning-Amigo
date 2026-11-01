package com.agentlearning.ui.speech

internal const val SPEECH_CHUNK_LENGTH = 60

private val SENTENCE_END = charArrayOf('。', '！', '？', '!', '?', '；', ';', '…', '\n')
private val CLAUSE_BREAK = charArrayOf('，', ',', '、', '：', ':', ' ')

/**
 * Drops Chinese/English punctuation, but keeps mathematical operators and
 * currency signs because those should be read aloud.
 */
private val UNSPEAKABLE_SYMBOLS = Regex(
    """[^\p{L}\p{N}\s\p{Sc}。！？!?；;…，,、：:.%％‰+=<>＋＝＜＞≤≥≠≈±×÷√∑∞−∓≡∝∫∆∇]+""",
)

internal fun textForSpeech(markdown: String): String =
    markdown
        .replace(Regex("""(?s)```.*?```"""), " ")
        .replace(Regex("""\[([^\]]+)]\([^)]+\)"""), "$1")
        .replace(Regex("""\*\*(.*?)\*\*|__(.*?)__"""), "$1$2")
        .replace(Regex("""(?m)^[ \t]{0,3}#{1,6}[ \t]+"""), "")
        .replace(Regex("""(?m)^[ \t]*>[ \t]?"""), "")
        .replace(Regex("""(?m)^[ \t]*(?:[-+*]|\d+[.)])[ \t]+"""), "")
        .replace(Regex("""(?<=\d)\s*\*\s*(?=\d)"""), "×")
        .replace(Regex("""(?<=\d)\s*/\s*(?=\d)"""), "÷")
        .replace(Regex("""(?<=\d)\s*-\s*(?=\d)"""), "−")
        .replace(Regex("""[*_`]"""), " ")
        .replace("|", " ")
        .replace("&", " 和 ")
        .replace("（", "，")
        .replace("(", "，")
        .replace("）", "。")
        .replace(")", "。")
        .replace("•", "，")
        .replace("·", "，")
        .replace("[", " ")
        .replace("]", " ")
        .replace("【", " ")
        .replace("】", " ")
        .replace(Regex("""['’‘]"""), "")
        .replace(UNSPEAKABLE_SYMBOLS, " ")
        .replace(Regex("""[ \t]+([。！？!?；;…，,、：:.])"""), "$1")
        .replace(Regex("""[^\S\n]+"""), " ")
        .replace(Regex(""" *\n *"""), "\n")
        .replace(Regex("""\n{2,}"""), "\n")
        .trim()

/**
 * Splits text into short, sentence-sized chunks. The neural voice is slow on
 * low-end devices, so feeding it small chunks lets playback start long before
 * the whole note has been synthesized.
 */
internal fun speechChunks(
    text: String,
    maxLength: Int = SPEECH_CHUNK_LENGTH,
): List<String> {
    require(maxLength > 0) { "maxLength must be positive" }
    val normalized = text.trim()
    if (normalized.isEmpty()) return emptyList()

    val chunks = mutableListOf<String>()
    val current = StringBuilder()
    for (sentence in splitIntoSentences(normalized)) {
        for (piece in splitOversized(sentence, maxLength)) {
            if (current.isNotEmpty() && current.length + piece.length > maxLength) {
                chunks += current.toString().trim()
                current.clear()
            }
            current.append(piece)
        }
    }
    if (current.isNotBlank()) chunks += current.toString().trim()
    return chunks
}

private fun splitIntoSentences(text: String): List<String> {
    val sentences = mutableListOf<String>()
    val builder = StringBuilder()
    for ((index, char) in text.withIndex()) {
        builder.append(char)
        val endsSentence = char in SENTENCE_END ||
            (char == '.' && (index == text.lastIndex || text[index + 1].isWhitespace()))
        if (endsSentence) {
            sentences += builder.toString().trim()
            builder.clear()
        }
    }
    if (builder.isNotBlank()) sentences += builder.toString().trim()
    return sentences.filter { it.isNotEmpty() }
}

private fun splitOversized(sentence: String, maxLength: Int): List<String> {
    if (sentence.length <= maxLength) return listOf(sentence)

    val parts = mutableListOf<String>()
    var remaining = sentence
    while (remaining.length > maxLength) {
        var end = remaining.lastIndexOfAny(CLAUSE_BREAK, maxLength)
        if (end < maxLength / 2) {
            end = maxLength
        } else {
            end += 1
        }
        parts += remaining.substring(0, end).trim()
        remaining = remaining.substring(end).trim()
    }
    if (remaining.isNotEmpty()) parts += remaining
    return parts.filter { it.isNotEmpty() }
}
