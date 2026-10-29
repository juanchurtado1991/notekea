package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testbuddy.ManualCase
import java.nio.file.Path

/** Step 2: one manual case at a time, what to do and what to see, answered Passed, Failed or Skip for now. */
@Composable
internal fun ManualStep(
    cases: List<ManualCase>,
    index: Int,
    actions: ManualActions
) {
    val case = cases.getOrNull(index) ?: return
    var isFailing by remember(case.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        Card {
            CaseHeading(case = case, number = index + 1, total = cases.size)
            Section(label = "Do this", text = case.steps)
            if (case.data.isNotEmpty()) DataHint(folders = case.data, onOpen = actions.onOpenData)
            Section(label = "You should see", text = case.expected)
            if (isFailing) {
                FailedForm(onSave = actions.onFail, onCancel = { isFailing = false }, onPick = actions.onPickScreenshot)
            } else {
                AnswerButtons(onPass = actions.onPass, onFail = { isFailing = true }, onSkip = actions.onSkip)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = actions.onPrevious, enabled = index > 0) { Text(text = "‹ Previous case") }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Text(
                text = "Your answers are saved: you can close this window and go on later.",
                fontSize = 12.sp,
                color = SelfTestColors.Muted
            )
        }
    }
}

/** What the manual step can ask the session and the system for. */
internal class ManualActions(
    val onPass: () -> Unit,
    val onFail: (note: String, screenshot: Path?) -> Unit,
    val onSkip: () -> Unit,
    val onPrevious: () -> Unit,
    val onOpenData: () -> Unit,
    val onPickScreenshot: () -> Path?
)

@Composable
private fun CaseHeading(
    case: ManualCase,
    number: Int,
    total: Int
) {
    Text(text = "Case $number of $total · ${case.reason.hint()}", fontSize = 12.sp, color = SelfTestColors.Muted)
    Text(text = "${case.id} · ${case.title}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun Section(
    label: String,
    text: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(text = text, fontSize = 14.sp)
    }
}

@Composable
private fun DataHint(
    folders: List<String>,
    onOpen: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "A fresh copy of ${folders.joinToString()} is ready in the Test data folder.",
            fontSize = 13.sp,
            color = SelfTestColors.Muted
        )
        TextButton(onClick = onOpen) { Text(text = "Open the folder") }
    }
}

@Composable
private fun AnswerButtons(
    onPass: () -> Unit,
    onFail: () -> Unit,
    onSkip: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Button(onClick = onPass, colors = ButtonDefaults.buttonColors(containerColor = SelfTestColors.Passed)) {
            Text(text = "Passed")
        }
        Button(onClick = onFail, colors = ButtonDefaults.buttonColors(containerColor = SelfTestColors.Failed)) {
            Text(text = "Failed")
        }
        Spacer(modifier = Modifier.weight(weight = 1f))
        OutlinedButton(onClick = onSkip) { Text(text = "Skip for now") }
    }
}

private fun com.testbuddy.ManualReason.hint(): String = when (this) {
    com.testbuddy.ManualReason.EYES -> "Needs your eyes"
    com.testbuddy.ManualReason.SYSTEM -> "Uses your system"
    com.testbuddy.ManualReason.PACKAGED -> "Uses the installed app"
    com.testbuddy.ManualReason.COSTLY -> "Checked by hand"
}
