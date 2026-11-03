package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** What an indexing run does (`ROADMAP.md` 2.3): only the notes that are new, changed or of another version. */
class PlanIndexUseCaseTest {
    private val plan = PlanIndexUseCase()

    @Test
    fun `new, changed and other-version notes are read, gone ones removed, the rest left`() {
        val notes = listOf(
            file(path = "Recipes/Arepas.md", size = 10),
            file(path = "Books/Dune.md", size = 20),
            file(path = "Trip.md", size = 31),
            file(path = "Old version.md", size = 40),
            file(path = "New.md", size = 50)
        )
        val indexed = listOf(
            indexed(file = file(path = "Recipes/Arepas.md", size = 10)),
            indexed(file = file(path = "Books/Dune.md", size = 20)),
            indexed(file = file(path = "Trip.md", size = 30)),
            indexed(file = file(path = "Old version.md", size = 40), version = "old"),
            indexed(file = file(path = "Deleted.md", size = 60))
        )

        val result = plan(notes = notes, indexed = indexed, version = VERSION)

        assertEquals(
            expected = IndexPlan(
                read = listOf(
                    file(path = "New.md", size = 50),
                    file(path = "Old version.md", size = 40),
                    file(path = "Trip.md", size = 31)
                ),
                remove = listOf(RelativePath(value = "Deleted.md")),
                unchanged = 2
            ),
            actual = result
        )
    }

    @Test
    fun `a note whose size or date the file system cannot tell is always read`() {
        val unknown = NoteFile(path = RelativePath(value = "Note.md"), size = null, modifiedAt = null)

        val result = plan(notes = listOf(unknown), indexed = listOf(indexed(file = unknown)), version = VERSION)

        assertEquals(expected = listOf(unknown), actual = result.read)
    }

    @Test
    fun `nothing to do when every note is indexed as it is`() {
        val notes = listOf(file(path = "A.md", size = 1), file(path = "B.md", size = 2))

        val result = plan(notes = notes, indexed = notes.map { indexed(file = it) }, version = VERSION)

        assertEquals(expected = Triple(true, 2, 0), actual = Triple(result.isEmpty, result.unchanged, result.read.size))
    }

    private companion object {
        const val VERSION = "v1"
        val DATE = Instant.parse(input = "2026-11-03T10:00:00Z")

        fun file(path: String, size: Long) = NoteFile(path = RelativePath(value = path), size = size, modifiedAt = DATE)

        fun indexed(file: NoteFile, version: String = VERSION) =
            IndexedNote(file = file, contentHash = "hash-${file.path.value}", version = version)
    }
}
