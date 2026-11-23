package app.notekea.sync.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The buttons that end a dialog follow the platform (the developer's choice of 2026-11-23): the conflict's choices
 * stood in one order everywhere, Keep mine last also on Windows, where the primary button comes first.
 */
@OptIn(ExperimentalTestApi::class)
class ConflictDialogButtonsTest {
    @Test
    fun `Keep mine is the last button, and the first on Windows`() {
        assertEquals(
            expected = listOf(
                listOf("Keep both", "Keep theirs", "Keep mine"),
                listOf("Keep mine", "Keep theirs", "Keep both")
            ),
            actual = listOf(
                shown(buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY),
                shown(buttonOrder = DialogButtonOrder.PRIMARY_THEN_CANCEL)
            )
        )
    }

    private fun shown(buttonOrder: DialogButtonOrder): List<String> {
        var order = emptyList<String>()

        runComposeUiTest {
            setContent {
                NotekeaTheme(darkTheme = false) {
                    ConflictDialog(
                        review = ConflictReview(
                            note = RelativePath(value = "Plan.md"),
                            mine = "Monday",
                            theirs = "Friday"
                        ),
                        buttonOrder = buttonOrder,
                        onChoose = {},
                        onDismiss = {}
                    )
                }
            }

            order = listOf("Keep both", "Keep theirs", "Keep mine").sortedBy {
                onNodeWithText(text = it).getBoundsInRoot().left
            }
        }

        return order
    }
}
