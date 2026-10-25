package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.DeleteEntryUseCase
import app.notekea.notes.domain.FakeFileRevealer
import app.notekea.notes.domain.FakeFileWatcher
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeNoteFileOperations
import app.notekea.notes.domain.FakeNoteMetadataCache
import app.notekea.notes.domain.FakeNoteMetadataReader
import app.notekea.notes.domain.FakeNoteScanner
import app.notekea.notes.domain.FakeTrash
import app.notekea.notes.domain.FakeTreePreferencesStore
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.FollowLinkUseCase
import app.notekea.notes.domain.GetTreePreferencesUseCase
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.RevealEntryUseCase
import app.notekea.notes.domain.SaveTreePreferencesUseCase
import app.notekea.notes.domain.TrashLocation
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.TreeRow
import app.notekea.notes.domain.TreeSort
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.notekea.notes.domain.LinkUpdateResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VaultTreeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val scanner = FakeNoteScanner()
    private val operations = FakeNoteFileOperations()
    private val trash = FakeTrash()
    private val revealer = FakeFileRevealer()
    private val preferences = FakeTreePreferencesStore()
    private val contents = FakeNoteContentStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        listOf(WELCOME, ROADMAP).forEach { scanner.addNote(file = file(path = it)) }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the vault opens with its folders as they were left`() = runTreeTest {
        preferences.stored = TreePreferences(expandedFolders = setOf(path(PROJECTS)), sort = TreeSort.MODIFIED)

        val state = startedViewModel().state.value

        assertEquals(
            expected = listOf(PROJECTS, ROADMAP, WELCOME),
            actual = state.rows.map { it.path.value }
        )
        assertEquals(
            expected = false to TreeSort.MODIFIED,
            actual = state.isLoading to state.sort
        )
        assertEquals(
            expected = emptyList(),
            actual = preferences.saved
        )
    }

    @Test
    fun `opening a folder is remembered`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(PROJECTS)))
        advanceUntilIdle()

        assertEquals(
            expected = listOf(TreePreferences(expandedFolders = setOf(path(PROJECTS)), sort = TreeSort.NAME)),
            actual = preferences.saved
        )
        assertTrue(
            actual = viewModel.state.value.rows
                .any { it is TreeRow.Note && it.path == path(ROADMAP) }
        )
    }

    @Test
    fun `a vault that cannot be read shows the error until a retry succeeds`() = runTreeTest {
        scanner.failure = NotesError.PermissionDenied(path = VAULT)
        val viewModel = startedViewModel()
        val failed = viewModel.state.value

        scanner.failure = null
        viewModel.onIntent(intent = VaultTreeIntent.OnRetryClick)
        advanceUntilIdle()

        assertEquals(
            expected = NotesError.PermissionDenied(path = VAULT),
            actual = failed.loadError
        )
        assertEquals(
            expected = null to 2,
            actual = viewModel.state.value.loadError to viewModel.state.value.rows.size
        )
    }

    @Test
    fun `a new note goes to the selected folder and opens for renaming`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(ROADMAP)))

        viewModel.onIntent(intent = VaultTreeIntent.OnNewNoteClick(baseName = UNTITLED))
        advanceUntilIdle()

        assertEquals(
            expected = RenameDraft(path = path("$PROJECTS/Untitled.md"), text = UNTITLED),
            actual = viewModel.state.value.renaming
        )
        assertTrue(actual = path("$PROJECTS/Untitled.md") in operations.existing)
    }

    @Test
    fun `clearing the selection sends a new folder to the vault root`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(ROADMAP)))

        viewModel.onIntent(intent = VaultTreeIntent.OnSelectionClear)
        viewModel.onIntent(intent = VaultTreeIntent.OnNewFolderClick(baseName = UNTITLED))
        advanceUntilIdle()

        assertTrue(actual = path(UNTITLED) in operations.existing)
    }

    @Test
    fun `choosing a sort reorders the notes and is remembered`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnSortChange(sort = TreeSort.MODIFIED))
        viewModel.onIntent(intent = VaultTreeIntent.OnSortChange(sort = TreeSort.MODIFIED))
        advanceUntilIdle()

        val modified = TreePreferences(expandedFolders = emptySet(), sort = TreeSort.MODIFIED)
        assertEquals(
            expected = TreeSort.MODIFIED to listOf(modified),
            actual = viewModel.state.value.sort to preferences.saved
        )
    }

    @Test
    fun `confirming a rename twice at once moves the entry once`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "Start here"))

        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(path(WELCOME) to path("Start here.md")),
            actual = operations.moves
        )
        assertEquals(
            expected = path("Start here.md") to null,
            actual = viewModel.state.value.selectedPath to viewModel.state.value.renaming
        )
    }

    @Test
    fun `renaming a linked note updates the notes that link to it and says so`() = runTreeTest {
        contents.texts[path("Start here.md")] = ""
        contents.texts[path(ROADMAP)] = "Begin with [[Welcome]]."
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "Start here"))

        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = "Begin with [[Start here]]." to TreeNotice.LinksUpdated(updatedCount = 1, skippedCount = 0),
            actual = contents.texts.getValue(path(ROADMAP)) to viewModel.state.value.notice
        )
    }

    @Test
    fun `renaming a note nothing links to changes no other note and says nothing`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "No links here."
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "Start here"))

        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = "No links here." to null,
            actual = contents.texts.getValue(path(ROADMAP)) to viewModel.state.value.notice
        )
    }

    @Test
    fun `a rename in the tree tells the editor where the note went`() = runTreeTest {
        val viewModel = startedViewModel()
        val effects = mutableListOf<VaultTreeEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        runCurrent()
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "Start here"))

        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()
        runCurrent()

        assertEquals(
            expected = listOf<VaultTreeEffect>(
                VaultTreeEffect.EntryRenamed(from = path(WELCOME), to = path("Start here.md"))
            ),
            actual = effects
        )
    }

    @Test
    fun `a rename from the editor selects the note at its new path and reports the links`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(WELCOME)))

        viewModel.onIntent(
            intent = VaultTreeIntent.OnEntryRenamed(
                from = path(WELCOME),
                to = path("Start here.md"),
                links = LinkUpdateResult(updatedCount = 2, skipped = emptyList())
            )
        )
        advanceUntilIdle()

        assertEquals(
            expected = path("Start here.md") to TreeNotice.LinksUpdated(updatedCount = 2, skippedCount = 0),
            actual = viewModel.state.value.selectedPath to viewModel.state.value.notice
        )
        assertTrue(actual = path("Start here.md") in viewModel.state.value.tree.notes)
    }

    @Test
    fun `a clicked link selects its note and opens the folders above it`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(
            intent = VaultTreeIntent.OnFollowLink(source = path(WELCOME), target = "roadmap", kind = LinkKind.WIKI)
        )
        advanceUntilIdle()

        assertEquals(
            expected = path(ROADMAP) to setOf(path(PROJECTS)),
            actual = viewModel.state.value.selectedPath to viewModel.state.value.expandedFolders
        )
    }

    @Test
    fun `a wiki link to a missing note creates it at the root and selects it`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(
            intent = VaultTreeIntent.OnFollowLink(source = path(WELCOME), target = "Ideas", kind = LinkKind.WIKI)
        )
        advanceUntilIdle()

        assertEquals(
            expected = path("Ideas.md") to true,
            actual = viewModel.state.value.selectedPath to (path("Ideas.md") in viewModel.state.value.tree.notes)
        )
    }

    @Test
    fun `a markdown link to a missing file shows why nothing opened`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(
            intent = VaultTreeIntent.OnFollowLink(
                source = path(WELCOME),
                target = "Missing.md",
                kind = LinkKind.MARKDOWN_ENCODED
            )
        )
        advanceUntilIdle()

        assertEquals(
            expected = NotesError.EntryMissing(path = "Missing.md"),
            actual = viewModel.state.value.actionError
        )
    }

    @Test
    fun `a refused name stays in the field with its reason`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "a/b"))

        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = RenameDraft(path = path(WELCOME), text = "a/b", error = NotesError.InvalidName),
            actual = viewModel.state.value.renaming
        )
    }

    @Test
    fun `deleting asks first, then trashes the entry and tells where it went`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteRequest())
        val asking = viewModel.state.value.pendingDelete

        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(path(WELCOME), path(WELCOME)),
            actual = listOf(asking) + trash.trashed
        )
        assertEquals(
            expected = TreeNotice.MovedToVaultTrash(name = "Welcome"),
            actual = viewModel.state.value.notice
        )
    }

    @Test
    fun `a cancelled delete trashes nothing`() = runTreeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteCancel)
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = emptyList(),
            actual = trash.trashed
        )
        assertEquals(
            expected = TrashLocation.VAULT_TRASH_FOLDER,
            actual = (trash.outcome as Outcome.Success).value
        )
    }

    @Test
    fun `a file manager that cannot open shows a dismissible error`() = runTreeTest {
        revealer.outcome = Outcome.Failure(error = NotesError.RevealFailed(path = WELCOME))
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnRevealRequest(path = path(WELCOME)))
        advanceUntilIdle()
        val failed = viewModel.state.value.actionError
        viewModel.onIntent(intent = VaultTreeIntent.OnErrorDismiss)

        assertEquals(
            expected = NotesError.RevealFailed(path = WELCOME) to null,
            actual = failed to viewModel.state.value.actionError
        )
    }

    @Test
    fun `the sidebar can be hidden and resized within bounds, and both are remembered`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
        viewModel.onIntent(intent = VaultTreeIntent.OnSidebarResize(widthDp = 120))
        advanceUntilIdle()

        assertEquals(
            expected = TreePreferences(
                expandedFolders = emptySet(),
                sort = TreeSort.NAME,
                sidebarWidthDp = TreePreferences.MIN_SIDEBAR_WIDTH_DP,
                isSidebarCollapsed = true
            ),
            actual = preferences.saved.last()
        )
    }

    @Test
    fun `keyboard moves select rows in order`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnSelectNext)
        viewModel.onIntent(intent = VaultTreeIntent.OnExpandSelected)
        viewModel.onIntent(intent = VaultTreeIntent.OnExpandSelected)
        advanceUntilIdle()

        assertEquals(
            expected = path(ROADMAP),
            actual = viewModel.state.value.selectedPath
        )
    }

    @Test
    fun `an unexpected failure ends loading with the generic error`() = runTreeTest {
        preferences.failure = IllegalStateException(BUG)

        val state = startedViewModel().state.value

        assertEquals(
            expected = false to true,
            actual = state.isLoading to state.hasUnexpectedError
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    private fun runTreeTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    private fun TestScope.startedViewModel(): VaultTreeViewModel {
        val viewModel = VaultTreeViewModel(
            vaultPath = VAULT,
            platformConventions = PreviewPlatformConventions.linux,
            observeVaultTree = ObserveVaultTreeUseCase(
                noteScanner = scanner,
                noteMetadataReader = FakeNoteMetadataReader(),
                fileWatcher = FakeFileWatcher(),
                noteMetadataCache = FakeNoteMetadataCache()
            ),
            flattenTree = FlattenTreeUseCase(naturalNameComparator = NaturalNameComparator()),
            createNote = CreateNoteUseCase(noteFileOperations = operations),
            createFolder = CreateFolderUseCase(noteFileOperations = operations),
            renameEntry = RenameEntryUseCase(
                noteFileOperations = operations,
                validateFileName = ValidateFileNameUseCase()
            ),
            deleteEntry = DeleteEntryUseCase(trash = trash),
            revealEntry = RevealEntryUseCase(fileRevealer = revealer),
            updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
                planLinkUpdates = PlanLinkUpdatesUseCase(
                    noteContentStore = contents,
                    linkFinder = JetBrainsMarkdownLinkFinder()
                ),
                applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = contents)
            ),
            followLink = FollowLinkUseCase(createNote = CreateNoteUseCase(noteFileOperations = operations)),
            getTreePreferences = GetTreePreferencesUseCase(treePreferencesStore = preferences),
            saveTreePreferences = SaveTreePreferencesUseCase(treePreferencesStore = preferences)
        )
        advanceUntilIdle()
        return viewModel
    }

    companion object {
        private const val BUG = "bug"
        private const val PROJECTS = "Projects"
        private const val ROADMAP = "Projects/Roadmap.md"
        private const val UNTITLED = "Untitled"
        private const val WELCOME = "Welcome.md"
    }
}
