package app.notekea.sync.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The dialog naming the files a sync left out for their size (the code review of 2026-11-22), on screen. */
@OptIn(ExperimentalTestApi::class)
class LargeFilesDialogTest {
    @Test
    fun `each file left out is named, the limit is said, and OK closes it`() = runComposeUiTest {
        var closed = 0

        setContent {
            NotekeaTheme(darkTheme = false) {
                LargeFilesDialog(
                    files = listOf(RelativePath(value = "Videos/Film.mp4"), RelativePath(value = "Backup.zip")),
                    onDismiss = {
                        closed++
                    }
                )
            }
        }

        val named = listOf("Videos/Film.mp4", "Backup.zip", "over 100 MB").sumOf {
            onAllNodesWithText(
                text = it,
                substring = true
            ).fetchSemanticsNodes().size
        }

        onNodeWithText(text = "OK").performClick()
        waitForIdle()

        assertEquals(
            expected = listOf(3, 1),
            actual = listOf(named, closed)
        )
    }
}
