package app.notekea.chat.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.chat.presentation.view.ChatMessages
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test

/** The chat follows the answer as it is written (the developer's smoke of 2026-11-13: it stayed where it was). */
@OptIn(ExperimentalTestApi::class)
class ChatFollowingTest {
    @Test
    fun `the end of an answer far longer than the panel is in view as it is written`() =
        runComposeUiTest {
            val entry = ChatPreviewData.writing.copy(
                step = ChatStep.WRITING,
                sources = emptyList()
            )

            var answer by mutableStateOf(value = "Start")

            setContent {
                NotekeaTheme(darkTheme = false) {
                    Box(modifier = Modifier.size(size = PANEL_DP.dp)) {
                        ChatMessages(
                            entries = listOf(ChatPreviewData.answered, entry.copy(answer = answer)),
                            isSmall = false,
                            onIntent = {}
                        )
                    }
                }
            }

            waitForIdle()

            repeat(times = PARAGRAPHS) {
                answer += "\n\nParagraph $it of a long answer."
                waitForIdle()
            }

            answer += "\n\nThe last words."
            waitForIdle()

            onNodeWithText(
                text = "The last words.",
                substring = true
            ).assertIsDisplayed()
        }

    private companion object {
        const val PANEL_DP = 320
        const val PARAGRAPHS = 30
    }
}
