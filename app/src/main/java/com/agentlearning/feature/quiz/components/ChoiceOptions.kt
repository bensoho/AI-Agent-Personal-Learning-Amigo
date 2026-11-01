package com.agentlearning.feature.quiz.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.agentlearning.domain.model.Question

@Composable
fun ChoiceOptions(
    question: Question,
    selected: Set<String>,
    submitted: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options.forEachIndexed { index, option ->
            val isSelected = option.id in selected
            val isCorrect = option.id in question.correctAnswer
            val targetContainer = when {
                submitted && isCorrect -> MaterialTheme.colorScheme.secondaryContainer
                submitted && isSelected -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
            val targetBorder = when {
                submitted && isCorrect -> MaterialTheme.colorScheme.secondary
                submitted && isSelected -> MaterialTheme.colorScheme.error
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val container by animateColorAsState(targetContainer, label = "optionContainer")
            val borderColor by animateColorAsState(targetBorder, label = "optionBorder")
            Surface(
                color = container,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(if (isSelected || (submitted && isCorrect)) 1.5.dp else 1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        enabled = !submitted,
                        role = if (question.isMultiSelect) Role.Checkbox else Role.RadioButton,
                        onClick = { onToggle(option.id) },
                    ),
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    // The whole row handles the click, so the control itself is display-only.
                    if (question.isMultiSelect) {
                        Checkbox(checked = isSelected, onCheckedChange = null, enabled = !submitted)
                    } else {
                        RadioButton(selected = isSelected, onClick = null, enabled = !submitted)
                    }
                    Text(
                        "${optionLabel(index)}. ${option.text}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    // Shape + color mark the result, so correctness is not conveyed by color alone.
                    when {
                        submitted && isCorrect -> Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "正确答案",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        submitted && isSelected -> Icon(
                            Icons.Filled.Close,
                            contentDescription = "选择错误",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
