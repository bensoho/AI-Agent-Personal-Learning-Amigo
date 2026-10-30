package com.agentlearning.feature.quiz.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CodeColors(val keyword: Color, val string: Color, val comment: Color, val number: Color)

// Groups: 1 comment, 2 string, 3 keyword, 4 number. The leftmost match wins, so '#' inside a string stays a string.
private val tokenRegex = Regex(
    """(#.*)|("{3}[\s\S]*?"{3}|'{3}[\s\S]*?'{3}|"(?:\\.|[^"\\\n])*"|'(?:\\.|[^'\\\n])*')""" +
        """|\b(def|class|return|import|from|as|if|elif|else|for|while|in|not|and|or|is|None|True|False|with|try|except|finally|raise|lambda|yield|async|await|pass|break|continue)\b""" +
        """|\b(\d+(?:\.\d+)?)\b"""
)

private fun highlight(code: String, colors: CodeColors): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (match in tokenRegex.findAll(code)) {
        append(code.substring(last, match.range.first))
        val color = when {
            match.groups[1] != null -> colors.comment
            match.groups[2] != null -> colors.string
            match.groups[3] != null -> colors.keyword
            else -> colors.number
        }
        withStyle(SpanStyle(color = color)) { append(match.value) }
        last = match.range.last + 1
    }
    append(code.substring(last))
}

@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    val scheme = MaterialTheme.colorScheme
    val colors = CodeColors(scheme.primary, scheme.tertiary, scheme.outline, scheme.secondary)
    val text = remember(code, colors) { highlight(code, colors) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { clipboard.setText(AnnotatedString(code)) }) { Text("复制代码") }
            }
            Box(Modifier.horizontalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                Text(text, fontFamily = FontFamily.Monospace, fontSize = 13.sp, softWrap = false)
            }
        }
    }
}
