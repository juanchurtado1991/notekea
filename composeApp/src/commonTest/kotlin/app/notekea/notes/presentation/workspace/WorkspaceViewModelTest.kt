package app.notekea.notes.presentation.workspace

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.EditorPane
import app.notekea.notes.domain.FakeAttachmentStore
import app.notekea.notes.domain.FakeEditorPreferencesStore
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeVaultSettingsReader
import app.notekea.notes.domain.FakeWorkspaceStore
import app.notekea.notes.domain.GetNoteCoverUseCase
import app.notekea.notes.domain.GetSidePanelUseCase
import app.notekea.notes.domain.GetWorkspaceUseCase
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SaveSidePanelUseCase
import app.notekea.notes.domain.SidePanelMode
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelSection
import app.notekea.notes.domain.VaultTree
import app.notekea.notes.domain.Workspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals

/** The editor's tabs and panes (`ROADMAP.md` 1.11) over real use cases and in-memory stores. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalEncodingApi::class)
class WorkspaceViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val workspaces = FakeWorkspaceStore()
    private val preferences = FakeEditorPreferencesStore()
    private val contents = FakeNoteContentStore()
    private val attachmentStore = FakeAttachmentStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val tree = listOf(AREPAS, WELCOME).fold(initial = VaultTree.EMPTY) { tree, path ->
        tree.withNote(note = NoteEntry(file = file(path = path.value), metadata = NoteMetadata.NONE))
    }.withAttachment(file = file(path = PLATE.value)).withAttachment(file = file(path = BROKEN.value))

    @Test
    fun `opened entries get tabs, the second pane comes and goes, and every change is saved`() = runWorkspaceTest {
        val viewModel = started()

        listOf(AREPAS, WELCOME, PLATE).forEach { viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = it)) }
        viewModel.onIntent(intent = WorkspaceIntent.OnSplitRight)
        val split = viewModel.state.value.workspace
        viewModel.onIntent(intent = WorkspaceIntent.OnTabClose(pane = 1, tab = 0))
        advanceUntilIdle()

        assertEquals(
            expected = Triple(
                listOf(listOf(AREPAS, WELCOME), listOf(PLATE)),
                Workspace(panes = listOf(EditorPane(tabs = listOf(AREPAS, WELCOME), active = 1)), activePane = 0),
                viewModel.state.value.workspace
            ),
            actual = Triple(split.panes.map { it.tabs }, viewModel.state.value.workspace, workspaces.workspaces[VAULT])
        )
    }

    @Test
    fun `the last run's tabs come back once the files are known, without those of files that are gone`() =
        runWorkspaceTest {
            workspaces.workspaces[VAULT] = Workspace.EMPTY.open(path = AREPAS).open(path = GONE).open(path = PLATE)
                .splitRight()
            val viewModel = started(isLoaded = false)
            val beforeLoad = viewModel.state.value.workspace

            viewModel.onIntent(intent = WorkspaceIntent.OnTreeChange(tree = tree, isLoaded = true))
            advanceUntilIdle()

            assertEquals(
                expected = Triple(
                    Workspace.EMPTY,
                    listOf(listOf(AREPAS), listOf(PLATE)),
                    true
                ),
                actual = Triple(
                    beforeLoad,
                    viewModel.state.value.workspace.panes.map { it.tabs },
                    viewModel.state.value.isRestored
                )
            )
        }

    @Test
    fun `renames move tabs, deletions close them, and shortcuts cycle, jump and close the active one`() =
        runWorkspaceTest {
            val viewModel = started()
            listOf(AREPAS, WELCOME, PLATE).forEach { viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = it)) }

            viewModel.onIntent(intent = WorkspaceIntent.OnRenamed(from = RECIPES, to = RelativePath(value = "Food")))
            viewModel.onIntent(intent = WorkspaceIntent.OnDeleted(path = WELCOME))
            viewModel.onIntent(intent = WorkspaceIntent.OnCycle(step = 1))
            val cycled = viewModel.state.value.workspace.activePath
            viewModel.onIntent(intent = WorkspaceIntent.OnJump(number = 9))
            viewModel.onIntent(intent = WorkspaceIntent.OnCloseActive)

            assertEquals(
                expected = RelativePath(value = "Food/Arepas.md") to listOf(RelativePath(value = "Food/Arepas.md")),
                actual = cycled to viewModel.state.value.workspace.panes.single().tabs
            )
        }

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
            viewModel.onIntent(intent = WorkspaceIntent.OnCoverRequest(path = AREPAS))
            viewModel.onIntent(intent = WorkspaceIntent.OnCoverRequest(path = WELCOME))
            advanceUntilIdle()
            val state = viewModel.state.value

            assertEquals(
                expected = listOf(1, null, 1, null) to listOf(false, true),
                actual = listOf(
                    state.previews[PLATE]?.image?.width,
                    state.previews[BROKEN]?.image?.width,
                    state.covers[AREPAS]?.width,
                    state.covers[WELCOME]
                ) to listOf(PLATE, BROKEN).map { state.previews.getValue(it).isUnreadable }
            )
        }

    @Test
    fun `the side panel is shown at first, hides, and the outline shortcut shows it again and focuses it`() =
        runWorkspaceTest {
            val viewModel = started()
            val effects = mutableListOf<WorkspaceEffect>()
            backgroundScope.launch { viewModel.effects.collect { effects += it } }
            runCurrent()
            val first = viewModel.state.value.sidePanel

            viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelToggle)
            viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelResize(widthDp = 2_000))
            advanceUntilIdle()
            val hidden = preferences.sidePanel
            viewModel.onIntent(intent = WorkspaceIntent.OnOutlineFocusRequest)
            advanceUntilIdle()
            runCurrent()

            assertEquals(
                expected = listOf(
                    SidePanelPreferences(widthDp = 340, isVisible = true),
                    SidePanelPreferences(widthDp = 560, isVisible = false),
                    SidePanelPreferences(widthDp = 560, isVisible = true)
                ) to listOf<WorkspaceEffect>(WorkspaceEffect.FocusOutline),
                actual = listOf(first, hidden, preferences.sidePanel) to effects.toList()
            )
        }

    @Test
    fun `a section folds, Chat is chosen, and the outline shortcut comes back to an open outline`() = runWorkspaceTest {
        val viewModel = started()
        runCurrent()
        viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelSectionToggle(section = SidePanelSection.NOTE_INFO))
        viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelSectionToggle(section = SidePanelSection.OUTLINE))
        viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelModeSelect(mode = SidePanelMode.CHAT))
        advanceUntilIdle()
        val chosen = preferences.sidePanel
        viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelSectionToggle(section = SidePanelSection.NOTE_INFO))
        viewModel.onIntent(intent = WorkspaceIntent.OnOutlineFocusRequest)
        advanceUntilIdle()

        assertEquals(
            expected = SidePanelPreferences(
                mode = SidePanelMode.CHAT,
                foldedSections = setOf(SidePanelSection.NOTE_INFO, SidePanelSection.OUTLINE)
            ) to SidePanelPreferences(),
            actual = chosen to preferences.sidePanel
        )
    }

    private fun TestScope.started(
        isLoaded: Boolean = true
    ): WorkspaceViewModel {
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
        if (isLoaded) viewModel.onIntent(intent = WorkspaceIntent.OnTreeChange(tree = tree, isLoaded = true))
        advanceUntilIdle()
        runCurrent()
        return viewModel
    }

    private fun runWorkspaceTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { body() }

    companion object {
        private const val PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
        private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        private val BROKEN = RelativePath(value = "attachments/broken.png")
        private val GONE = RelativePath(value = "Gone.md")
        private val PLATE = RelativePath(value = "attachments/plate.png")
        private val RECIPES = RelativePath(value = "Recipes")
        private val WELCOME = RelativePath(value = "Welcome.md")
    }
}
