package app.notekea.shell.presentation

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.shell.domain.EditorPane
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.editor.domain.FakeEditorPreferencesStore
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.shell.domain.FakeWorkspaceStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.file
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.core.notes.domain.VaultTree
import app.notekea.shell.domain.Workspace
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.editor.presentation.EditorViewModels
import app.notekea.search.presentation.switcher.QuickSwitcherEffect
import app.notekea.shell.presentation.WorkspaceIntent
import app.notekea.shell.presentation.WorkspaceTools
import app.notekea.shell.presentation.WorkspaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * With two panes and the note open in the one that is not active, what follows opening it (a mention's selection, the
 * palette's keyboard) reaches that note's pane, not the pane that was active when it was asked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaneEditorsTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val workspaces = FakeWorkspaceStore()
    private val editors by lazy { List(size = Workspace.MAX_PANES) { fixture.create() } }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        listOf(AREPAS, WELCOME).forEach { fixture.store.texts[it] = TEXT }
        listOf(AREPAS, WELCOME).forEach { fixture.scanner.addNote(file = file(path = it.value)) }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a mention opened in the other pane is selected there`() = runPanesTest { workspace ->
        val focused = collectFocus(editor = editors[1])
        val actions = connectionActions(workspace = workspace, editors = panes(workspace)) { setOf(AREPAS, WELCOME) }

        actions.onReveal(WELCOME, 2..5)
        runCurrent()

        assertEquals(
            expected = Triple(1, TextRange(start = 2, end = 6), 1),
            actual = Triple(workspace.state.value.workspace.activePane, editors[1].buffer.selection, focused.size)
        )
    }

    @Test
    fun `a note chosen in the palette takes the keyboard in the pane that shows it`() = runPanesTest { workspace ->
        val focused = collectFocus(editor = editors[1])

        followSwitcherEffect(
            effect = QuickSwitcherEffect.OpenNote(path = WELCOME),
            onReveal = {},
            editors = panes(workspace),
            notes = setOf(AREPAS, WELCOME)
        )
        runCurrent()

        assertEquals(expected = 1, actual = focused.size)
    }

    @Test
    fun `a new note goes to the active pane`() = runPanesTest { workspace ->
        assertEquals(expected = editors[0], actual = panes(workspace).of(path = NEW))
    }

    private fun panes(
        workspace: WorkspaceViewModel
    ) = PaneEditors(workspace = workspace, editors = editors)

    private fun TestScope.collectFocus(
        editor: EditorViewModel
    ): List<EditorEffect> {
        val effects = mutableListOf<EditorEffect>()
        backgroundScope.launch { editor.effects.collect { if (it == EditorEffect.FocusText) effects += it } }
        runCurrent()
        return effects
    }

    /** Two panes, [AREPAS] in the active one and [WELCOME] in the other, each open in its editor. */
    private fun runPanesTest(
        body: suspend TestScope.(WorkspaceViewModel) -> Unit
    ) = runTest(context = testDispatcher) {
        workspaces.workspaces[VAULT] = Workspace(
            panes = listOf(
                EditorPane(tabs = listOf(AREPAS), active = 0),
                EditorPane(tabs = listOf(WELCOME), active = 0)
            ),
            activePane = 0
        )
        val workspace = workspace()
        editors[0].onIntent(intent = EditorIntent.OnOpen(path = AREPAS))
        editors[1].onIntent(intent = EditorIntent.OnOpen(path = WELCOME))
        runCurrent()
        try {
            body(workspace)
        } finally {
            editors.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.workspace(): WorkspaceViewModel {
        val preferences = FakeEditorPreferencesStore()
        val contents = FakeNoteContentStore()
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
                attachmentStore = FakeAttachmentStore(),
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
            )
        )
        val tree = listOf(AREPAS, WELCOME).fold(initial = VaultTree.EMPTY) { tree, path ->
            tree.withNote(note = NoteEntry(file = file(path = path.value), metadata = NoteMetadata.NONE))
        }
        viewModel.onIntent(intent = WorkspaceIntent.OnTreeChange(tree = tree, isLoaded = true))
        runCurrent()
        return viewModel
    }

    companion object {
        private const val TEXT = "Some body text"
        private val AREPAS = RelativePath(value = "Arepas.md")
        private val NEW = RelativePath(value = "New.md")
        private val WELCOME = RelativePath(value = "Welcome.md")
    }
}
