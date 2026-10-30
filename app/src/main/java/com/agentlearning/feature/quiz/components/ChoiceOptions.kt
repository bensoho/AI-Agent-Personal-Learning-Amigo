package com.agentlearning.feature.quiz.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        question.options.forEach { option ->
            val isSelected = option.id in selected
            val isCorrect = option.id in question.correctAnswer
            val container = when {
                submitted && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                submitted && isSelected -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
            Surface(
                color = container,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
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
                    Text("${option.id}. ${option.text}", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
