package com.testbuddy.crash

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

/**
 * The crash notice, in a dialog window of its own once a window has failed ([CrashBuddy.crash]); put it next to the app's
 * windows inside `application { }`. [theme] wraps it in the app's look (its MaterialTheme, light or dark); closing it
 * closes the failed window ([CrashBuddy.close]).
 */
@Composable
public fun CrashNotice(
    crashBuddy: CrashBuddy,
    theme: @Composable (content: @Composable () -> Unit) -> Unit = { MaterialTheme { it() } }
) {
    val crash by crashBuddy.crash.collectAsState()
    val report = crash ?: return
    DialogWindow(
        onCloseRequest = crashBuddy::close,
        state = rememberDialogState(size = DpSize(width = WIDTH_DP.dp, height = HEIGHT_DP.dp)),
        title = crashBuddy.config.texts.title,
        resizable = false
    ) {
        theme {
            CrashNoticeContent(
                crash = report,
                config = crashBuddy.config,
                onCopy = { CrashActions.copy(text = report.text) },
                onReport = { url -> CrashActions.report(text = report.text, url = url) },
                onClose = crashBuddy::close
            )
        }
    }
}

private const val WIDTH_DP = 560
private const val HEIGHT_DP = 320
