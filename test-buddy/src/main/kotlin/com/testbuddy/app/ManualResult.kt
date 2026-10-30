package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testbuddy.ManualCase
import java.nio.file.Path

/**
 * A manual case's result, after its last step: what the tester should have seen, then the case's two answers
 * ([ManualCase.passLabel], [ManualCase.failLabel]). The second asks what they saw and takes a screenshot; the steps
 * they took in the app are added to the report for them.
 */
@Composable
internal fun ColumnScope.ManualResult(
    case: ManualCase,
    actions: ManagerActions
) {
    var isFailing by remember(case.id) { mutableStateOf(value = false) }
    var note by remember(case.id) { mutableStateOf(value = "") }
    var screenshot by remember(case.id) { mutableStateOf<Path?>(value = null) }
    SectionLabel(text = "YOU SHOULD HAVE SEEN")
    Text(text = case.expected, fontSize = 13.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Button(
            onClick = actions.onPass,
            colors = ButtonDefaults.buttonColors(containerColor = SelfTestColors.Passed),
            modifier = Modifier.weight(weight = 1f)
        ) { Text(text = case.passLabel) }
        Button(
            onClick = { isFailing = true },
            colors = ButtonDefaults.buttonColors(containerColor = SelfTestColors.Failed),
            modifier = Modifier.weight(weight = 1f)
        ) { Text(text = case.failLabel) }
    }
    if (isFailing) {
        WhatYouSaw(
            note = note,
            hasScreenshot = screenshot != null,
            onNote = { note = it },
            onPickScreenshot = { actions.onPickScreenshot()?.let { screenshot = it } }
        )
    }
    Spacer(modifier = Modifier.weight(weight = 1f))
    if (isFailing) {
        Button(onClick = {
            actions.onFail(note, screenshot)
        }, enabled = note.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(text = "Save and go to the next case")
        }
    } else {
        OutlinedButton(onClick = actions.onPreviousStep, modifier = Modifier.fillMaxWidth()) {
            Text(text = "‹ Back to the steps")
        }
    }
}

/** What the tester saw, and a screenshot: the note is needed to save a failure. */
@Composable
private fun WhatYouSaw(
    note: String,
    hasScreenshot: Boolean,
    onNote: (String) -> Unit,
    onPickScreenshot: () -> Unit
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNote,
        label = { Text(text = "What did you see?") },
        modifier = Modifier.fillMaxWidth().height(height = 96.dp)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onPickScreenshot) {
            Text(text = if (hasScreenshot) "Screenshot added" else "Add a screenshot", fontSize = 12.sp)
        }
        Text(text = "Your last steps in the app are added for you.", fontSize = 10.sp, color = SelfTestColors.Muted)
    }
}
