package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenameAndUpdateLinksUseCaseTest {
    private val scanner = FakeNoteScanner()
    private val operations = FakeNoteFileOperations()
    private val store = FakeNoteContentStore()
    private val renameAndUpdateLinks = RenameAndUpdateLinksUseCase(
        noteScanner = scanner,
        renameEntry = RenameEntryUseCase(noteFileOperations = operations, validateFileName = ValidateFileNameUseCase()),
        updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
            planLinkUpdates = PlanLinkUpdatesUseCase(
                noteContentStore = store,
                linkFinder = JetBrainsMarkdownLinkFinder()
            ),
            applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
        )
    )

    @Test
    fun `the note is renamed and the notes that link to it follow`() = runTest {
        addNote(path = AREPAS, text = "# Arepas")
        addNote(path = WELCOME, text = "Cook [[Arepas]].")

        val outcome =
            renameAndUpdateLinks(vaultPath = VAULT, path = RelativePath(value = AREPAS), newName = "Arepas reina")

        assertEquals(
            expected = Outcome.Success(
                value = RenamedEntry(
                    path = RelativePath(value = "Recipes/Arepas reina.md"),
                    links = LinkUpdateResult(updated = listOf(RelativePath(value = WELCOME)), skipped = emptyList())
                )
            ),
            actual = outcome
        )
        assertEquals(
            expected = "Cook [[Arepas reina]].",
            actual = store.texts.getValue(RelativePath(value = WELCOME))
        )
    }

    @Test
    fun `the same name changes nothing, and a refused or unlisted rename is reported`() = runTest {
        addNote(path = AREPAS, text = "# Arepas")
        addNote(path = WELCOME, text = "Cook [[Arepas]].")

        val same = renameAndUpdateLinks(vaultPath = VAULT, path = RelativePath(value = AREPAS), newName = "Arepas")
        val hidden = renameAndUpdateLinks(vaultPath = VAULT, path = RelativePath(value = AREPAS), newName = ".Arepas")
        scanner.failure = NotesError.VaultMissing(path = VAULT)
        val missing = renameAndUpdateLinks(vaultPath = VAULT, path = RelativePath(value = AREPAS), newName = "Other")

        assertEquals(
            expected = listOf(
                Outcome.Success(value = RenamedEntry(path = RelativePath(value = AREPAS), links = null)),
                Outcome.Failure(error = NotesError.HiddenName),
                Outcome.Failure(error = NotesError.VaultMissing(path = VAULT))
            ),
            actual = listOf(same, hidden, missing)
        )
        assertEquals(
            expected = emptyList(),
            actual = operations.moves
        )
    }

    private fun addNote(
        path: String,
        text: String
    ) {
        scanner.addNote(file = file(path = path))
        operations.existing += RelativePath(value = path)
        store.texts[RelativePath(value = path)] = text
    }

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val WELCOME = "Welcome.md"
    }
}
