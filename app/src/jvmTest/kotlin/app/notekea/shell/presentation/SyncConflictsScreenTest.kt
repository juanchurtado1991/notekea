package app.notekea.shell.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.shell.presentation.follow.SyncConflictsOf
import app.notekea.sync.domain.FakeVaultSync
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.domain.VaultSyncs
import app.notekea.sync.presentation.SyncStatusViewModel
import kotlin.test.Test

/** A conflict on screen: the banner names the note, Review shows both versions, Keep mine settles it. */
@OptIn(ExperimentalTestApi::class)
class SyncConflictsScreenTest {
    @Test
    fun `Review on the banner shows both versions, and a choice closes it`() = runComposeUiTest {
        val sync = FakeVaultSync()
        val store = FakeNoteContentStore()
        store.texts[PLAN] = "Friday"
        store.texts[RelativePath(value = "Plan (conflict).md")] = "Monday"

        val viewModel = SyncStatusViewModel(
            vaultPath = "/vault",
            engines = VaultSyncs { sync },
            noteContentStore = store
        )

        sync.status.value = SyncStatus.Conflict(notes = listOf(PLAN))

        setContent {
            NotekeaTheme(darkTheme = false) { SyncConflictsOf(sync = viewModel) }
        }

        onNodeWithText(text = "Changed here and on another computer: Plan").assertExists()
        onNodeWithText(text = "Review").performClick()
        waitForIdle()
        onNodeWithText(text = "Plan changed in two places").assertExists()
        onNodeWithText(text = "Monday").assertExists()
        onNodeWithText(text = "Friday").assertExists()
        onNodeWithText(text = "Keep mine").performClick()
        waitForIdle()
        onNodeWithText(text = "Plan changed in two places").assertDoesNotExist()
    }

    private companion object {
        val PLAN = RelativePath(value = "Plan.md")
    }
}
