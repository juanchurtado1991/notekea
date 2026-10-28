package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.FakeBookmarkStore
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.notes.domain.DailyNoteSettings
import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.FakeVaultSettingsReader
import app.notekea.notes.domain.GetDailyNoteSettingsUseCase
import app.notekea.notes.domain.OpenDailyNoteUseCase
import kotlin.time.Instant
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.ApplyTemplateUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.FakeFileOpener
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
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
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
    private val opener = FakeFileOpener()
    private val vaultSettings = FakeVaultSettingsReader()
    private val bookmarkStore = FakeBookmarkStore()

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
    fun `new note from template asks for a template for the selected folder, then makes the note to name`() =
        runTreeTest {
            contents.texts[path(MEETING_TEMPLATE)] = "# {{title}}\n{{date}}\n"
            val viewModel = startedViewModel()
            val effects = mutableListOf<VaultTreeEffect>()
            backgroundScope.launch { viewModel.effects.collect { effects += it } }
            viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(ROADMAP)))
            advanceUntilIdle()
            runCurrent()
            effects.clear()

            viewModel.onIntent(intent = VaultTreeIntent.OnNewNoteFromTemplateClick)
            viewModel.onIntent(intent = fromTemplate())
            advanceUntilIdle()
            runCurrent()
            contents.texts[path("$PROJECTS/Kickoff.md")] = ""
            viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "Kickoff"))
            viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
            advanceUntilIdle()

            assertEquals(
                expected = listOf(
                    VaultTreeEffect.ChooseTemplate(folder = path(PROJECTS)),
                    VaultTreeEffect.OpenEntry(path = path("$PROJECTS/Untitled.md")),
                    "# Kickoff\n2026-10-27\n"
                ),
                actual = effects.take(n = 2) + contents.texts[path("$PROJECTS/Kickoff.md")]
            )
        }

    @Test
    fun `a note from a template keeps its first name when the rename is cancelled, and is filled with it`() =
        runTreeTest {
            contents.texts[path(MEETING_TEMPLATE)] = "# {{title}}\n"
            val viewModel = startedViewModel()

            viewModel.onIntent(intent = fromTemplate())
            advanceUntilIdle()
            contents.texts[path("$PROJECTS/Untitled.md")] = ""
            viewModel.onIntent(intent = VaultTreeIntent.OnRenameCancel)
            advanceUntilIdle()

            assertEquals(
                expected = null to "# Untitled\n",
                actual = viewModel.state.value.renaming to contents.texts[path("$PROJECTS/Untitled.md")]
            )
        }

    @Test
    fun `bookmarks are toggled and reordered, kept for the vault, and come back with it`() = runTreeTest {
        val viewModel = startedViewModel()
        listOf(WELCOME, PROJECTS, ROADMAP, WELCOME, WELCOME).forEach {
            viewModel.onIntent(intent = BookmarkIntent.OnToggle(path = path(it)))
        }
        viewModel.onIntent(intent = BookmarkIntent.OnMove(from = 2, to = 0))
        advanceUntilIdle()

        assertEquals(
            expected = listOf(WELCOME, PROJECTS, ROADMAP).map(::path) to listOf(WELCOME, PROJECTS, ROADMAP).map(::path),
            actual = viewModel.state.value.bookmarks.paths to startedViewModel().state.value.bookmarks.paths
        )
    }

    @Test
    fun `a bookmark follows a rename in the tree or the editor, and goes with a deleted entry`() = runTreeTest {
        val viewModel = startedViewModel()
        listOf(WELCOME, ROADMAP).forEach { viewModel.onIntent(intent = BookmarkIntent.OnToggle(path = path(it))) }

        rename(viewModel = viewModel, from = PROJECTS, to = "Plans")
        viewModel.onIntent(
            intent = VaultTreeIntent.OnEntryRenamed(from = path(WELCOME), to = path("Start here.md"), links = null)
        )
        val renamed = viewModel.state.value.bookmarks.paths
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteRequest(path = path("Plans")))
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("Start here.md", "Plans/Roadmap.md").map(::path) to listOf(path("Start here.md")),
            actual = renamed to bookmarkStore.saved.getValue(VAULT).paths
        )
    }

    @Test
    fun `a clicked bookmark is selected in the tree with its folders open and opens in a tab`() = runTreeTest {
        val viewModel = startedViewModel()
        val effects = mutableListOf<VaultTreeEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(intent = BookmarkIntent.OnClick(path = path(ROADMAP)))
        advanceUntilIdle()
        runCurrent()

        assertEquals(
            expected = Triple(
                path(ROADMAP),
                true,
                listOf<VaultTreeEffect>(VaultTreeEffect.OpenEntry(path = path(ROADMAP)))
            ),
            actual = Triple(
                viewModel.state.value.selectedPath,
                path(PROJECTS) in viewModel.state.value.expandedFolders,
                effects.toList()
            )
        )
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
            expected =
            "Begin with [[Start here]]." to TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0),
            actual = contents.texts.getValue(path(ROADMAP)) to viewModel.state.value.notice
        )
    }

    @Test
    fun `a shown links notice never blocks the next rename, its links or its notice`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "Begin with [[Welcome]]."
        val viewModel = startedViewModel()
        rename(viewModel = viewModel, from = WELCOME, to = "Start here")
        val first = viewModel.state.value.notice

        rename(viewModel = viewModel, from = "Start here.md", to = "Begin")

        assertEquals(
            expected = Triple(
                TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0),
                "Begin with [[Begin]].",
                TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0)
            ),
            actual = Triple(first, contents.texts.getValue(path(ROADMAP)), viewModel.state.value.notice)
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
                links = LinkUpdateResult(updated = listOf(path(ROADMAP), path("Ideas/Plan.md")), skipped = emptyList())
            )
        )
        advanceUntilIdle()

        assertEquals(
            expected =
            path("Start here.md") to TreeNotice.LinksUpdated(notes = listOf("Plan", "Roadmap"), skippedCount = 0),
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
    fun `a note chosen in the quick switcher is selected with the folders above it open`() = runTreeTest {
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnRevealNote(path = path(ROADMAP)))
        advanceUntilIdle()

        assertEquals(
            expected = path(ROADMAP) to setOf(path(PROJECTS)),
            actual = viewModel.state.value.selectedPath to viewModel.state.value.expandedFolders
        )
    }

    @Test
    fun `a folder clicked in the status bar shows in a hidden sidebar, without opening a tab`() = runTreeTest {
        val viewModel = startedViewModel()
        val effects = mutableListOf<VaultTreeEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        viewModel.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
        runCurrent()

        viewModel.onIntent(intent = VaultTreeIntent.OnRevealInTree(path = path(PROJECTS)))
        advanceUntilIdle()

        assertEquals(
            expected = Triple<RelativePath?, Boolean, List<VaultTreeEffect>>(path(PROJECTS), false, emptyList()),
            actual = Triple(
                viewModel.state.value.selectedPath,
                viewModel.state.value.isSidebarCollapsed,
                effects.toList()
            )
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

    @Test
    fun `a click, Mod+click or Enter opens a note, file or folder in a tab, arrows only select`() = runTreeTest {
        scanner.addAttachment(file = file(path = PLATE))
        scanner.addNote(file = file(path = WELCOME))
        val viewModel = startedViewModel()
        val effects = mutableListOf<VaultTreeEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        runCurrent()

        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(PLATE)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path("attachments")))
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(WELCOME), inBackground = true))
        viewModel.onIntent(intent = VaultTreeIntent.OnSelectPrevious)
        viewModel.onIntent(intent = VaultTreeIntent.OnOpenSelected)
        advanceUntilIdle()

        assertEquals(
            expected = listOf<VaultTreeEffect>(
                VaultTreeEffect.OpenEntry(path = path(PLATE)),
                VaultTreeEffect.OpenEntry(path = path("attachments")),
                VaultTreeEffect.OpenEntry(path = path(WELCOME), inBackground = true),
                VaultTreeEffect.OpenEntry(path = viewModel.state.value.selectedPath!!)
            ),
            actual = effects.toList()
        )
    }

    @Test
    fun `a note or folder created while the sidebar is hidden shows it, with the new name ready to type`() =
        runTreeTest {
            val viewModel = startedViewModel()
            viewModel.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
            val hidden = viewModel.state.value.isSidebarCollapsed

            viewModel.onIntent(intent = VaultTreeIntent.OnNewFolderClick(baseName = "Untitled folder"))
            advanceUntilIdle()

            assertEquals(
                expected = Triple(true, false, path("Untitled folder")),
                actual = Triple(hidden, viewModel.state.value.isSidebarCollapsed, viewModel.state.value.renaming?.path)
            )
        }

    @Test
    fun `expand all opens every folder and collapse all closes them, both remembered`() = runTreeTest {
        listOf("Projects/Garden/Tomato plan.md", "Recipes/Arepas.md").forEach {
            scanner.addNote(file = file(path = it))
        }
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnExpandAll)
        advanceUntilIdle()
        val expanded = viewModel.state.value.expandedFolders to preferences.saved.lastOrNull()?.expandedFolders
        viewModel.onIntent(intent = VaultTreeIntent.OnCollapseAll)
        advanceUntilIdle()

        val all = setOf(path("Projects"), path("Projects/Garden"), path("Recipes"))
        assertEquals(
            expected = (all to all) to (emptySet<RelativePath>() to emptySet<RelativePath>()),
            actual =
            expanded to (viewModel.state.value.expandedFolders to preferences.saved.lastOrNull()?.expandedFolders)
        )
    }

    @Test
    fun `a file opens with its app, a failure is shown, and a deletion is told to the tabs`() = runTreeTest {
        scanner.addAttachment(file = file(path = PLATE))
        scanner.addAttachment(file = file(path = MENU))
        val viewModel = startedViewModel()
        val effects = mutableListOf<VaultTreeEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        runCurrent()

        viewModel.onIntent(intent = VaultTreeIntent.OnFileOpen(path = path(MENU)))
        advanceUntilIdle()
        opener.failure = NotesError.OpenFailed(path = PLATE)
        viewModel.onIntent(intent = VaultTreeIntent.OnFileOpen(path = path(PLATE)))
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteRequest(path = path(MENU)))
        viewModel.onIntent(intent = VaultTreeIntent.OnDeleteConfirm)
        advanceUntilIdle()
        runCurrent()

        assertEquals(
            expected = Triple(
                listOf(path(MENU)),
                NotesError.OpenFailed(path = PLATE),
                listOf<VaultTreeEffect>(VaultTreeEffect.EntryDeleted(path = path(MENU)))
            ),
            actual = Triple(opener.opened.toList(), viewModel.state.value.actionError, effects.toList())
        )
    }

    @Test
    fun `today creates today's daily note where the user is, selects it in its open folder, then opens it as it is`() =
        runTreeTest {
            val viewModel = startedViewModel()

            viewModel.onIntent(intent = VaultTreeIntent.OnTodayClick)
            advanceUntilIdle()
            val first = viewModel.state.value
            viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(WELCOME)))
            viewModel.onIntent(intent = VaultTreeIntent.OnTodayClick)
            advanceUntilIdle()

            assertEquals(
                expected = listOf(path(TODAY), true, path(TODAY), 1),
                actual = listOf(
                    first.selectedPath,
                    path("Journal") in first.expandedFolders,
                    viewModel.state.value.selectedPath,
                    operations.existing.count { it == path(TODAY) }
                )
            )
        }

    @Test
    fun `a selected daily note knows the closest daily notes on each side and steps to them, others have none`() =
        runTreeTest {
            listOf("Journal/2026-10-20.md", "Journal/2026-10-22.md", TODAY, WELCOME).forEach {
                scanner.addNote(file = file(path = it))
            }
            val viewModel = startedViewModel()
            viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path("Journal/2026-10-22.md")))
            val middle = viewModel.state.value.dayNeighbors

            viewModel.onIntent(intent = VaultTreeIntent.OnDayStep(step = 1))
            viewModel.onIntent(intent = VaultTreeIntent.OnDayStep(step = 1))
            val last = viewModel.state.value.selectedPath
            viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(WELCOME)))
            viewModel.onIntent(intent = VaultTreeIntent.OnDayStep(step = -1))

            assertEquals(
                expected = listOf(
                    DayNeighbors(
                        date = CalendarDate(year = 2026, month = 10, day = 22),
                        previous = path("Journal/2026-10-20.md"),
                        next = path(TODAY)
                    ),
                    path(TODAY),
                    null,
                    path(WELCOME)
                ),
                actual = listOf(middle, last, viewModel.state.value.dayNeighbors, viewModel.state.value.selectedPath)
            )
        }

    @Test
    fun `daily notes follow the folder and format of the vault's Obsidian settings`() = runTreeTest {
        vaultSettings.dailyNotes = DailyNoteSettings(folder = "Diary", format = "DD.MM.YYYY", template = null)
        listOf("Diary/25.10.2026.md", "Journal/2026-10-22.md").forEach { scanner.addNote(file = file(path = it)) }
        val viewModel = startedViewModel()

        viewModel.onIntent(intent = VaultTreeIntent.OnTodayClick)
        advanceUntilIdle()

        assertEquals(
            expected = path("Diary/27.10.2026.md") to path("Diary/25.10.2026.md"),
            actual = viewModel.state.value.selectedPath to viewModel.state.value.dayNeighbors?.previous
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    /** Daily notes over the fakes, at [NOW] for a user two hours east of UTC, where it is already the next day. */
    private fun newNoteTools(): NewNoteTools = NewNoteTools(
        createNote = CreateNoteUseCase(noteFileOperations = operations),
        applyTemplate = ApplyTemplateUseCase(
            fillTemplate = FillTemplateUseCase(
                vaultSettingsReader = vaultSettings,
                noteContentStore = contents,
                localCalendar = FakeLocalCalendar(offsetHours = 2),
                clock = FakeClock(currentTime = Instant.parse(input = NOW))
            ),
            noteContentStore = contents
        )
    )

    private fun dailyNoteTools(): DailyNoteTools = DailyNoteTools(
        getDailyNoteSettings = GetDailyNoteSettingsUseCase(vaultSettingsReader = vaultSettings),
        openDailyNote = OpenDailyNoteUseCase(
            vaultSettingsReader = vaultSettings,
            noteFileOperations = operations,
            noteContentStore = contents,
            localCalendar = FakeLocalCalendar(offsetHours = 2),
            clock = FakeClock(currentTime = Instant.parse(input = NOW))
        )
    )

    private fun fromTemplate(): VaultTreeIntent = VaultTreeIntent.OnNoteFromTemplate(
        folder = path(PROJECTS),
        template = path(MEETING_TEMPLATE),
        baseName = UNTITLED
    )

    private fun TestScope.rename(
        viewModel: VaultTreeViewModel,
        from: String,
        to: String
    ) {
        contents.texts.getOrPut(key = path("$to.md")) { "" }
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(from)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = to))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()
    }

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
            newNotes = newNoteTools(),
            createFolder = CreateFolderUseCase(noteFileOperations = operations),
            renameEntry = RenameEntryUseCase(
                noteFileOperations = operations,
                validateFileName = ValidateFileNameUseCase()
            ),
            trash = trash,
            fileRevealer = revealer,
            fileOpener = opener,
            days = dailyNoteTools(),
            bookmarkStore = bookmarkStore,
            updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
                planLinkUpdates = PlanLinkUpdatesUseCase(
                    noteContentStore = contents,
                    linkFinder = JetBrainsMarkdownLinkFinder()
                ),
                applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = contents)
            ),
            followLink = FollowLinkUseCase(createNote = CreateNoteUseCase(noteFileOperations = operations)),
            treePreferencesStore = preferences
        )
        advanceUntilIdle()
        return viewModel
    }

    companion object {
        private const val BUG = "bug"
        private const val NOW = "2026-10-26T23:30:00Z"
        private const val TODAY = "Journal/2026-10-27.md"
        private const val MEETING_TEMPLATE = "Templates/Meeting.md"
        private const val MENU = "attachments/Weekly menu.pdf"
        private const val PLATE = "attachments/Arepas plate.png"
        private const val PROJECTS = "Projects"
        private const val ROADMAP = "Projects/Roadmap.md"
        private const val UNTITLED = "Untitled"
        private const val WELCOME = "Welcome.md"
    }
}
