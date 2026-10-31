package com.agentlearning.feature.knowledge.plan

data class StudyPlan(
    val title: String,
    val guides: List<PlanDocument>,
    val months: List<PlanMonth>,
) {
    fun document(id: String): PlanDocument? =
        guides.firstOrNull { it.id == id }
            ?: months.firstNotNullOfOrNull { month ->
                month.weeks.firstOrNull { it.document.id == id }?.document
            }
}

/** Keeps guides and weeks whose title or content contains [query]; months without matches are dropped. */
fun StudyPlan.filter(query: String): StudyPlan {
            if (query.isBlank()) return this
            val q = query.trim()
            return copy(
                guides = guides.filter { it.matches(q) },
                months = months.mapNotNull { month ->
                    val monthHit = month.title.contains(q, ignoreCase = true)
                    val weeks = if (monthHit) month.weeks else month.weeks.filter { it.document.matches(q) }
                    if (weeks.isEmpty()) null else month.copy(weeks = weeks)
                },
            )
}

data class PlanMonth(
    val number: Int,
    val title: String,
    val goal: String?,
    val weeks: List<PlanWeek>,
)

data class PlanWeek(
    val number: Int,
    val monthNumber: Int,
    val title: String,
    val document: PlanDocument,
)

data class PlanDocument(
    val id: String,
    val title: String,
    val subtitle: String,
    val markdown: String,
) {
    fun matches(query: String): Boolean =
        title.contains(query, ignoreCase = true) || markdown.contains(query, ignoreCase = true)
}

/**
 * Splits the learning-plan markdown into months and weeks. A week's document merges its
 * schedule (chapter 三), lecture notes (chapter 四) and code walkthrough (chapter 五).
 */
object StudyPlanParser {

    const val OVERVIEW_ID = "overview"
    const val METHOD_ID = "method"
    const val SETUP_ID = "setup"
    const val RESOURCES_ID = "resources"

    fun weekId(number: Int) = "week-$number"

    private enum class Part { OVERVIEW, SCHEDULE, LECTURE, CODE, APPENDIX }

    private val h1 = Regex("""^#\s+(.+)$""")
    private val monthHeading = Regex("""^##\s+第\s*(\d+)\s*个月\s*[：:]\s*(.+)$""")
    private val scheduleWeekHeading = Regex("""^###\s+第\s*(\d+)\s*周\s*[：:]\s*(.+)$""")
    private val chapterWeekHeading = Regex("""^##\s+第\s*(\d+)\s*周\s*[：:]\s*(.+)$""")
    private val monthGoal = Regex("""^\*\*月度目标[：:]\*\*\s*(.+)$""")
    private val rule = Regex("""-{3,}""")

    fun parse(markdown: String): StudyPlan {
        var title = "学习计划"
        var titleSeen = false
        var part = Part.OVERVIEW
        var inFence = false
        var currentMonth = 0

        val overview = StringBuilder()
        val method = StringBuilder()
        val setup = StringBuilder()
        val resources = StringBuilder()
        val monthTitles = linkedMapOf<Int, String>()
        val monthIntros = mutableMapOf<Int, StringBuilder>()
        val weekTitles = mutableMapOf<Int, String>()
        val weekMonth = mutableMapOf<Int, Int>()
        val schedules = mutableMapOf<Int, StringBuilder>()
        val lectures = mutableMapOf<Int, StringBuilder>()
        val codes = mutableMapOf<Int, StringBuilder>()
        var target = overview

        for (raw in markdown.lines()) {
            val line = raw.trimEnd()
            if (line.trimStart().startsWith("```")) {
                inFence = !inFence
                target.appendLine(line)
                continue
            }
            if (inFence) {
                target.appendLine(line)
                continue
            }

            val h1Match = h1.matchEntire(line)
            if (h1Match != null) {
                val heading = h1Match.groupValues[1].trim()
                part = when {
                    heading.contains("详细课程安排") -> Part.SCHEDULE
                    heading.contains("逐周学习讲义") -> Part.LECTURE
                    heading.contains("逐周代码") -> Part.CODE
                    part == Part.OVERVIEW -> Part.OVERVIEW
                    else -> Part.APPENDIX
                }
                target = when (part) {
                    Part.OVERVIEW, Part.SCHEDULE -> overview
                    Part.LECTURE -> method
                    Part.CODE -> setup
                    Part.APPENDIX -> resources
                }
                if (part == Part.OVERVIEW && !titleSeen) {
                    title = heading
                    titleSeen = true
                } else if (part == Part.APPENDIX) {
                    resources.appendLine("# ${stripChapterNumber(heading)}")
                }
                continue
            }

            when (part) {
                Part.SCHEDULE -> {
                    val month = monthHeading.matchEntire(line)
                    val week = scheduleWeekHeading.matchEntire(line)
                    when {
                        month != null -> {
                            currentMonth = month.groupValues[1].toInt()
                            monthTitles[currentMonth] = cleanTitle(month.groupValues[2])
                            target = monthIntros.getOrPut(currentMonth) { StringBuilder() }
                        }
                        week != null -> {
                            val number = week.groupValues[1].toInt()
                            weekTitles[number] = cleanTitle(week.groupValues[2])
                            weekMonth[number] = currentMonth
                            target = schedules.getOrPut(number) { StringBuilder() }
                        }
                        else -> target.appendLine(line)
                    }
                }
                Part.LECTURE, Part.CODE -> {
                    val week = chapterWeekHeading.matchEntire(line)
                    if (week != null) {
                        val number = week.groupValues[1].toInt()
                        val heading = cleanTitle(week.groupValues[2])
                        weekTitles.putIfAbsent(number, heading)
                        target = (if (part == Part.LECTURE) lectures else codes).getOrPut(number) { StringBuilder() }
                        if (part == Part.CODE) target.appendLine("## $heading")
                    } else {
                        target.appendLine(line)
                    }
                }
                Part.OVERVIEW, Part.APPENDIX -> target.appendLine(line)
            }
        }

        val months = monthTitles.map { (number, monthTitle) ->
            val goal = monthIntros[number]?.toString()?.split(Regex("""\n\s*\n"""))
                ?.map { paragraph -> paragraph.lines().joinToString(" ") { it.trim() }.trim() }
                ?.firstNotNullOfOrNull { monthGoal.matchEntire(it)?.groupValues?.get(1)?.trim() }
            val weeks = weekMonth.filterValues { it == number }.keys.sorted().map { week ->
                buildWeek(week, number, weekTitles.getValue(week), schedules[week], lectures[week], codes[week])
            }
            PlanMonth(number, monthTitle, goal, weeks)
        }

        val guides = listOfNotNull(
            guide(OVERVIEW_ID, "计划总览", "学习目标、技术体系与 16 周总览", overview),
            guide(METHOD_ID, "学习方法", "每周节奏、答案使用方式与完整性检查", method),
            guide(SETUP_ID, "代码环境准备", "依赖安装与示例运行方式", setup),
            guide(RESOURCES_ID, "资源与建议", "推荐学习资源、开发环境与学习建议", resources),
        )
        return StudyPlan(title, guides, months)
    }

    private fun buildWeek(
        number: Int,
        month: Int,
        title: String,
        schedule: StringBuilder?,
        lecture: StringBuilder?,
        code: StringBuilder?,
    ): PlanWeek {
        val markdown = buildString {
            appendSection("课程安排", schedule?.let { normalizeInlineLists(it.toString()) })
            appendSection("学习讲义", lecture)
            appendSection("代码跟做示例", code)
        }.trim()
        val document = PlanDocument(
            id = weekId(number),
            title = "第${number}周：$title",
            subtitle = "第${month}个月 · Week %02d".format(number),
            markdown = markdown,
        )
        return PlanWeek(number, month, title, document)
    }

    private fun StringBuilder.appendSection(heading: String, body: CharSequence?) {
        val content = body?.let { trimRules(it.toString()) }.orEmpty()
        if (content.isEmpty()) return
        appendLine("# $heading")
        appendLine()
        appendLine(content)
        appendLine()
    }

    private fun guide(id: String, title: String, subtitle: String, body: CharSequence): PlanDocument? {
        val markdown = trimRules(body.toString())
        return if (markdown.isEmpty()) null else PlanDocument(id, title, subtitle, markdown)
    }

    // Drops blank lines and horizontal rules left at the edges of a section after splitting.
    private fun trimRules(text: String): String {
        val lines = text.lines()
        fun isEdge(line: String) = line.isBlank() || line.trim().matches(rule)
        val start = lines.indexOfFirst { !isEdge(it) }
        if (start < 0) return ""
        val end = lines.indexOfLast { !isEdge(it) }
        return lines.subList(start, end + 1).joinToString("\n")
    }

    private val inlineListLead = Regex("""^(\*\*[^*]+\*\*|[^\s*#|>`][^：:]*[：:])\s+(-|1\.)\s+(.*)$""")
    private val blockStart = Regex("""^\s*(#|[-*+]\s|\d+[.)]\s|\||>|```)""")

    /**
     * The schedule chapter was converted by Pandoc with lists flattened into hard-wrapped
     * paragraphs, e.g. "**练习题** 1. ... 2. ..." or "功能要求： - ... - ...". Rebuild real lists.
     */
    internal fun normalizeInlineLists(text: String): String {
        val out = mutableListOf<String>()
        val lines = text.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank() || blockStart.containsMatchIn(line) || rule.matches(line.trim())) {
                out += line
                i++
                continue
            }
            val paragraph = mutableListOf(line.trim())
            i++
            while (i < lines.size && lines[i].isNotBlank()) paragraph += lines[i++].trim()
            val joined = paragraph.joinToString(" ")
            val match = inlineListLead.matchEntire(joined)
            if (match == null) {
                out += paragraph
                continue
            }
            val (lead, marker, rest) = match.destructured
            out += lead
            out += ""
            if (marker == "-") {
                rest.split(Regex("""\s+-\s+""")).forEach { out += "- ${it.trim()}" }
            } else {
                rest.split(Regex("""\s+(?=\d+\.\s)""")).forEachIndexed { index, item ->
                    out += if (index == 0) "1. ${item.trim()}" else item.trim()
                }
            }
            out += ""
        }
        return out.joinToString("\n")
    }

    private fun stripChapterNumber(heading: String): String =
        heading.replace(Regex("""^[一二三四五六七八九十]+、\s*"""), "")

    private fun cleanTitle(title: String): String = title.trim().replace(rule, "——")
}
