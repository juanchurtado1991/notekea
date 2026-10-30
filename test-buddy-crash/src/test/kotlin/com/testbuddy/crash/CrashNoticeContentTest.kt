@file:OptIn(ExperimentalTestApi::class)

package com.testbuddy.crash

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

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
                    crash = CrashReport(text = "report", path = "/reports/crash.txt"),
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

    @Test
    fun `without an issue form there is no Report button`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                CrashNoticeContent(
                    crash = CrashReport(text = "report", path = null),
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
}
