package app.notekea.shell.presentation

import app.notekea.shell.presentation.workspace.WorkspaceIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals

/** The side panel when what was stored of it cannot be read (the code review of 2026-11-22). */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceSidePanelRestoreTest : WorkspaceViewModelTestBase() {
    @Test
    fun `a side panel that could not be restored still hides, and the tabs are restored without it`() =
        runWorkspaceTest {
            preferences.isSidePanelUnreadable = true
            val viewModel = started()

            viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = AREPAS))
            viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelToggle)
            advanceUntilIdle()

            assertEquals(
                expected = listOf(false, false, true),
                actual = listOf(
                    viewModel.state.value.sidePanel.isVisible,
                    preferences.sidePanel.isVisible,
                    viewModel.state.value.isRestored
                )
            )
        }
}
