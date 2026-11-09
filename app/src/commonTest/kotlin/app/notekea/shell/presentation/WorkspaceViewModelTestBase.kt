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

/** What the tests of the editor's tabs and panes share: real use cases over in-memory stores. */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class WorkspaceViewModelTestBase {
    protected val testDispatcher = StandardTestDispatcher()
    protected val workspaces = FakeWorkspaceStore()
    protected val preferences = FakeEditorPreferencesStore()
    protected val contents = FakeNoteContentStore()
    protected val attachmentStore = FakeAttachmentStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    protected val tree = listOf(AREPAS, WELCOME)
        .fold(initial = VaultTree.EMPTY) { tree, path ->
            tree.withNote(
                note = NoteEntry(
                    file = file(path = path.value),
                    metadata = NoteMetadata.NONE
                )
            )
        }
        .withAttachment(file = file(path = PLATE.value))
        .withAttachment(file = file(path = BROKEN.value))

    protected fun TestScope.started(isLoaded: Boolean = true): WorkspaceViewModel {
        val viewModel = WorkspaceViewModel(
            vaultPath = VAULT,
            tools = WorkspaceTools(
                getWorkspace = GetWorkspaceUseCase(workspaceStore = workspaces),
                workspaceStore = workspaces,
                getSidePanel = GetSidePanelUseCase(editorPreferencesStore = preferences),
                saveSidePanel = SaveSidePanelUseCase(editorPreferencesStore = preferences),
                getNoteCover = GetNoteCoverUseCase(
                    noteContentStore = contents,
                    vaultSettingsReader = FakeVaultSettingsReader()
                ),
                attachmentStore = attachmentStore,
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
            )
        )

        if (isLoaded) {
            viewModel.onIntent(
                intent = WorkspaceIntent.OnTreeChange(
                    tree = tree,
                    isLoaded = true
                )
            )
        }

        advanceUntilIdle()
        runCurrent()

        return viewModel
    }

    protected fun runWorkspaceTest(body: suspend TestScope.() -> Unit) = runTest(
        context = testDispatcher
    ) { body() }

    protected companion object {
        const val SLOW_SAVE_MILLIS = 1_000L
        val COVER_SIZE = IntSize(
            width = 336,
            height = 252
        )
        const val PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val BROKEN = RelativePath(value = "attachments/broken.png")
        val GONE = RelativePath(value = "Gone.md")
        val PLATE = RelativePath(value = "attachments/plate.png")
        val RECIPES = RelativePath(value = "Recipes")
        val WELCOME = RelativePath(value = "Welcome.md")
    }
}
