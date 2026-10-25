package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteEditingUseCasesTest {
    private val store = FakeNoteContentStore()
    private val openNote = OpenNoteUseCase(noteContentStore = store)
    private val saveNote = SaveNoteUseCase(noteContentStore = store)

    @Test
    fun `a note opens split into its header and body`() = runTest {
        store.texts[NOTE] = "---\ntags: [recipe]\n---\n# Arepas\n"

        assertEquals(
            expected = Outcome.Success(
                value = NoteDocument(header = "---\ntags: [recipe]\n---\n", body = "# Arepas\n", usesCrLf = false)
            ),
            actual = openNote(vaultPath = VAULT, path = NOTE)
        )
    }

    @Test
    fun `a note that cannot be read does not open`() = runTest {
        assertEquals(
            expected = Outcome.Failure(error = NotesError.OperationFailed(path = NOTE.value)),
            actual = openNote(vaultPath = VAULT, path = NOTE)
        )
    }

    @Test
    fun `saving writes the header untouched and returns the text written`() = runTest {
        store.texts[NOTE] = ORIGINAL
        val document = NoteDocument.fromText(text = ORIGINAL).copy(body = "# Arepas reina\n")

        val saved = saveNote(vaultPath = VAULT, path = NOTE, document = document, expectedOnDisk = ORIGINAL)

        assertEquals(
            expected = Outcome.Success(value = "---\ntags: [recipe]\n---\n# Arepas reina\n"),
            actual = saved
        )
        assertEquals(
            expected = "---\ntags: [recipe]\n---\n# Arepas reina\n",
            actual = store.texts.getValue(NOTE)
        )
    }

    @Test
    fun `a note changed elsewhere is never overwritten without being asked`() = runTest {
        store.texts[NOTE] = "Edited in another app"
        val document = NoteDocument.fromText(text = ORIGINAL).copy(body = "Mine")

        val refused = saveNote(vaultPath = VAULT, path = NOTE, document = document, expectedOnDisk = ORIGINAL)
        val kept = saveNote(
            vaultPath = VAULT,
            path = NOTE,
            document = document,
            expectedOnDisk = "Edited in another app"
        )

        assertEquals(
            expected = listOf(
                Outcome.Failure(error = NotesError.ChangedOnDisk(path = NOTE.value)),
                Outcome.Success(value = "---\ntags: [recipe]\n---\nMine")
            ),
            actual = listOf(refused, kept)
        )
    }

    @Test
    fun `a save never brings back a note that was moved or deleted meanwhile`() = runTest {
        val document = NoteDocument.fromText(text = ORIGINAL)

        val saved = saveNote(vaultPath = VAULT, path = NOTE, document = document, expectedOnDisk = ORIGINAL)

        assertEquals(
            expected = Outcome.Failure(error = NotesError.EntryMissing(path = NOTE.value)),
            actual = saved
        )
        assertEquals(
            expected = null,
            actual = store.texts[NOTE]
        )
    }

    @Test
    fun `a deleted note can be written again, and a refused write is reported`() = runTest {
        val document = NoteDocument.fromText(text = ORIGINAL)

        val restored = saveNote(vaultPath = VAULT, path = NOTE, document = document, expectedOnDisk = null)
        store.refused += NOTE
        val refused = saveNote(vaultPath = VAULT, path = NOTE, document = document, expectedOnDisk = ORIGINAL)

        assertEquals(
            expected = listOf(
                Outcome.Success(value = ORIGINAL),
                Outcome.Failure(error = NotesError.OperationFailed(path = NOTE.value))
            ),
            actual = listOf(restored, refused)
        )
    }

    companion object {
        private const val ORIGINAL = "---\ntags: [recipe]\n---\n# Arepas\n"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
