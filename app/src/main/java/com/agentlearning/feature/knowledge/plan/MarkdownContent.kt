package com.agentlearning.feature.knowledge.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.agentlearning.feature.quiz.components.CodeBlock

/** Adds parsed blocks as lazy items so long documents stay virtualized. */
internal fun LazyListScope.markdownItems(blocks: List<MdBlock>) {
    itemsIndexed(blocks, key = { index, _ -> index }) { _, block -> MarkdownBlock(block) }
}

@Composable
private fun MarkdownBlock(block: MdBlock) {
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    when (block) {
        is MdBlock.Heading -> {
            val style = when (block.level) {
                1 -> typography.titleLarge
                2 -> typography.titleMedium
                else -> typography.titleSmall
            }
            Text(
                inlineText(block.text, scheme.secondaryContainer),
                style = style,
                color = if (block.level <= 2) scheme.primary else scheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = if (block.level == 1) 12.dp else 6.dp),
            )
            if (block.level == 1) HorizontalDivider(color = scheme.outlineVariant)
        }
        is MdBlock.Paragraph -> Text(inlineText(block.text, scheme.secondaryContainer), style = typography.bodyLarge)
        is MdBlock.ListItem -> Row(
            Modifier.padding(start = (block.indent * 16).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(block.marker, color = scheme.primary, style = typography.bodyLarge)
            Text(inlineText(block.text, scheme.secondaryContainer), style = typography.bodyLarge)
        }
        is MdBlock.Quote -> Row(
            Modifier
                .fillMaxWidth()
                .background(scheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(12.dp),
        ) {
            Text(
                inlineText(block.text, scheme.secondaryContainer),
                style = typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        is MdBlock.Code -> CodeBlock(block.code)
        is MdBlock.Preformatted -> Box(
            Modifier
                .fillMaxWidth()
                .background(scheme.surfaceVariant, RoundedCornerShape(8.dp))
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
        ) {
            Text(block.text, style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace), softWrap = false)
        }
        is MdBlock.Table -> MarkdownTable(block)
        MdBlock.Divider -> HorizontalDivider(Modifier.padding(vertical = 4.dp), color = scheme.outlineVariant)
    }
}

@Composable
private fun MarkdownTable(table: MdBlock.Table) {
    val scheme = MaterialTheme.colorScheme
    val columns = maxOf(table.header.size, table.rows.maxOfOrNull { it.size } ?: 0)
    Column(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainerLow, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState()),
    ) {
        TableRow(table.header, columns, MaterialTheme.typography.labelLarge, scheme.primaryContainer)
        table.rows.forEach { row ->
            HorizontalDivider(color = scheme.outlineVariant)
            TableRow(row, columns, MaterialTheme.typography.bodySmall, Color.Transparent)
        }
    }
}

@Composable
private fun TableRow(cells: List<String>, columns: Int, style: TextStyle, background: Color) {
    val codeBackground = MaterialTheme.colorScheme.secondaryContainer
    Row(Modifier.background(background)) {
        repeat(columns) { index ->
            Text(
                inlineText(cells.getOrNull(index).orEmpty(), codeBackground),
                style = style,
                modifier = Modifier.width(160.dp).padding(8.dp),
            )
        }
    }
}

@Composable
private fun inlineText(text: String, codeBackground: Color): AnnotatedString =
    remember(text, codeBackground) {
        buildAnnotatedString {
            MarkdownParser.spans(text).forEach { span ->
                when (span) {
                    is MdSpan.Plain -> append(span.text)
                    is MdSpan.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(span.text) }
                    is MdSpan.InlineCode -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground)) {
                        append(span.text)
                    }
                }
            }
        }
    }
