package com.agentlearning.feature

import com.agentlearning.feature.knowledge.plan.MdBlock
import com.agentlearning.feature.speech.SpeechSegment
import com.agentlearning.feature.speech.SpeechText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechTextTest {

    @Test
    fun `code tables and dividers are skipped and block indexes are kept`() {
        val blocks = listOf(
            MdBlock.Heading(2, "第一周"),
            MdBlock.Code("python", "print('hi')"),
            MdBlock.Paragraph("学习 **LangGraph** 的 `StateGraph`。"),
            MdBlock.Table(listOf("a"), listOf(listOf("b"))),
            MdBlock.Divider,
            MdBlock.ListItem("•", "安装依赖", 0),
        )

        val segments = SpeechText.fromMarkdown(blocks)

        assertEquals(
            listOf(
                SpeechSegment(0, "第一周。"),
                SpeechSegment(2, "学习 LangGraph 的 StateGraph。"),
                SpeechSegment(5, "安装依赖。"),
            ),
            segments,
        )
    }

    @Test
    fun `links keep their label and bare urls are dropped`() {
        assertEquals(
            "见 官方文档 和",
            SpeechText.clean("见 [官方文档](https://docs.langchain.com) 和 https://example.com"),
        )
    }

    @Test
    fun `long paragraphs are split at sentence ends within the length limit`() {
        val sentence = "这是一个用于测试分段的句子，内容需要足够长才能触发拆分逻辑。"
        val text = sentence.repeat(20)

        val segments = SpeechText.fromBlocks(listOf(3 to text))

        assertTrue(segments.size > 1)
        assertTrue(segments.all { it.blockIndex == 3 && it.text.length <= 280 })
        assertEquals(text, segments.joinToString("") { it.text })
    }

    @Test
    fun `blocks without text produce no segments`() {
        assertEquals(emptyList<SpeechSegment>(), SpeechText.fromBlocks(listOf(-1 to null, 0 to "  ")))
    }
}
