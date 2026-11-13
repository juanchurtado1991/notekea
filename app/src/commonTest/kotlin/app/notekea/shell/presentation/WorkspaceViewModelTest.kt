package app.notekea.shell.presentation

import androidx.compose.ui.unit.IntSize
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.file
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultTree
import app.notekea.core.notes.domain.sidepanel.ConnectionsTab
import app.notekea.core.notes.domain.sidepanel.SidePanelMode
import app.notekea.core.notes.domain.sidepanel.SidePanelPreferences
import app.notekea.core.notes.domain.sidepanel.SidePanelSection
import app.notekea.editor.domain.FakeEditorPreferencesStore
import app.notekea.shell.domain.EditorPane
import app.notekea.shell.domain.FakeWorkspaceStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.workspace.WorkspaceEffect
import app.notekea.shell.presentation.workspace.WorkspaceIntent
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

/** The editor's tabs and panes over real use cases and in-memory stores. */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceViewModelTest : WorkspaceViewModelTestBase() {
    @Test
    fun `tabs that cannot be kept tell the window`() = runWorkspaceTest {
        val viewModel = started()
        workspaces.failure = IllegalStateException("bug")

        viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = RelativePath(value = "Bug.md")))

        assertEquals(
            expected = WorkspaceEffect.UnexpectedError,
            actual = viewModel.effects.first()
        )
    }

    @Test
    fun `a tab opens at once while the disk is still busy, and is saved when the disk answers`() =
        runWorkspaceTest {
            val viewModel = started()
            val onDisk = workspaces.workspaces[VAULT]
            workspaces.gate.close()

            viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = AREPAS))
            advanceUntilIdle()

            assertEquals(
                listOf(AREPAS),
                viewModel
                    .state
                    .value
                    .workspace
                    .panes
                    .flatMap { it.tabs }
            )

            assertEquals(onDisk, workspaces.workspaces[VAULT])

            workspaces.gate.open()
            advanceUntilIdle()

            assertEquals(viewModel.state.value.workspace, workspaces.workspaces[VAULT])
        }

    @Test
    fun `opened entries get tabs, the second pane comes and goes, and every change is saved`() =
        runWorkspaceTest {
            val viewModel = started()

            listOf(AREPAS, WELCOME, PLATE).forEach {
                viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = it))
            }

            viewModel.onIntent(intent = WorkspaceIntent.OnSplitRight)
            val split = viewModel.state.value.workspace

            viewModel.onIntent(
                intent = WorkspaceIntent.OnTabClose(
                    pane = 1,
                    tab = 0
                )
            )

            advanceUntilIdle()

            assertEquals(
                expected = Triple(
                    listOf(listOf(AREPAS, WELCOME), listOf(PLATE)),
                    Workspace(
                        panes = listOf(
                            EditorPane(
                                tabs = listOf(AREPAS, WELCOME),
                                active = 1
                            )
                        ),
                        activePane = 0
                    ),
                    viewModel.state.value.workspace
                ),
                actual = Triple(
                    split.panes.map { it.tabs },
                    viewModel.state.value.workspace,
                    workspaces.workspaces[VAULT]
                )
            )
        }

    @Test
    fun `the cache alone does not restore the tabs, so a note deleted while Notekea was closed stays closed`() =
        runWorkspaceTest {
            // The 1.11-10 Auto-Test case: the cache still lists the note deleted while the app was closed.
            workspaces.workspaces[VAULT] = Workspace.EMPTY.open(path = AREPAS).open(path = GONE)
            val viewModel = started(isLoaded = false)

            val gone = NoteEntry(
                file = file(path = GONE.value),
                metadata = NoteMetadata.NONE
            )

            val cached = tree.withNote(note = gone).copy(isFromCache = true)

            viewModel.onIntent(
                intent = WorkspaceIntent.OnTreeChange(
                    tree = cached,
                    isLoaded = true
                )
            )

            advanceUntilIdle()
            val fromCache = viewModel.state.value.isRestored

            viewModel.onIntent(
                intent = WorkspaceIntent.OnTreeChange(
                    tree = tree,
                    isLoaded = true
                )
            )

            advanceUntilIdle()

            assertEquals(
                expected = false to listOf(listOf(AREPAS)),
                actual = fromCache to viewModel
                    .state
                    .value
                    .workspace
                    .panes
                    .map { it.tabs }
            )
        }

    @Test
    fun `the last run's tabs come back once the files are known, without those of files that are gone`() =
        runWorkspaceTest {
            workspaces.workspaces[VAULT] = Workspace.EMPTY
                .open(path = AREPAS)
                .open(path = GONE)
                .open(path = PLATE)
                .splitRight()

            val viewModel = started(isLoaded = false)
            val beforeLoad = viewModel.state.value.workspace

            viewModel.onIntent(
                intent = WorkspaceIntent.OnTreeChange(
                    tree = tree,
                    isLoaded = true
                )
            )

            advanceUntilIdle()

            assertEquals(
                expected = Triple(
                    Workspace.EMPTY,
                    listOf(listOf(AREPAS), listOf(PLATE)),
                    true
                ),
                actual = Triple(
                    beforeLoad,
                    viewModel
                        .state
                        .value
                        .workspace
                        .panes
                        .map { it.tabs },
                    viewModel.state.value.isRestored
                )
            )
        }

    @Test
    fun `renames move tabs, deletions close them, and shortcuts cycle, jump and close the active one`() =
        runWorkspaceTest {
            val viewModel = started()

            listOf(AREPAS, WELCOME, PLATE).forEach {
                viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = it))
            }

            viewModel.onIntent(
                intent = WorkspaceIntent.OnRenamed(
                    from = RECIPES,
                    to = RelativePath(value = "Food")
                )
            )

            viewModel.onIntent(intent = WorkspaceIntent.OnDeleted(path = WELCOME))
            viewModel.onIntent(intent = WorkspaceIntent.OnCycle(step = 1))
            val cycled = viewModel.state.value.workspace.activePath
            viewModel.onIntent(intent = WorkspaceIntent.OnJump(number = 9))
            viewModel.onIntent(intent = WorkspaceIntent.OnCloseActive)

            assertEquals(
                expected =
                RelativePath(value = "Food/Arepas.md") to
                    listOf(RelativePath(value = "Food/Arepas.md")),
                actual = cycled to viewModel
                    .state
                    .value
                    .workspace
                    .panes
                    .single()
                    .tabs
            )
        }

    @Test
    fun `the side panel is shown at first, hides, and the outline shortcut shows it again and focuses it`() =
        runWorkspaceTest {
            val viewModel = started()
            val effects = mutableListOf<WorkspaceEffect>()

            backgroundScope.launch {
                viewModel.effects.collect { effects += it }
            }

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
                    SidePanelPreferences(
                        widthDp = 340,
                        isVisible = true
                    ),
                    SidePanelPreferences(
                        widthDp = 560,
                        isVisible = false
                    ),
                    SidePanelPreferences(
                        widthDp = 560,
                        isVisible = true
                    )
                ) to listOf<WorkspaceEffect>(WorkspaceEffect.FocusOutline),
                actual = listOf(first, hidden, preferences.sidePanel) to effects.toList()
            )
        }

    @Test
    fun `a section folds, Chat and a Connections tab are chosen, the outline shortcut opens it`() =
        runWorkspaceTest {
            val viewModel = started()
            runCurrent()

            viewModel.onIntent(
                intent = WorkspaceIntent.OnSidePanelSectionToggle(
                    section = SidePanelSection.NOTE_INFO
                )
            )

            viewModel.onIntent(
                intent = WorkspaceIntent.OnSidePanelSectionToggle(
                    section = SidePanelSection.OUTLINE
                )
            )

            viewModel.onIntent(
                intent = WorkspaceIntent.OnSidePanelModeSelect(mode = SidePanelMode.CHAT)
            )

            viewModel.onIntent(
                intent = WorkspaceIntent.OnConnectionsTabSelect(tab = ConnectionsTab.LINKED)
            )

            advanceUntilIdle()
            val chosen = preferences.sidePanel

            viewModel.onIntent(
                intent = WorkspaceIntent.OnSidePanelSectionToggle(
                    section = SidePanelSection.NOTE_INFO
                )
            )

            viewModel.onIntent(intent = WorkspaceIntent.OnOutlineFocusRequest)
            advanceUntilIdle()

            assertEquals(
                expected = SidePanelPreferences(
                    mode = SidePanelMode.CHAT,
                    foldedSections = setOf(SidePanelSection.NOTE_INFO, SidePanelSection.OUTLINE),
                    connections = ConnectionsTab.LINKED
                ) to SidePanelPreferences(connections = ConnectionsTab.LINKED),
                actual = chosen to preferences.sidePanel
            )
        }

    @Test
    fun `the chat shortcut shows the hidden panel on the chat and gives its field the keyboard`() =
        runWorkspaceTest {
            val viewModel = started()
            val effects = mutableListOf<WorkspaceEffect>()

            backgroundScope.launch {
                viewModel.effects.collect { effects += it }
            }

            runCurrent()
            viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelToggle)
            advanceUntilIdle()
            viewModel.onIntent(intent = WorkspaceIntent.OnChatFocusRequest)
            advanceUntilIdle()
            runCurrent()

            assertEquals(
                expected = SidePanelPreferences(
                    isVisible = true,
                    mode = SidePanelMode.CHAT
                ) to listOf<WorkspaceEffect>(WorkspaceEffect.FocusChat),
                actual = preferences.sidePanel to effects.toList()
            )
        }

    @Test
    fun `a slow save of the panel never lands after a newer one`() = runWorkspaceTest {
        val viewModel = started()
        preferences.sidePanelSaveMillis += listOf(SLOW_SAVE_MILLIS, 0L)

        viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelResize(widthDp = 300))
        runCurrent()
        viewModel.onIntent(intent = WorkspaceIntent.OnSidePanelResize(widthDp = 360))
        advanceUntilIdle()

        assertEquals(
            expected = 360,
            actual = preferences.sidePanel.widthDp
        )
    }

    @Test
    fun `a slow save of the tabs never lands after a newer one`() = runWorkspaceTest {
        val viewModel = started()
        workspaces.writeMillis += listOf(SLOW_SAVE_MILLIS, 0L)

        viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = AREPAS))
        runCurrent()
        viewModel.onIntent(intent = WorkspaceIntent.OnOpen(path = WELCOME))
        advanceUntilIdle()

        assertEquals(
            expected = viewModel.state.value.workspace,
            actual = workspaces.workspaces[VAULT]
        )
    }
}
