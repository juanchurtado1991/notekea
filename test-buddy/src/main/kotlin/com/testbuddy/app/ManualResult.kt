package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
 * A manual case's result, after its last step: the case's [header], what the tester should have seen, then the case's
 * two answers ([ManualCase.passLabel], [ManualCase.failLabel]). The second asks what they saw and takes a screenshot;
 * the steps they took in the app are added to the report. Save and [otherCases] stay under it.
 */
@Composable
internal fun ManualResult(
    case: ManualCase,
    actions: ManagerActions,
    header: @Composable ColumnScope.() -> Unit,
    otherCases: @Composable ColumnScope.() -> Unit
) {
    var isFailing by remember(case.id) { mutableStateOf(value = false) }

    var note by remember(case.id) { mutableStateOf(value = "") }

    var screenshot by remember(case.id) { mutableStateOf<Path?>(value = null) }
    val texts = LocalTesterTexts.current

    TabLayout(actions = {
        if (isFailing) {
            Button(
                onClick = { actions.onFail(note, screenshot) },
                enabled = note.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = texts.saveAndNext) }
        } else {
            OutlinedButton(
                onClick = actions.onPreviousStep,
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = texts.backToSteps) }
        }

        otherCases()
    }) {
        header()
        SectionLabel(text = texts.shouldHaveSeen)

        Text(
            text = case.expected,
            fontSize = 13.sp
        )

        Answers(
            case = case,
            actions = actions,
            onFail = { isFailing = true }
        )

        if (isFailing) {
            WhatYouSaw(
                note = note,
                hasScreenshot = screenshot != null,
                onNote = { note = it },
                onPickScreenshot = {
                    actions.onPickScreenshot()?.let { screenshot = it }
                }
            )
        }
    }
}

@Composable
private fun Answers(
    case: ManualCase,
    actions: ManagerActions,
    onFail: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Button(
            onClick = actions.onPass,
            colors = ButtonDefaults.buttonColors(containerColor = AutoTestColors.Passed),
            modifier = Modifier.weight(weight = 1f)
        ) { Text(text = case.passLabel) }

        Button(
            onClick = onFail,
            colors = ButtonDefaults.buttonColors(containerColor = AutoTestColors.Failed),
            modifier = Modifier.weight(weight = 1f)
        ) { Text(text = case.failLabel) }
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
        label = { Text(text = LocalTesterTexts.current.whatDidYouSee) },
        modifier = Modifier.fillMaxWidth().height(height = 96.dp)
    )

    val texts = LocalTesterTexts.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onPickScreenshot) {
            Text(
                text = if (hasScreenshot) texts.screenshotAdded else texts.addScreenshot,
                fontSize = 12.sp
            )
        }

        Text(
            text = texts.lastStepsAdded,
            fontSize = 10.sp,
            color = AutoTestColors.Muted
        )
    }
}
