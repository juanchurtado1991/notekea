package app.notekea.shell.presentation

import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.shell.domain.PlayedVideo
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.tabs.TabKind
import app.notekea.shell.presentation.workspace.WorkspaceIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals

/** The YouTube tab (the developer's smoke of 2026-11-13: a video played in a window of another app, and blank). */
@OptIn(ExperimentalCoroutinesApi::class)
class YouTubeTabTest : WorkspaceViewModelTestBase() {
    @Test
    fun `a video played opens one YouTube tab, named as the video, and another video plays in the same tab`() =
        runWorkspaceTest {
            val viewModel = started()
            viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = AREPAS))

            listOf("dQw4w9WgXcQ" to "Arepas", "9bZkp7q19f0" to "Empanadas").forEach { (id, title) ->
                viewModel.onIntent(
                    intent = WorkspaceIntent.OnYouTubePlay(
                        id = id,
                        title = title
                    )
                )
            }

            advanceUntilIdle()
            val state = viewModel.state.value

            assertEquals(
                expected = listOf<Any?>(
                    listOf(AREPAS, Workspace.YOUTUBE),
                    Workspace.YOUTUBE,
                    TabKind.YOUTUBE,
                    PlayedVideo(
                        id = "9bZkp7q19f0",
                        title = "Empanadas"
                    )
                ),
                actual = listOf(
                    state
                        .workspace
                        .panes
                        .single()
                        .tabs,
                    state.workspace.activePath,
                    state.kindOf(path = Workspace.YOUTUBE),
                    state.video
                )
            )
        }

    @Test
    fun `the last run's YouTube tab, with no video to play, does not come back`() =
        runWorkspaceTest {
            workspaces.workspaces[VAULT] = Workspace.EMPTY
                .open(path = AREPAS)
                .open(path = Workspace.YOUTUBE)

            val viewModel = started()

            assertEquals(
                expected = listOf(AREPAS),
                actual = viewModel
                    .state
                    .value
                    .workspace
                    .panes
                    .single()
                    .tabs
            )
        }
}
