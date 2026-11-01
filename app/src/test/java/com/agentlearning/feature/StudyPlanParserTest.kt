package com.agentlearning.feature

import com.agentlearning.feature.knowledge.plan.MarkdownParser
import com.agentlearning.feature.knowledge.plan.MdBlock
import com.agentlearning.feature.knowledge.plan.MdSpan
import com.agentlearning.feature.knowledge.plan.StudyPlanParser
import com.agentlearning.feature.knowledge.plan.filter
import com.agentlearning.feature.knowledge.KnowledgeScope
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyPlanParserTest {

    private val sample = """
        # 示例学习计划

        ## 一、目标

        -   掌握 LangChain

        # 三、详细课程安排

        ## 第1个月：LangChain 基础

        **月度目标：** 能够独立使用
        LangChain 开发应用。

        ### 第1周：LLM 基础

        **理论学习** - Token - Temperature

        **练习题** 1. Token 是什么？ 2. Temperature
        有什么影响？

        ### 第2周：核心组件

        **本周交付物：** 一个助手。

        ## 第2个月：LangGraph

        ### 第3周：Graph 基础------入门

        内容

        # 四、逐周学习讲义：理论解释

        先理解概念。

        ## 第1周：LLM 基础与开发环境

        ### 概念讲解

        讲义内容

        # 五、逐周代码跟做示例

        依赖准备

        ## 第1周：发起模型请求

        ```python
        # 这是代码注释，不是标题
        print("hi")
        ```

        # 六、推荐学习资源

        - 官方文档
    """.trimIndent()

    @Test
    fun parse_groupsWeeksByMonth() {
        val plan = StudyPlanParser.parse(sample)

        assertEquals("示例学习计划", plan.title)
        assertEquals(listOf(1, 2), plan.months.map { it.number })
        assertEquals(listOf(1, 2), plan.months[0].weeks.map { it.number })
        assertEquals(listOf(3), plan.months[1].weeks.map { it.number })
        assertEquals("能够独立使用 LangChain 开发应用。", plan.months[0].goal)
        assertEquals("Graph 基础——入门", plan.months[1].weeks[0].title)
    }

    @Test
    fun parse_mergesScheduleLectureAndCodeIntoWeekDocument() {
        val week = StudyPlanParser.parse(sample).document(StudyPlanParser.weekId(1))
        assertNotNull(week)
        val markdown = week!!.markdown
        assertTrue(markdown.contains("# 课程安排"))
        assertTrue(markdown.contains("# 学习讲义"))
        assertTrue(markdown.contains("讲义内容"))
        assertTrue(markdown.contains("# 代码跟做示例"))
        assertTrue(markdown.contains("# 这是代码注释，不是标题"))
        assertEquals("第1个月 · Week 01", week.subtitle)
    }

    @Test
    fun parse_rebuildsFlattenedLists() {
        val markdown = StudyPlanParser.parse(sample).document("week-1")!!.markdown
        assertTrue(markdown.contains("- Token\n- Temperature"))
        assertTrue(markdown.contains("1. Token 是什么？\n2. Temperature 有什么影响？"))
    }

    @Test
    fun parse_collectsGuides() {
        val plan = StudyPlanParser.parse(sample)
        val ids = plan.guides.map { it.id }
        assertEquals(
            listOf(StudyPlanParser.OVERVIEW_ID, StudyPlanParser.METHOD_ID, StudyPlanParser.SETUP_ID, StudyPlanParser.RESOURCES_ID),
            ids,
        )
        assertTrue(plan.document(StudyPlanParser.RESOURCES_ID)!!.markdown.contains("推荐学习资源"))
    }

    @Test
    fun filter_keepsOnlyMatchingWeeks() {
        val filtered = StudyPlanParser.parse(sample).filter("讲义内容")
        assertEquals(listOf(1), filtered.months.single().weeks.map { it.number })
    }

    @Test
    fun filter_combinesMonthWeekAndQueryWithoutGuides() {
        val plan = StudyPlanParser.parse(sample)
        val filtered = plan.filter("Token", KnowledgeScope(monthNumber = 1, weekNumber = 1))
        assertTrue(filtered.guides.isEmpty())
        assertEquals(listOf(1), filtered.months.map { it.number })
        assertEquals(listOf(1), filtered.months.single().weeks.map { it.number })
        assertTrue(plan.filter("Token", KnowledgeScope(monthNumber = 2)).months.isEmpty())
        assertTrue(plan.filter("", KnowledgeScope(monthNumber = 1, weekNumber = 3)).months.isEmpty())
    }

    @Test
    fun filter_monthSelectionAndWeekSelectionWorkWithBlankQuery() {
        val plan = StudyPlanParser.parse(sample)
        assertEquals(listOf(1, 2), plan.filter("", KnowledgeScope(monthNumber = 1)).months.single().weeks.map { it.number })
        assertEquals(listOf(3), plan.filter("", KnowledgeScope(weekNumber = 3)).months.single().weeks.map { it.number })
        assertEquals(plan, plan.filter(" "))
    }

    @Test
    fun filter_monthTitleSearchKeepsOnlySelectedWeek() {
        val result = StudyPlanParser.parse(sample).filter("LangChain", KnowledgeScope(weekNumber = 2))
        assertEquals(listOf(2), result.months.single().weeks.map { it.number })
    }

    @Test
    fun markdown_parsesPandocSimpleTableWithCjkColumns() {
        val markdown = listOf(
            "  -------- ------ --------------------",
            "  阶段     周次   主题",
            "  -------- ------ --------------------",
            "  第1个月  1--4   LangChain 基础",
            "                  RAG",
            "  -------- ------ --------------------",
        ).joinToString("\n")

        val table = MarkdownParser.parse(markdown).single() as MdBlock.Table

        assertEquals(listOf("阶段", "周次", "主题"), table.header)
        assertEquals(listOf("第1个月", "1--4", "LangChain 基础 RAG"), table.rows.single())
    }

    @Test
    fun bundledResources_rendersListsTablesLinksAndCodeAsSeparateBlocks() {
        val asset = File("src/main/assets/docs/study_plan.md").readText(Charsets.UTF_8)
        val source = File("../LangChain_LangGraph_LangSmith_4个月学习计划.md").readText(Charsets.UTF_8)
        assertEquals(source, asset)
        val resources = StudyPlanParser.parse(asset).document(StudyPlanParser.RESOURCES_ID)!!
        val blocks = MarkdownParser.parse(resources.markdown)

        fun section(title: String): List<MdBlock> {
            val start = blocks.indexOf(MdBlock.Heading(2, title))
            assertTrue("Missing section: $title", start >= 0)
            return blocks.drop(start + 1).takeWhile { it !is MdBlock.Heading }
        }

        assertEquals(
            listOf("State 如何变化？", "Node 为什么这样设计？", "Graph 如何决定下一步？",
                "Agent 如何判断任务是否完成？", "执行失败后如何恢复？"),
            section("1. 不要只学习 API，要理解 Agent 底层原理")
                .filterIsInstance<MdBlock.ListItem>().map { it.text },
        )
        assertEquals(
            listOf("回答是否准确。", "是否调用正确的工具。", "是否检索到相关内容。",
                "是否遵循业务规则。", "是否出现幻觉。", "是否在合理成本和时间内完成任务。"),
            section("4. 重视 LangSmith Evaluation").filterIsInstance<MdBlock.ListItem>().map { it.text },
        )
        assertEquals(
            MdBlock.Table(
                listOf("学习活动", "时间占比"),
                listOf(listOf("理论学习", "30%"), listOf("编码实战", "60%"), listOf("复盘总结", "10%")),
            ),
            section("2. 建议时间分配").filterIsInstance<MdBlock.Table>().single(),
        )
        assertEquals(
            MdBlock.Table(
                listOf("传统软件工程", "AI Agent 工程"),
                listOf(
                    listOf("Unit Testing", "Node / Tool Testing"),
                    listOf("Integration Testing", "Agent Workflow Testing"),
                    listOf("Regression Testing", "LLM Evaluation"),
                    listOf("Code Coverage", "Evaluation Dataset Coverage"),
                    listOf("CI Quality Gate", "AI Quality Gate"),
                    listOf("Application Logging", "LangSmith Tracing"),
                ),
            ),
            section("4. 重视 LangSmith Evaluation").filterIsInstance<MdBlock.Table>().single(),
        )
        assertEquals(2, blocks.filterIsInstance<MdBlock.Table>().size)
        assertEquals(4, section("5. 不要过早陷入 Multi-Agent").filterIsInstance<MdBlock.ListItem>().size)
        assertEquals(16, blocks.filterIsInstance<MdBlock.ListItem>().count { it.text.startsWith("[ ] 第") })
        val resourceLinks = blocks.filterIsInstance<MdBlock.ListItem>().filter { it.text.contains("https://") }
        assertEquals(6, resourceLinks.size)
        assertEquals(listOf("1.", "2.", "3.", "4.", "5.", "6."), resourceLinks.map { it.marker })
        assertTrue(resourceLinks.all { it.text.contains("\nhttps://") && !it.text.contains("\\") })
        assertEquals(listOf("bash", "text"), blocks.filterIsInstance<MdBlock.Code>().map { it.language })
        assertTrue(section("开发环境建议").filterIsInstance<MdBlock.Code>().single().code.contains("uv add pytest"))
        assertTrue(section("3. 从第一周开始使用 GitHub").filterIsInstance<MdBlock.Code>()
            .single().code.contains("    └── deployment/"))
        assertTrue(blocks.filterIsInstance<MdBlock.Paragraph>().none { it.text.contains(" - ") })
    }

    @Test
    fun markdown_parsesBlocksAndInlineSpans() {
        val blocks = MarkdownParser.parse(
            """
            ## 标题
            第一行
            第二行

            -   列表项
                续行
            1. 编号

            | 周次 | 主题 |
            |---|---|
            | 1 | LLM |

            > 引用
            """.trimIndent(),
        )
        assertEquals(MdBlock.Heading(2, "标题"), blocks[0])
        assertEquals(MdBlock.Paragraph("第一行 第二行"), blocks[1])
        assertEquals(MdBlock.ListItem("•", "列表项 续行", 0), blocks[2])
        assertEquals(MdBlock.ListItem("1.", "编号", 0), blocks[3])
        assertEquals(MdBlock.Table(listOf("周次", "主题"), listOf(listOf("1", "LLM"))), blocks[4])
        assertEquals(MdBlock.Quote("引用"), blocks[5])

        assertEquals(
            listOf(MdSpan.Bold("粗体"), MdSpan.Plain(" 与 "), MdSpan.InlineCode("code")),
            MarkdownParser.spans("**粗体** 与 `code`"),
        )
    }
}
