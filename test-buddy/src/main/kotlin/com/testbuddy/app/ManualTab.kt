package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testbuddy.ManualAnswer
import com.testbuddy.ManualCase
import com.testbuddy.ManualReason
import java.nio.file.Path

/**
 * The manual cases, one at a time: which case of how many and why it is manual, its title, and its steps as a line
 * (✓ done, the current one in bold, the rest to come); Back and "Done, next step"; above them the case's test data and
 * how to open it ([TestDataCard]), and Restart the app when the case needs it. After its last step comes the result ([ManualResult]).
 */
@Composable
internal fun ColumnScope.ManualTab(
    appName: String,
    cases: List<ManualCase>,
    dataFolder: Path,
    state: ManagerState,
    actions: ManagerActions
) {
    val case = cases.getOrNull(state.manualIndex)
    if (case == null) {
        Text(text = "There are no manual cases.", fontSize = 13.sp)
        return
    }
    val answered = state.answers[case.id]?.let { if (it is ManualAnswer.Passed) " · passed" else " · failed" }.orEmpty()
    Text(
        text = "Case ${state.manualIndex + 1} of ${cases.size} · ${case.reason.hint()}$answered",
        fontSize = 11.sp,
        color = SelfTestColors.Muted
    )
    Text(text = "${case.id} · ${case.title}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    if (state.manualStep >= case.stepList.size) {
        ManualResult(case = case, actions = actions)
    } else {
        // In full on the first step, where the vault is opened; one line after it, so the steps keep their room.
        when {
            case.data.isEmpty() -> Unit

            state.manualStep == 0 -> TestDataCard(appName = appName, folders = case.data, dataFolder = dataFolder)

            else -> Text(
                text = "Test data: a fresh copy of ${case.data.joinToString()}",
                fontSize = 11.sp,
                color = SelfTestColors.Muted
            )
        }
        Steps(appName = appName, case = case, step = state.manualStep, actions = actions)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = actions.onPreviousCase, enabled = state.manualIndex > 0) {
            Text(text = "‹ Previous case", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(weight = 1f))
        TextButton(onClick = actions.onSkip) { Text(text = "Skip this case ›", fontSize = 12.sp) }
    }
}

@Composable
private fun ColumnScope.Steps(
    appName: String,
    case: ManualCase,
    step: Int,
    actions: ManagerActions
) {
    Card {
        SectionLabel(text = "STEP ${step + 1} OF ${case.stepList.size}")
        StepTimeline(
            done = case.stepList.take(n = step),
            current = case.stepList[step],
            pending = case.stepList.drop(n = step + 1)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        if (case.launchEnvironment.isNotEmpty()) {
            OutlinedButton(onClick = actions.onRestart) {
                Text(text = "Restart $appName for this case", fontSize = 12.sp)
            }
        }
    }
    Spacer(modifier = Modifier.weight(weight = 1f))
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = actions.onPreviousStep, enabled = step > 0) { Text(text = "‹ Back") }
        Spacer(modifier = Modifier.weight(weight = 1f))
        Button(onClick = actions.onNextStep) {
            Text(text = if (step == case.stepList.lastIndex) "Done ›" else "Done, next step ›")
        }
    }
}

internal fun ManualReason.hint(): String = when (this) {
    ManualReason.EYES -> "Needs your eyes"
    ManualReason.SYSTEM -> "Uses your system"
    ManualReason.PACKAGED -> "Uses the installed app"
    ManualReason.COSTLY -> "Checked by hand"
}
