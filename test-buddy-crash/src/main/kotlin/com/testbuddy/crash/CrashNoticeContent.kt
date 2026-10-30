package com.testbuddy.crash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * What the crash notice says: the app must close, its work is saved first, where the report is (selectable), and the
 * actions: Copy details (confirmed under the buttons), Report on GitHub when the app has a form, and Close, the primary
 * one, placed as the platform orders dialog buttons ([CrashConfig.isCloseLast]). It fills the window it is in.
 */
@Composable
internal fun CrashNoticeContent(
    crash: CrashReport,
    config: CrashConfig,
    onCopy: () -> Unit,
    onReport: (url: String) -> Unit,
    onClose: () -> Unit
) {
    var isCopied by remember(crash) { mutableStateOf(value = false) }
    val texts = config.texts
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(all = 24.dp), verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            Text(
                text = texts.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(text = texts.message, style = MaterialTheme.typography.bodyMedium)
            ReportLocation(texts = texts, path = crash.path)
            Spacer(modifier = Modifier.weight(weight = 1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                if (!config.isCloseLast) Button(onClick = onClose) { Text(text = texts.close) }
                OutlinedButton(onClick = { onCopy().also { isCopied = true } }) { Text(text = texts.copy) }
                config.issueFormUrl?.let { url ->
                    OutlinedButton(onClick = { onReport(url) }) { Text(text = texts.report) }
                }
                Spacer(modifier = Modifier.weight(weight = 1f))
                if (config.isCloseLast) Button(onClick = onClose) { Text(text = texts.close) }
            }
            if (isCopied) Text(text = texts.copied, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Where the report was saved, selectable, or that it could not be (it can still be copied). */
@Composable
private fun ReportLocation(
    texts: CrashTexts,
    path: String?
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    if (path == null) {
        Text(text = texts.notSaved, style = MaterialTheme.typography.bodySmall, color = muted)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Text(text = texts.saved, style = MaterialTheme.typography.bodySmall, color = muted)
        SelectionContainer {
            Text(
                text = path,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = muted
            )
        }
    }
}
