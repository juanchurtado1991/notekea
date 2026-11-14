package app.notekea.core.presentation.designsystem.components.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test

/** A tooltip inside selectable text, as a citation of a chat answer is (crash report of 2026-11-14). */
@OptIn(ExperimentalTestApi::class)
class NotekeaTooltipTest {
    @Test
    fun `a press on selectable text with a tooltip shown does not crash`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                SelectionContainer {
                    Column {
                        Text(text = ANSWER)

                        NotekeaTooltip(label = PASSAGE) { Text(text = CITATION) }
                    }
                }
            }
        }

        onNodeWithText(text = CITATION).performMouseInput { moveTo(position = center) }
        mainClock.advanceTimeBy(milliseconds = SHOWN_AFTER_MILLIS)
        waitForIdle()
        onNodeWithText(text = PASSAGE).assertExists()

        onNodeWithText(text = ANSWER).performMouseInput { click() }
        waitForIdle()
    }

    private companion object {
        const val ANSWER = "The developer is Fidelis"
        const val CITATION = "1"
        const val PASSAGE = "Developed by Fidelis in San José Villanueva."
        const val SHOWN_AFTER_MILLIS = 3_000L
    }
}
