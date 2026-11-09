package app.notekea.shell.presentation

import androidx.compose.ui.unit.IntSize
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.domain.ConnectionsTab
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.file
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.SidePanelMode
import app.notekea.core.notes.domain.SidePanelPreferences
import app.notekea.core.notes.domain.SidePanelSection
import app.notekea.core.notes.domain.VaultTree
import app.notekea.editor.domain.FakeEditorPreferencesStore
import app.notekea.shell.domain.EditorPane
import app.notekea.shell.domain.FakeWorkspaceStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.shell.domain.Workspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.io.encoding.Base64
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The pictures a workspace reads for its file tabs and folder cards. */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkspacePicturesViewModelTest : WorkspaceViewModelTestBase() {
    @Test
    fun `a file tab's picture is read, an unreadable one says so, and a folder card gets its note's first image`() =
        runWorkspaceTest {
            attachmentStore.contents[PLATE] = Base64.decode(source = PNG_BASE64)
            attachmentStore.contents[BROKEN] = "not a picture".encodeToByteArray()
            contents.texts[AREPAS] = "Intro\n![[plate.png]]"
            val viewModel = started()

            viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = PLATE))
            viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = BROKEN))
            viewModel.onIntent(intent = WorkspaceIntent.OnSplitRight)

            viewModel.onIntent(
                intent = WorkspaceIntent.OnCoverRequest(
                    path = AREPAS,
                    size = COVER_SIZE
                )
            )

            viewModel.onIntent(
                intent = WorkspaceIntent.OnCoverRequest(
                    path = WELCOME,
                    size = COVER_SIZE
                )
            )

            advanceUntilIdle()
            val state = viewModel.state.value

            assertEquals(
                expected = listOf(1, null, 1, null) to listOf(false, true),
                actual = listOf(
                    state.previews[PLATE]?.image?.width,
                    state.previews[BROKEN]?.image?.width,
                    state.covers[AREPAS]?.width,
                    state.covers[WELCOME]
                ) to listOf(PLATE, BROKEN).map {
                    state
                        .previews
                        .getValue(it)
                        .isUnreadable
                }
            )
        }

    @Test
    fun `only the newest folder card pictures are kept`() = runWorkspaceTest {
        val viewModel = started()

        val paths = (0..WorkspaceViewModel.MAX_COVERS).map { RelativePath(value = "Card $it.md") }

        paths.forEach {
            viewModel.onIntent(
                intent = WorkspaceIntent.OnCoverRequest(
                    path = it,
                    size = COVER_SIZE
                )
            )
        }

        advanceUntilIdle()

        assertEquals(
            expected = paths.drop(n = 1).toSet(),
            actual = viewModel.state.value.covers.keys
        )
    }

    @Test
    fun `a file's picture is let go once no tab holds the file`() = runWorkspaceTest {
        attachmentStore.contents[PLATE] = Base64.decode(source = PNG_BASE64)
        val viewModel = started()

        viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = PLATE))
        advanceUntilIdle()
        val read = viewModel.state.value.previews.keys
        viewModel.onIntent(intent = WorkspaceIntent.OnCloseActive)

        assertEquals(
            expected = setOf(PLATE) to emptySet(),
            actual = read to viewModel.state.value.previews.keys
        )
    }
}
