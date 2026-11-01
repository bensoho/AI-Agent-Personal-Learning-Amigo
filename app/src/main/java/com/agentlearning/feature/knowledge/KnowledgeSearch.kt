package com.agentlearning.feature.knowledge

import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.feature.knowledge.plan.MarkdownParser
import com.agentlearning.feature.knowledge.plan.MdBlock

data class KnowledgeScope(val monthNumber: Int? = null, val weekNumber: Int? = null) {
    val isActive: Boolean get() = monthNumber != null || weekNumber != null
}

fun filterKnowledge(outline: List<CourseOutline>, query: String, scope: KnowledgeScope): List<CourseOutline> {
    val q = query.trim()
    return outline.mapNotNull { course ->
        if (scope.monthNumber != null && course.course.monthNumber != scope.monthNumber) return@mapNotNull null
        val weeks = course.weeks.mapNotNull weekLoop@{ week ->
            if (scope.weekNumber != null && week.weekNumber != scope.weekNumber) return@weekLoop null
            val headingMatches = course.course.title.contains(q, ignoreCase = true) || week.title.contains(q, ignoreCase = true)
            val topics = week.topics.filter { progress ->
                val topic = progress.topic
                q.isEmpty() || headingMatches || listOfNotNull(topic.title, topic.description, topic.notes)
                    .any { it.contains(q, ignoreCase = true) }
            }
            if (topics.isEmpty()) null else week.copy(topics = topics)
        }
        if (weeks.isEmpty()) null else course.copy(weeks = weeks)
    }
}

fun searchMatchRanges(text: String, query: String): List<IntRange> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val ranges = mutableListOf<IntRange>()
    var start = text.indexOf(q, ignoreCase = true)
    while (start >= 0) {
        ranges += start until start + q.length
        start = text.indexOf(q, start + q.length, ignoreCase = true)
    }
    return ranges
}

fun searchExcerpt(text: String, query: String, maxLength: Int = 120): String? {
    require(maxLength > 0)
    val q = query.trim()
    if (q.isEmpty()) return null
    val normalized = MarkdownParser.parse(text).joinToString(" ") { block ->
        val content = when (block) {
            is MdBlock.Heading -> block.text
            is MdBlock.Paragraph -> block.text
            is MdBlock.ListItem -> block.text
            is MdBlock.Quote -> block.text
            is MdBlock.Code -> block.code
            is MdBlock.Preformatted -> block.text
            is MdBlock.Table -> (block.header + block.rows.flatten()).joinToString(" ")
            MdBlock.Divider -> ""
        }
        if (block is MdBlock.Code || block is MdBlock.Preformatted) content
        else MarkdownParser.spans(content).joinToString("") { it.text }
    }.replace(Regex("\\s+"), " ").trim()
    val match = normalized.indexOf(q, ignoreCase = true)
    if (match < 0) return null
    val length = maxOf(maxLength, q.length)
    val start = (match - length / 4).coerceAtLeast(0)
    val end = (start + length).coerceAtMost(normalized.length)
    return (if (start > 0) "..." else "") + normalized.substring(start, end) + if (end < normalized.length) "..." else ""
}
