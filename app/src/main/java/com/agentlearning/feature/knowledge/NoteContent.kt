package com.agentlearning.feature.knowledge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agentlearning.feature.quiz.components.CodeBlock

private sealed interface NoteBlock {
    data class Heading(val text: String) : NoteBlock
    data class Bullet(val text: String) : NoteBlock
    data class Paragraph(val text: String) : NoteBlock
    data class Code(val code: String) : NoteBlock
}

// Supported syntax: "## heading", "- bullet", fenced ``` code blocks, and plain paragraphs.
private fun parseNotes(notes: String): List<NoteBlock> {
    val blocks = mutableListOf<NoteBlock>()
    var code: MutableList<String>? = null
    for (line in notes.lines()) {
        val open = code
        when {
            line.trimStart().startsWith("```") -> {
                if (open == null) {
                    code = mutableListOf()
                } else {
                    blocks += NoteBlock.Code(open.joinToString("\n"))
                    code = null
                }
            }
            open != null -> open += line
            line.isBlank() -> Unit
            line.startsWith("## ") -> blocks += NoteBlock.Heading(line.removePrefix("## "))
            line.startsWith("- ") -> blocks += NoteBlock.Bullet(line.removePrefix("- "))
            else -> blocks += NoteBlock.Paragraph(line)
        }
    }
    code?.let { blocks += NoteBlock.Code(it.joinToString("\n")) }
    return blocks
}

@Composable
fun NoteContent(notes: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parseNotes(notes).forEach { block ->
            when (block) {
                is NoteBlock.Heading -> Text(
                    block.text,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                is NoteBlock.Bullet -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", color = MaterialTheme.colorScheme.primary)
                    Text(block.text, style = MaterialTheme.typography.bodyLarge)
                }
                is NoteBlock.Paragraph -> Text(block.text, style = MaterialTheme.typography.bodyLarge)
                is NoteBlock.Code -> CodeBlock(block.code)
            }
        }
    }
}
