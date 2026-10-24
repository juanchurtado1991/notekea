package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenameEntryUseCaseTest {
    private val operations = FakeNoteFileOperations()
    private val renameEntry =
        RenameEntryUseCase(noteFileOperations = operations, validateFileName = ValidateFileNameUseCase())

    @Test
    fun `a note is renamed without typing its extension, and a typed one is not doubled`() = runTest {
        val outcomes = listOf("  Arepas reina ", "Arepas reina.MD").map {
            renameEntry(vaultPath = VAULT, path = AREPAS, newName = it)
        }

        assertEquals(
            expected = List(size = 2) { Outcome.Success(value = RelativePath(value = "Recipes/Arepas reina.md")) },
            actual = outcomes
        )
    }

    @Test
    fun `a folder keeps the name as typed`() = runTest {
        assertEquals(
            expected = Outcome.Success(value = RelativePath(value = "Cooking.notes")),
            actual = renameEntry(vaultPath = VAULT, path = RelativePath(value = "Recipes"), newName = "Cooking.notes")
        )
    }

    @Test
    fun `invalid and hidden names are refused without touching the disk`() = runTest {
        val outcomes = listOf("", "  .md", "a/b", "Arepas: reina", ".secret").map {
            renameEntry(vaultPath = VAULT, path = AREPAS, newName = it)
        }

        assertEquals(
            expected = List(size = 4) { Outcome.Failure(error = NotesError.InvalidName) } +
                Outcome.Failure(error = NotesError.HiddenName),
            actual = outcomes
        )
        assertEquals(
            expected = emptyList(),
            actual = operations.moves
        )
    }

    @Test
    fun `keeping the same name changes nothing and a move failure is returned`() = runTest {
        val unchanged = renameEntry(vaultPath = VAULT, path = AREPAS, newName = "Arepas")
        operations.failure = NotesError.NameTaken(name = "Pasta e fagioli.md")
        val taken = renameEntry(vaultPath = VAULT, path = AREPAS, newName = "Pasta e fagioli")

        assertEquals(
            expected = listOf(
                Outcome.Success(value = AREPAS),
                Outcome.Failure(error = NotesError.NameTaken(name = "Pasta e fagioli.md"))
            ),
            actual = listOf(unchanged, taken)
        )
        assertEquals(
            expected = emptyList(),
            actual = operations.moves
        )
    }

    companion object {
        private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
    }
}
