package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.NewNoteDefaults
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class CreateEntryUseCasesTest {
    private val operations = FakeNoteFileOperations()
    private val createNote = CreateNoteUseCase(noteFileOperations = operations)
    private val createFolder = CreateFolderUseCase(noteFileOperations = operations)

    @Test
    fun `a note takes the base name, then the next free number`() = runTest {
        val first = createNote(
            vaultPath = VAULT,
            folder = IDEAS,
            baseName = UNTITLED
        )

        operations.existing += IDEAS.child(name = "Untitled 1.md")

        val second = createNote(
            vaultPath = VAULT,
            folder = IDEAS,
            baseName = UNTITLED
        )

        assertEquals(
            expected = listOf(
                success(path = "Ideas/Untitled.md"),
                success(path = "Ideas/Untitled 2.md")
            ),
            actual = listOf(first, second)
        )
    }

    // The developer's request of 2026-11-14: a new note starts with the day it was created and its tags.
    @Test
    fun `a new note is written with its default properties`() = runTest {
        val store = FakeNoteContentStore()

        val create = CreateNoteUseCase(
            noteFileOperations = operations,
            noteContentStore = store,
            defaults = NewNoteDefaults(
                localCalendar = FakeLocalCalendar(),
                clock = object : Clock {
                    override fun now(): Instant = Instant.parse(input = "2026-11-14T09:00:00Z")
                }
            )
        )

        create(
            vaultPath = VAULT,
            folder = IDEAS,
            baseName = UNTITLED
        )

        assertEquals(
            expected = "---\ncreated: 2026-11-14\ntags: []\n---\n",
            actual = store.texts[IDEAS.child(name = "Untitled.md")]
        )
    }

    @Test
    fun `a folder has no extension and is numbered the same way`() = runTest {
        operations.existing += RelativePath(value = "Untitled folder")

        assertEquals(
            expected = success(path = "Untitled folder 1"),
            actual = createFolder(
                vaultPath = VAULT,
                folder = RelativePath.ROOT,
                baseName = "Untitled folder"
            )
        )
    }

    @Test
    fun `a name taken while creating moves on to the next one`() = runTest {
        operations.takenOnCreate += IDEAS.child(name = "Untitled.md")

        assertEquals(
            expected = success(path = "Ideas/Untitled 1.md"),
            actual = createNote(
                vaultPath = VAULT,
                folder = IDEAS,
                baseName = UNTITLED
            )
        )
    }

    @Test
    fun `other failures are returned as they are, and a full folder reports the name as taken`() =
        runTest {
            operations.failure = NotesError.EntryMissing(path = IDEAS.value)

            val missingFolder = createNote(
                vaultPath = VAULT,
                folder = IDEAS,
                baseName = UNTITLED
            )

            operations.failure = null

            operations.existing +=
                (0 until 1_000).map { IDEAS.child(name = if (it == 0) "x.md" else "x $it.md") }

            assertEquals(
                expected = listOf(
                    Outcome.Failure(error = NotesError.EntryMissing(path = IDEAS.value)),
                    Outcome.Failure(error = NotesError.NameTaken(name = "x.md"))
                ),
                actual = listOf(
                    missingFolder,
                    createNote(
                        vaultPath = VAULT,
                        folder = IDEAS,
                        baseName = "x"
                    )
                )
            )
        }

    private fun success(path: String): Outcome<RelativePath, NotesError> = Outcome.Success(
        value = RelativePath(value = path)
    )

    companion object {
        private const val UNTITLED = "Untitled"
        private val IDEAS = RelativePath(value = "Ideas")
    }
}
