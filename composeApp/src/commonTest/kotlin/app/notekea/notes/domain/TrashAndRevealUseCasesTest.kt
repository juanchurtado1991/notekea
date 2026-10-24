package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TrashAndRevealUseCasesTest {
    @Test
    fun `deleting moves the entry to the trash and tells where it went`() = runTest {
        val trash = FakeTrash(outcome = Outcome.Success(value = TrashLocation.SYSTEM_TRASH))

        assertEquals(
            expected = Outcome.Success(value = TrashLocation.SYSTEM_TRASH),
            actual = DeleteEntryUseCase(trash = trash)(vaultPath = VAULT, path = NOTE)
        )
        assertEquals(
            expected = listOf(NOTE),
            actual = trash.trashed
        )
    }

    @Test
    fun `revealing asks the file manager and returns its failure`() = runTest {
        val revealer = FakeFileRevealer(outcome = Outcome.Failure(error = NotesError.RevealFailed(path = NOTE.value)))

        assertEquals(
            expected = Outcome.Failure(error = NotesError.RevealFailed(path = NOTE.value)),
            actual = RevealEntryUseCase(fileRevealer = revealer)(vaultPath = VAULT, path = NOTE)
        )
        assertEquals(
            expected = listOf(NOTE),
            actual = revealer.revealed
        )
    }

    companion object {
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
