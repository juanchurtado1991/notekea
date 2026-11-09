@file:OptIn(ExperimentalTestApi::class)

package com.testbuddy.crash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The notice's buttons: Copy confirms itself, Report opens the app's form and Close closes; no form, no Report. */
class CrashNoticeContentTest {
    private val config = CrashConfig(
        appName = "Sample",
        version = "1.0",
        reportFolder = Path.of("/tmp/never"),
        issueFormUrl = "https://example.com/issues/new"
    )

    @Test
    fun `the buttons copy, report and close`() = runComposeUiTest {
        val events = mutableListOf<String>()

        setContent {
            MaterialTheme {
                CrashNoticeContent(
                    crash = CrashReport(
                        text = "report",
                        path = "/reports/crash.txt"
                    ),
                    config = config,
                    onCopy = { events += "copy" },
                    onReport = { events += "report $it" },
                    onClose = { events += "close" }
                )
            }
        }

        onNodeWithText("Copy details").performClick()
        onNodeWithText("Copied to the clipboard").assertExists()
        onNodeWithText("Report on GitHub").performClick()
        onNodeWithText("Close Sample").performClick()
        onNodeWithText("/reports/crash.txt").assertExists()

        assertEquals(listOf("copy", "report https://example.com/issues/new", "close"), events)
    }

    // On macOS at 200% and on Windows the notice's window is shorter than its text: the text scrolls and the buttons
    // stay inside it.
    @Test
    fun `in a short window the buttons stay inside it`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier.size(
                        width = WIDTH_DP.dp,
                        height = SHORT_HEIGHT_DP.dp
                    )
                ) {
                    CrashNoticeContent(
                        crash = CrashReport(
                            text = "report",
                            path = "/reports/" + "long folder/".repeat(n = 20)
                        ),
                        config = config,
                        onCopy = {},
                        onReport = {},
                        onClose = {}
                    )
                }
            }
        }

        val close = onNodeWithText("Close Sample").fetchSemanticsNode().boundsInRoot

        val (height, bottom) = with(density) {
            BUTTON_HEIGHT_DP.dp.toPx() to SHORT_HEIGHT_DP.dp.toPx()
        }

        assertTrue(
            close.height >= height && close.bottom <= bottom,
            "The Close button is at $close"
        )
    }

    @Test
    fun `without an issue form there is no Report button`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                CrashNoticeContent(
                    crash = CrashReport(
                        text = "report",
                        path = null
                    ),
                    config = config.copy(issueFormUrl = null),
                    onCopy = {},
                    onReport = {},
                    onClose = {}
                )
            }
        }

        onAllNodesWithText("Report on GitHub").assertCountEquals(0)
        onNodeWithText("The report could not be saved, but you can still copy it.").assertExists()
    }

    private companion object {
        const val BUTTON_HEIGHT_DP = 40
        const val SHORT_HEIGHT_DP = 220
        const val WIDTH_DP = 560
    }
}
