package app.notekea.notes.presentation.tree

import kotlinx.coroutines.ExperimentalCoroutinesApi
import app.notekea.core.domain.time.CalendarDate
import app.notekea.notes.domain.DailyNoteSettings
import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent

/** Going to entries from elsewhere */
@OptIn(ExperimentalCoroutinesApi::class)
class VaultTreeNavigationTest : VaultTreeViewModelTestBase() {
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
}
