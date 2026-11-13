package app.notekea.clips.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.clips.domain.UpdateStep
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The status bar says an update of the web sources is going on (the developer's request of 2026-11-13). */
@OptIn(ExperimentalTestApi::class)
class WebUpdateStatusTest {
    @Test
    fun `while pages are read the bar says which of how many, a click shows the dialog, and it leaves at the end`() =
        runComposeUiTest {
            val intents = mutableListOf<ClipIntent>()

            var state by mutableStateOf(
                value = ClipState(
                    update = UpdateStep.Reading(
                        done = 11,
                        total = 45,
                        url = "https://x.com/docs"
                    )
                )
            )

            setContent {
                NotekeaTheme(darkTheme = false) {
                    WebUpdateStatus(
                        state = state,
                        onIntent = { intents += it }
                    )
                }
            }

            onNodeWithText(text = "Web sources · 12 of 45").performClick()
            state = state.copy(update = UpdateStep.Done())
            waitForIdle()

            onNodeWithText(
                text = "Web sources",
                substring = true
            ).assertDoesNotExist()

            assertEquals(
                expected = listOf<ClipIntent>(ClipIntent.OnUpdateShow),
                actual = intents
            )
        }
}
