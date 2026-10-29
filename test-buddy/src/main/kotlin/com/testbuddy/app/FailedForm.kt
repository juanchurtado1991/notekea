package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.Path

/** What a tester fills in when a case failed: what they saw, and a screenshot if they took one. */
@Composable
internal fun FailedForm(
    onSave: (note: String, screenshot: Path?) -> Unit,
    onCancel: () -> Unit,
    onPick: () -> Path?
) {
    var note by remember { mutableStateOf("") }
    var screenshot by remember { mutableStateOf<Path?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(text = "What did you see?") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            OutlinedButton(onClick = { onPick()?.let { screenshot = it } }) { Text(text = "Add a screenshot…") }
            Text(
                text = screenshot?.fileName?.toString() ?: "Optional, but it helps a lot",
                fontSize = 12.sp,
                color = SelfTestColors.Muted
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Button(
                onClick = { onSave(note, screenshot) },
                enabled = note.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SelfTestColors.Failed)
            ) { Text(text = "Save as failed") }
            TextButton(onClick = onCancel) { Text(text = "Cancel") }
        }
    }
}
