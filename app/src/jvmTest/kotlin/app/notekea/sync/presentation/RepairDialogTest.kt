package app.notekea.sync.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.sync.domain.RepairChoice
import kotlin.test.Test
import kotlin.test.assertEquals

/** The two ways to repair a vault left in the middle of a merge, on screen (2026-11-14). */
@OptIn(ExperimentalTestApi::class)
class RepairDialogTest {
    @Test
    fun `each way to repair is its own choice, the files changed here are named, and Not now changes nothing`() =
        runComposeUiTest {
            val chosen = mutableListOf<Any>()

            setContent {
                NotekeaTheme(darkTheme = false) {
                    RepairDialog(
                        changedHere = (1..8).map { RelativePath(value = "Notes/Note $it.md") },
                        onChoose = { chosen += it },
                        onDismiss = { chosen += LATER }
                    )
                }
            }

            onNodeWithText(text = "Keep my changes").performClick()
            onNodeWithText(text = "Go back to the repository").performClick()
            onNodeWithText(text = "Not now").performClick()
            waitForIdle()

            val named = onAllNodesWithText(
                text = "Note 1, Note 2, Note 3, Note 4, Note 5, Note 6 +2",
                substring = true
            ).fetchSemanticsNodes().size

            assertEquals(
                expected = listOf<Any>(
                    listOf(RepairChoice.KEEP_MINE, RepairChoice.TAKE_REMOTE, LATER),
                    1
                ),
                actual = listOf<Any>(chosen, named)
            )
        }

    private companion object {
        const val LATER = "later"
    }
}
