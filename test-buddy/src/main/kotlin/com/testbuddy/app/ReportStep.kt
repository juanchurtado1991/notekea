package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Step 3: the report to send, the screenshots' folder, and Finish and clean up once the app is closed. */
@Composable
internal fun ReportStep(
    appName: String,
    state: SelfTestState,
    actions: ReportActions
) {
    var isCopied by remember(state.report) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        Text(
            text = "Send this report, and the pictures of the Report folder, to whoever asked you to test.",
            fontSize = 13.sp,
            color = SelfTestColors.Muted
        )
        ReportText(report = state.report)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Button(onClick = { actions.onCopy(state.report).also { isCopied = true } }) {
                Text(text = if (isCopied) "Copied" else "Copy report")
            }
            OutlinedButton(onClick = actions.onOpenReport) { Text(text = "Open the Report folder") }
            Spacer(modifier = Modifier.weight(weight = 1f))
            OutlinedButton(onClick = actions.onBack) { Text(text = "‹ Manual cases") }
        }
        Card { FinishSection(appName = appName, isClean = state.isClean, onFinish = actions.onFinish) }
    }
}

/** What the report step can ask the session and the system for. */
internal class ReportActions(
    val onCopy: (report: String) -> Unit,
    val onOpenReport: () -> Unit,
    val onBack: () -> Unit,
    val onFinish: () -> Unit
)

@Composable
private fun ReportText(report: String) {
    SelectionContainer {
        Text(
            text = report,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .background(color = SelfTestColors.Quiet, shape = RoundedCornerShape(size = 6.dp))
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        )
    }
}

@Composable
private fun FinishSection(
    appName: String,
    isClean: Boolean,
    onFinish: () -> Unit
) {
    if (isClean) {
        Text(text = "All clean", fontWeight = FontWeight.SemiBold)
        Text(text = "$appName is as it was before the test. You can uninstall $appName Self-Test.", fontSize = 14.sp)
        return
    }
    Text(text = "When you have sent the report", fontWeight = FontWeight.SemiBold)
    Text(
        text = "Close $appName, then press Finish and clean up: it puts $appName back as it was before the test and " +
            "deletes the test data.",
        fontSize = 14.sp
    )
    Button(onClick = onFinish) { Text(text = "Finish and clean up") }
}
