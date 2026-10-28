package app.notekea.notes.presentation.tree

import kotlinx.coroutines.ExperimentalCoroutinesApi
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.TreeRow
import app.notekea.notes.domain.TreeSort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent

@OptIn(ExperimentalCoroutinesApi::class)
class VaultTreeViewModelTest : VaultTreeViewModelTestBase() {
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
}
