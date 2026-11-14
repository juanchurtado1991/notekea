package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A note dragged into a folder in the tree, and a copy of a note from its menu (the requests of 2026-11-14). */
class MoveAndDuplicateTest {
    private val operations = FakeNoteFileOperations()
    private val contents = FakeNoteContentStore()
    private val move = MoveEntryUseCase(noteFileOperations = operations)
    private val note = RelativePath(value = "Welcome.md")
    private val recipes = RelativePath(value = "Recipes")

    private fun folderRow(path: String) = TreeRow.Folder(
        path = RelativePath(value = path),
        depth = 0,
        fileCount = 0,
        isExpanded = false
    )

    private fun noteRow(path: String) = TreeRow.Note(
        path = RelativePath(value = path),
        depth = 0,
        modifiedAt = null
    )

    @Test
    fun `an entry dropped on a folder moves into it under its own name`() = runTest {
        val moved = move(
            vaultPath = VAULT,
            path = note,
            folder = recipes
        )

        val target = RelativePath(value = "Recipes/Welcome.md")

        assertEquals(
            expected = listOf<Any>(Outcome.Success(value = target), listOf(note to target)),
            actual = listOf<Any>(moved, operations.moves)
        )
    }

    @Test
    fun `nothing is written over, nothing moves into itself, and a drop where it is changes nothing`() =
        runTest {
            operations.existing += RelativePath(value = "Recipes/Welcome.md")

            val taken = move(VAULT, note, recipes)
            val intoItself = move(VAULT, recipes, RelativePath(value = "Recipes/Soups"))
            val same = move(VAULT, RelativePath(value = "Recipes/Arepas.md"), recipes)

            assertEquals(
                expected = listOf(
                    Outcome.Failure(error = NotesError.NameTaken(name = "Welcome.md")),
                    Outcome.Failure(error = NotesError.InvalidName),
                    Outcome.Success(value = RelativePath(value = "Recipes/Arepas.md")),
                    emptyList<Any>()
                ),
                actual = listOf<Any>(taken, intoItself, same, operations.moves)
            )
        }

    @Test
    fun `a drop goes into the folder under the pointer, beside a note, or to the root`() {
        val dragged = RelativePath(value = "Projects/Plan.md")

        assertEquals(
            expected = listOf("Recipes", "Recipes", "", null, null),
            actual = listOf(
                TreeDrop.folderFor(dragged, folderRow(path = "Recipes")),
                TreeDrop.folderFor(dragged, noteRow(path = "Recipes/Arepas.md")),
                TreeDrop.folderFor(dragged, null),
                TreeDrop.folderFor(dragged, folderRow(path = "Projects")),
                TreeDrop.folderFor(RelativePath(value = "Projects"), folderRow(path = "Projects/Garden"))
            ).map { it?.value }
        )
    }

    @Test
    fun `a copy of a note holds its text, named after it and numbered when taken`() = runTest {
        val arepas = RelativePath(value = "Recipes/Arepas.md")
        contents.texts[arepas] = "# Arepas\n\nTwo cups of flour."
        operations.existing += RelativePath(value = "Recipes/Arepas copy.md")

        val duplicate = DuplicateNoteUseCase(
            noteFileOperations = operations,
            noteContentStore = contents
        )

        val copy = duplicate(
            vaultPath = VAULT,
            note = arepas,
            suffix = "copy"
        )

        val path = RelativePath(value = "Recipes/Arepas copy 1.md")

        assertEquals(
            expected = listOf<Any?>(Outcome.Success(value = path), "# Arepas\n\nTwo cups of flour."),
            actual = listOf<Any?>(copy, contents.texts[path])
        )
    }
}
