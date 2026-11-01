package com.agentlearning.feature

import com.agentlearning.domain.model.Course
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.Topic
import com.agentlearning.domain.model.TopicProgress
import com.agentlearning.domain.model.WeekOutline
import com.agentlearning.feature.knowledge.KnowledgeScope
import com.agentlearning.feature.knowledge.filterKnowledge
import com.agentlearning.feature.knowledge.searchExcerpt
import com.agentlearning.feature.knowledge.searchMatchRanges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeSearchTest {
    private fun topic(id: String, month: Int, week: Int, title: String, notes: String) = TopicProgress(
        topic = Topic(id, "month-$month", week, "Week $week", title, "Description", notes),
        total = 3, answered = 0, mastered = 0, reviewing = 0, learning = 0,
    )

    private val outline = listOf(
        CourseOutline(
            Course("month-1", "LangChain", 1),
            listOf(
                WeekOutline(1, "LLM basics", listOf(topic("tokens", 1, 1, "Tokens", "Context window"))),
                WeekOutline(2, "Prompts", listOf(topic("templates", 1, 2, "Templates", "Few-shot examples"))),
            ),
        ),
        CourseOutline(
            Course("month-2", "LangGraph", 2),
            listOf(WeekOutline(5, "Graphs", listOf(topic("state", 2, 5, "State", "Graph context")))),
        ),
    )

    @Test
    fun blankQuery_preservesFullCatalog() {
        assertEquals(outline, filterKnowledge(outline, "  ", KnowledgeScope()))
    }

    @Test
    fun monthAndWeekFilters_intersectWithSearch() {
        val result = filterKnowledge(outline, "CONTEXT", KnowledgeScope(monthNumber = 1, weekNumber = 1))
        assertEquals(listOf(1), result.map { it.course.monthNumber })
        assertEquals(listOf(1), result.single().weeks.map { it.weekNumber })
        assertEquals("tokens", result.single().weeks.single().topics.single().topic.id)
        assertTrue(filterKnowledge(outline, "Context", KnowledgeScope(1, 2)).isEmpty())
        assertTrue(filterKnowledge(outline, "", KnowledgeScope(1, 5)).isEmpty())
    }

    @Test
    fun weekFilter_aloneWorksAcrossMonths() {
        assertEquals(listOf(2), filterKnowledge(outline, "", KnowledgeScope(weekNumber = 5)).map { it.course.monthNumber })
    }

    @Test
    fun search_matchesCourseAndWeekHeadings() {
        assertEquals(listOf(2), filterKnowledge(outline, "langgraph", KnowledgeScope()).map { it.course.monthNumber })
        assertEquals(listOf(2), filterKnowledge(outline, "prompts", KnowledgeScope()).single().weeks.map { it.weekNumber })
    }

    @Test
    fun matchRanges_areLiteralCaseInsensitiveAndNonOverlapping() {
        assertEquals(listOf(0..2, 4..6), searchMatchRanges("API api", " api "))
        assertEquals(listOf(1..3), searchMatchRanges("a[x]b", "[x]"))
        assertEquals(listOf(0..1, 2..3), searchMatchRanges("aaaa", "aa"))
        assertTrue(searchMatchRanges("text", " ").isEmpty())
    }

    @Test
    fun excerpt_centersDistantMatchAndBoundsLength() {
        val excerpt = searchExcerpt("prefix ".repeat(80) + "Context window " + "suffix ".repeat(80), "context")!!
        assertTrue(excerpt.contains("Context"))
        assertTrue(excerpt.startsWith("..."))
        assertTrue(excerpt.endsWith("..."))
        assertTrue(excerpt.length <= 126)
    }

    @Test
    fun excerpt_handlesWhitespaceLongQueriesAndNoMatch() {
        assertEquals("One two three", searchExcerpt("One\n  two\tthree", "two"))
        val query = "x".repeat(150)
        assertEquals(query, searchExcerpt(query, query))
        assertNull(searchExcerpt("text", "absent"))
        assertNull(searchExcerpt("text", " "))
    }

    @Test
    fun excerpt_removesMarkdownDecorationButPreservesCode() {
        assertEquals(
            "Context Window Use RAG and token limits.",
            searchExcerpt("## Context Window\n\n- Use **RAG** and `token` limits.", "RAG"),
        )
        assertEquals(
            "token_count = 2 ** 3",
            searchExcerpt("```python\ntoken_count = 2 ** 3\n```", "token_count"),
        )
    }
}
