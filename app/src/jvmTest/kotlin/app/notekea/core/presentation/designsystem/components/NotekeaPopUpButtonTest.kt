package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The pop-up button's menu opens under it (the developer's choice of 2026-11-23): it opened over the button. */
@OptIn(ExperimentalTestApi::class)
class NotekeaPopUpButtonTest {
    @Test
    fun `the menu opens under the button, which stays in sight`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                // Away from the window's top, where a menu is kept from the edge whatever its button asks.
                Box(modifier = Modifier.padding(top = TOP_DP.dp)) {
                    NotekeaPopUpButton(
                        choice = CHOICE,
                        choices = listOf(SYSTEM, CHOICE).map {
                            NotekeaMenuItem(
                                label = it,
                                onClick = {},
                                isChecked = it == CHOICE
                            )
                        },
                        label = "Language"
                    )
                }
            }
        }

        val button = onNodeWithText(text = CHOICE).fetchSemanticsNode().boundsInWindow
        onNodeWithText(text = CHOICE).performClick()
        waitForIdle()

        val first = onNodeWithText(text = SYSTEM).fetchSemanticsNode().boundsInWindow

        assertEquals(
            expected = listOf(2, true),
            actual = listOf(
                onAllNodesWithText(text = CHOICE).fetchSemanticsNodes().size,
                first.top >= button.bottom
            )
        )
    }

    private companion object {
        const val CHOICE = "Español"
        const val SYSTEM = "System"
        const val TOP_DP = 200
    }
}
