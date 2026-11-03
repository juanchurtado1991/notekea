package app.notekea.notes.presentation.tree

import kotlinx.coroutines.ExperimentalCoroutinesApi
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteRewrite
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.TrashLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent

/** The sidebar's edits */
@OptIn(ExperimentalCoroutinesApi::class)
class VaultTreeEditsTest : VaultTreeViewModelTestBase() {
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
}
