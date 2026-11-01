package com.agentlearning.feature.knowledge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun KnowledgeFilters(
    months: List<Int>,
    weeks: List<Int>,
    scope: KnowledgeScope,
    onMonthChange: (Int?) -> Unit,
    onWeekChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScopeMenu(
            label = scope.monthNumber?.let { "第 $it 月" } ?: "全部月份",
            allLabel = "全部月份",
            options = months,
            selected = scope.monthNumber,
            optionLabel = { "第 $it 月" },
            onSelect = onMonthChange,
            modifier = Modifier.weight(1f),
        )
        ScopeMenu(
            label = scope.weekNumber?.let { "Week %02d".format(it) } ?: "全部周次",
            allLabel = "全部周次",
            options = weeks,
            selected = scope.weekNumber,
            optionLabel = { "Week %02d".format(it) },
            onSelect = onWeekChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScopeMenu(
    label: String,
    allLabel: String,
    options: List<Int>,
    selected: Int?,
    optionLabel: (Int) -> String,
    onSelect: (Int?) -> Unit,
    modifier: Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (listOf<Int?>(null) + options).forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option?.let(optionLabel) ?: allLabel,
                            color = if (option == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (option == selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
fun SearchResultText(
    text: String,
    query: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    val highlight = SpanStyle(
        background = MaterialTheme.colorScheme.primaryContainer,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.SemiBold,
    )
    val annotated = remember(text, query, highlight) {
        buildAnnotatedString {
            append(text)
            searchMatchRanges(text, query).forEach { range -> addStyle(highlight, range.first, range.last + 1) }
        }
    }
    Text(annotated, modifier = modifier, style = style, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}
