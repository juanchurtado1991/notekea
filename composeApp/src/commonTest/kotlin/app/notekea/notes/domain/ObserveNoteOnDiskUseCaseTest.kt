package app.notekea.notes.domain

import app.cash.turbine.test
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveNoteOnDiskUseCaseTest {
    private val scanner = FakeNoteScanner()
    private val store = FakeNoteContentStore()
    private val observeNoteOnDisk = ObserveNoteOnDiskUseCase(noteScanner = scanner, noteContentStore = store)

    @Test
    fun `a change to the file is reported with its new text, once`() = runTest {
        scanner.addNote(file = file(path = NOTE))
        store.texts[RelativePath(value = NOTE)] = "Before"

        observeNoteOnDisk(vaultPath = VAULT, path = RelativePath(value = NOTE)).test {
            advanceTimeBy(delayTimeMillis = THREE_POLLS_MILLIS)
            expectNoEvents()

            scanner.addNote(file = file(path = NOTE, modifiedOffsetMillis = 1L))
            store.texts[RelativePath(value = NOTE)] = "After"
            advanceTimeBy(delayTimeMillis = THREE_POLLS_MILLIS)

            assertEquals(
                expected = NoteDiskChange.Changed(text = "After"),
                actual = awaitItem()
            )
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a file that disappears is reported as deleted, and coming back as changed`() = runTest {
        scanner.addNote(file = file(path = NOTE))
        store.texts[RelativePath(value = NOTE)] = "Text"

        observeNoteOnDisk(vaultPath = VAULT, path = RelativePath(value = NOTE)).test {
            scanner.files.remove(RelativePath(value = NOTE))
            advanceTimeBy(delayTimeMillis = THREE_POLLS_MILLIS)
            val deleted = awaitItem()

            scanner.addNote(file = file(path = NOTE, modifiedOffsetMillis = 2L))
            advanceTimeBy(delayTimeMillis = THREE_POLLS_MILLIS)

            assertEquals(
                expected = listOf(NoteDiskChange.Deleted, NoteDiskChange.Changed(text = "Text")),
                actual = listOf(deleted, awaitItem())
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private const val NOTE = "Recipes/Arepas.md"
        private const val THREE_POLLS_MILLIS = 3_000L
    }
}
