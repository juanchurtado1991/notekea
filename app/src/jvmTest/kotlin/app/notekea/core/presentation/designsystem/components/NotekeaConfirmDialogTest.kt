package app.notekea.core.presentation.designsystem.components

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** macOS's alert (redesign 8A): stacked buttons, and Return chooses the safe one when the action destroys. */
@OptIn(ExperimentalTestApi::class)
class NotekeaConfirmDialogTest {
    @Test
    fun `a stacked destructive alert shows both buttons, and Return cancels`() = runComposeUiTest {
        val answers = mutableListOf<String>()

        setContent {
            NotekeaTheme(darkTheme = false) {
                NotekeaConfirmDialog(
                    title = "Delete this note?",
                    message = "It moves to the trash.",
                    confirmLabel = DELETE,
                    cancelLabel = CANCEL,
                    buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
                    onConfirm = { answers += DELETE },
                    onDismiss = { answers += CANCEL },
                    isDestructive = true,
                    isStacked = true
                )
            }
        }

        waitForIdle()
        onNodeWithText(text = DELETE).assertExists()
        onNodeWithText(text = CANCEL).performKeyInput { pressKey(key = Key.Enter) }
        waitForIdle()

        assertEquals(
            expected = listOf(CANCEL),
            actual = answers
        )
    }

    private companion object {
        const val CANCEL = "Cancel"
        const val DELETE = "Delete"
    }
}
