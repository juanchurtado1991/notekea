package com.testbuddy.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.testbuddy.ManualReason
import com.testbuddy.sampleSuite
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * At 200% on macOS and on Windows the manager's window is too short for a case: the case scrolls and its buttons
 * stay inside the window, whole.
 */
class ManagerContentTest {
    private val suite = sampleSuite {
        manual(
            id = "1.2-01",
            reason = ManualReason.SYSTEM
        ) {
            prepare("notes")
            title(text = "Opens the note")

            steps(
                text = (1..STEPS)
                    .joinToString(separator = "\n") {
                        "$it. Do a long step number $it of the case."
                    }
            )

            expected(text = "It opens")
        }
    }
    private val actions = ManagerActions(
        onTab = {},
        onStart = {},
        onStop = {},
        onNextStep = {},
        onPreviousStep = {},
        onPass = {},
        onFail = { _, _ -> },
        onSkip = {},
        onPreviousCase = {},
        onRestart = {},
        onPickScreenshot = { null },
        onCopy = {},
        onOpenReport = {}
    )

    @Test
    fun `in a short window a case's buttons stay whole inside it`() = runComposeUiTest {
        setContent {
            AutoTestTheme {
                Box(
                    modifier = Modifier.size(
                        width = WIDTH_DP.dp,
                        height = SHORT_HEIGHT_DP.dp
                    )
                ) {
                    ManagerContent(
                        suite = suite,
                        machine = "Linux · 1920×1080",
                        dataFolder = Path.of("/home/ana/Documents/Sample QA/Test data"),
                        state = ManagerState(tab = ManagerTab.MANUAL),
                        actions = actions
                    )
                }
            }
        }

        val (height, bottom) = with(density) {
            BUTTON_HEIGHT_DP.dp.toPx() to SHORT_HEIGHT_DP.dp.toPx()
        }

        listOf("Done, next step ›", "Skip this case ›").forEach { text ->
            val button = onNodeWithText(text).fetchSemanticsNode().boundsInRoot
            assertTrue(button.height >= height && button.bottom <= bottom, "$text is at $button")
        }
    }

    private companion object {
        const val BUTTON_HEIGHT_DP = 36
        const val SHORT_HEIGHT_DP = 420
        const val STEPS = 8
        const val WIDTH_DP = 380
    }
}
