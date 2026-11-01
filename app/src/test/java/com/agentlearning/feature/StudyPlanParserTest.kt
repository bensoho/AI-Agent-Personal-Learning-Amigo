package com.agentlearning.feature

import com.agentlearning.feature.knowledge.plan.MarkdownParser
import com.agentlearning.feature.knowledge.plan.MdBlock
import com.agentlearning.feature.knowledge.plan.MdSpan
import com.agentlearning.feature.knowledge.plan.StudyPlanParser
import com.agentlearning.feature.knowledge.plan.filter
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
