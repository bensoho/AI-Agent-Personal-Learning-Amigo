package com.agentlearning.feature.knowledge.plan

sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class ListItem(val marker: String, val text: String, val indent: Int) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
    data class Preformatted(val text: String) : MdBlock
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock
    data object Divider : MdBlock
}

sealed interface MdSpan {
    val text: String

    data class Plain(override val text: String) : MdSpan
    data class Bold(override val text: String) : MdSpan
    data class InlineCode(override val text: String) : MdSpan
}

/** Minimal Markdown parser covering the syntax used by the bundled learning plan (Pandoc flavour). */
object MarkdownParser {

    private val heading = Regex("""^(#{1,6})\s+(.+)$""")
    private val bullet = Regex("""^(\s*)[-*+]\s+(.*)$""")
    private val ordered = Regex("""^(\s*)(\d+)[.)]\s+(.*)$""")
    private val rule = Regex("""^\s*(-{3,}|\*{3,}|_{3,})\s*$""")
    private val pandocTableRule = Regex("""^\s+-{3,}[\s-]*$""")
    private val tableSeparator = Regex("""^\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)*\|?\s*$""")
    private val inline = Regex("""\*\*(.+?)\*\*|`([^`]+)`""")

    fun parse(markdown: String): List<MdBlock> {
        val blocks = mutableListOf<MdBlock>()
        val lines = markdown.lines()
        val paragraph = StringBuilder()
        var i = 0

        fun flushParagraph() {
            if (paragraph.isNotBlank()) blocks += MdBlock.Paragraph(paragraph.toString().trim())
            paragraph.clear()
        }

        fun appendWrapped(builder: StringBuilder, text: String) {
            when {
                builder.isEmpty() -> Unit
                builder.endsWith("\n") -> Unit
                else -> builder.append(' ')
            }
            // A trailing backslash is a Pandoc hard line break.
            if (text.endsWith("\\")) builder.append(text.dropLast(1).trimEnd()).append('\n') else builder.append(text.trim())
        }

        while (i < lines.size) {
            val line = lines[i].trimEnd()
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("```") -> {
                    flushParagraph()
                    val language = trimmed.removePrefix("```").trim()
                    val code = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) code += lines[i++]
                    blocks += MdBlock.Code(language, code.joinToString("\n").trimEnd())
                }
                line.isBlank() -> flushParagraph()
                pandocTableRule.matches(line) -> {
                    flushParagraph()
                    val table = parsePandocSimpleTable(lines, i)
                    if (table != null) {
                        blocks += table.block
                        i = table.nextIndex
                        continue
                    }
                    blocks += MdBlock.Divider
                }
                rule.matches(line) -> {
                    flushParagraph()
                    blocks += MdBlock.Divider
                }
                heading.matches(line) -> {
                    flushParagraph()
                    val match = heading.matchEntire(line)!!
                    blocks += MdBlock.Heading(match.groupValues[1].length, match.groupValues[2].trim())
                }
                trimmed.startsWith("|") -> {
                    flushParagraph()
                    val rows = mutableListOf<List<String>>()
                    while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                        val row = lines[i].trim()
                        if (!tableSeparator.matches(row)) rows += splitRow(row)
                        i++
                    }
                    if (rows.isNotEmpty()) blocks += MdBlock.Table(rows.first(), rows.drop(1))
                    continue
                }
                trimmed.startsWith(">") -> {
                    flushParagraph()
                    val quote = StringBuilder()
                    while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                        appendWrapped(quote, lines[i].trimStart().removePrefix(">").trim())
                        i++
                    }
                    blocks += MdBlock.Quote(quote.toString().trim())
                    continue
                }
                bullet.matches(line) || ordered.matches(line) -> {
                    flushParagraph()
                    val bulletMatch = bullet.matchEntire(line)
                    val indentText: String
                    val marker: String
                    val first: String
                    if (bulletMatch != null) {
                        indentText = bulletMatch.groupValues[1]
                        marker = "•"
                        first = bulletMatch.groupValues[2]
                    } else {
                        val match = ordered.matchEntire(line)!!
                        indentText = match.groupValues[1]
                        marker = "${match.groupValues[2]}."
                        first = match.groupValues[3]
                    }
                    val item = StringBuilder()
                    appendWrapped(item, first)
                    i++
                    // Continuation lines: non-blank lines that do not start a new block.
                    while (i < lines.size) {
                        val next = lines[i].trimEnd()
                        if (next.isBlank() || startsBlock(next)) break
                        appendWrapped(item, next)
                        i++
                    }
                    blocks += MdBlock.ListItem(marker, item.toString().trim(), indentText.length / 2)
                    continue
                }
                else -> appendWrapped(paragraph, line)
            }
            i++
        }
        flushParagraph()
        return blocks
    }

    private data class ParsedTable(val block: MdBlock.Table, val nextIndex: Int)

    /**
     * Pandoc simple tables: a rule, the header, a rule whose dash runs mark the column boundaries,
     * the body, then a closing rule. Slicing uses display width so CJK cells stay in their columns.
     */
    private fun parsePandocSimpleTable(lines: List<String>, start: Int): ParsedTable? {
        var columnRule = -1
        for (j in start + 1 until minOf(lines.size, start + 12)) {
            if (pandocTableRule.matches(lines[j])) {
                columnRule = j
                break
            }
        }
        if (columnRule == -1) return null

        val ranges = Regex("-{3,}").findAll(lines[columnRule])
            .map { it.range.first to it.range.last + 1 }
            .toList()
        if (ranges.size < 2) return null

        val header = mutableListOf<String>()
        for (h in start + 1 until columnRule) {
            val text = lines[h]
            if (text.isBlank()) continue
            val cells = ranges.map { (from, to) -> sliceColumns(text, from, to) }
            if (header.isEmpty()) {
                header.addAll(cells)
            } else {
                cells.forEachIndexed { index, cell ->
                    if (cell.isNotBlank()) header[index] = (header[index] + " " + cell).trim()
                }
            }
        }
        if (header.all { it.isBlank() }) return null

        var end = columnRule + 1
        while (end < lines.size && !pandocTableRule.matches(lines[end])) end++

        val rows = mutableListOf<MutableList<String>>()
        var startsNewRow = true
        for (k in columnRule + 1 until end) {
            val text = lines[k]
            if (text.isBlank()) {
                startsNewRow = true
                continue
            }
            val cells = ranges.map { (from, to) -> sliceColumns(text, from, to) }
            if (startsNewRow || rows.isEmpty()) {
                rows += cells.toMutableList()
                startsNewRow = false
            } else {
                val last = rows.last()
                cells.forEachIndexed { index, cell ->
                    if (cell.isNotBlank()) last[index] = (last[index] + " " + cell).trim()
                }
            }
        }
        val body = rows.filter { row -> row.any { it.isNotBlank() } }
        if (body.isEmpty()) return null
        return ParsedTable(MdBlock.Table(header, body), if (end < lines.size) end + 1 else end)
    }

    private fun sliceColumns(line: String, start: Int, end: Int): String {
        val text = StringBuilder()
        var column = 0
        var index = 0
        while (index < line.length && column < end) {
            val codePoint = line.codePointAt(index)
            val width = displayWidth(codePoint)
            val next = column + width
            if (column < end && next > start) text.appendCodePoint(codePoint)
            column = next
            index += Character.charCount(codePoint)
        }
        return text.toString().trim()
    }

    private fun displayWidth(codePoint: Int): Int = when {
        codePoint == '\t'.code -> 4
        codePoint < 0x20 -> 0
        codePoint in 0x1100..0x115F -> 2
        codePoint in 0x2E80..0xA4CF -> 2
        codePoint in 0xAC00..0xD7A3 -> 2
        codePoint in 0xF900..0xFAFF -> 2
        codePoint in 0xFE10..0xFE19 -> 2
        codePoint in 0xFE30..0xFE6F -> 2
        codePoint in 0xFF00..0xFF60 -> 2
        codePoint in 0xFFE0..0xFFE6 -> 2
        codePoint in 0x1F300..0x1FAFF -> 2
        codePoint in 0x20000..0x3FFFD -> 2
        else -> 1
    }

    fun spans(text: String): List<MdSpan> {        val spans = mutableListOf<MdSpan>()
        var last = 0
        for (match in inline.findAll(text)) {
            if (match.range.first > last) spans += MdSpan.Plain(text.substring(last, match.range.first))
            val bold = match.groups[1]
            spans += if (bold != null) MdSpan.Bold(bold.value) else MdSpan.InlineCode(match.groupValues[2])
            last = match.range.last + 1
        }
        if (last < text.length) spans += MdSpan.Plain(text.substring(last))
        return spans
    }

    private fun startsBlock(line: String): Boolean {
        val trimmed = line.trimStart()
        return trimmed.startsWith("```") || trimmed.startsWith("|") || trimmed.startsWith(">") ||
            heading.matches(line) || rule.matches(line) || pandocTableRule.matches(line) ||
            bullet.matches(line) || ordered.matches(line)
    }

    private fun splitRow(row: String): List<String> =
        row.trim().removePrefix("|").removeSuffix("|").split("|").map { it.trim() }
}
