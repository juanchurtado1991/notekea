package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * The report to send: what to do with it, the report itself (the tab scrolls), Copy report, and the Report folder with
 * the screenshots, the app's output and a copy of the report.
 */
@Composable
internal fun ReportTab(
    state: ManagerState,
    actions: ManagerActions
) {
    var isCopied by remember(state.report) { mutableStateOf(value = false) }

    TabLayout(actions = {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Button(
                onClick = {
                    actions.onCopy(state.report).also { isCopied = true }
                },
                modifier = Modifier.weight(weight = 1f)
            ) { Text(text = if (isCopied) "Copied" else "Copy report") }

            OutlinedButton(
                onClick = actions.onOpenReport,
                modifier = Modifier.weight(weight = 1f)
            ) { Text(text = "Open the Report folder") }
        }
    }) { Report(report = state.report) }
}

@Composable
private fun ColumnScope.Report(report: String) {
    Text(
        text = "Send the report",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Text(
        text = "Copy it into a message to whoever asked you to test, with the pictures of the Report folder.",
        fontSize = 12.sp,
        color = AutoTestColors.Muted
    )

    Card {
        Text(
            text = report,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
