package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.file
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.time.Instant

class VaultTreeTest {
    @Test
    fun `adding a nested note adds its missing parent folders`() {
        val tree = VaultTree.EMPTY.withNote(note = note(path = TOMATO_PLAN))

        assertEquals(
            expected = setOf(path(value = PROJECTS), path(value = GARDEN)),
            actual = tree.folders
        )
        assertEquals(
            expected = setOf(path(value = TOMATO_PLAN)),
            actual = tree.notes.keys
        )
    }

    @Test
    fun `files that are not notes join with their folders, move with a renamed folder and leave with a removed one`() {
        val plate = file(path = "Recipes/attachments/plate.png")
        val tree = VaultTree.EMPTY.withAttachment(file = plate)
        val moved = tree.renamed(from = RelativePath(value = "Recipes"), to = RelativePath(value = "Food"))

        assertEquals(
            expected = setOf(RelativePath(value = "Recipes"), RelativePath(value = "Recipes/attachments")),
            actual = tree.folders
        )
        assertEquals(
            expected = mapOf(
                RelativePath(value = "Food/attachments/plate.png") to
                    plate.copy(path = RelativePath(value = "Food/attachments/plate.png"))
            ),
            actual = moved.attachments
        )
        assertEquals(
            expected = emptyMap(),
            actual = tree.without(path = RelativePath(value = "Recipes/attachments")).attachments
        )
    }

    @Test
    fun `adding a note again replaces it`() {
        val updated = note(path = TOMATO_PLAN).copy(metadata = NoteMetadata.NONE.copy(title = TITLE))

        val tree = VaultTree.EMPTY.withNote(note = note(path = TOMATO_PLAN)).withNote(note = updated)

        assertEquals(
            expected = mapOf(path(value = TOMATO_PLAN) to updated),
            actual = tree.notes
        )
    }

    @Test
    fun `adding a note already known as it is, or removing a path the tree does not have, keeps the same tree`() {
        val tree = VaultTree.EMPTY.withNote(note = note(path = TOMATO_PLAN))

        assertSame(expected = tree, actual = tree.withNote(note = note(path = TOMATO_PLAN)))
        assertSame(expected = tree, actual = tree.without(path = path(value = PROJECTS_PLAN)))
    }

    @Test
    fun `adding a nested empty folder adds its parents and never the root`() {
        val tree = VaultTree.EMPTY.withFolder(path = path(value = GARDEN)).withFolder(path = RelativePath.ROOT)

        assertEquals(
            expected = setOf(path(value = PROJECTS), path(value = GARDEN)),
            actual = tree.folders
        )
    }

    @Test
    fun `removing a folder removes everything inside it and nothing with a similar name`() {
        val tree = VaultTree.EMPTY
            .withNote(note = note(path = TOMATO_PLAN))
            .withNote(note = note(path = PROJECTS_PLAN))
            .withFolder(path = path(value = "Projects/Empty"))

        val pruned = tree.without(path = path(value = PROJECTS))

        assertEquals(
            expected = VaultTree(
                folders = emptySet(),
                notes = mapOf(path(value = PROJECTS_PLAN) to note(path = PROJECTS_PLAN))
            ),
            actual = pruned
        )
    }

    @Test
    fun `removing a note keeps its folder`() {
        val tree = VaultTree.EMPTY.withNote(note = note(path = TOMATO_PLAN)).without(path = path(value = TOMATO_PLAN))

        assertEquals(
            expected = VaultTree(folders = setOf(path(value = PROJECTS), path(value = GARDEN)), notes = emptyMap()),
            actual = tree
        )
    }

    @Test
    fun `a renamed note keeps its metadata at the new path`() {
        val tagged = note(path = TOMATO_PLAN).copy(metadata = NoteMetadata.NONE.copy(title = TITLE))

        val tree = VaultTree.EMPTY
            .withNote(
                note = tagged
            ).renamed(from = path(value = TOMATO_PLAN), to = path(value = "$GARDEN/Tomatoes.md"))

        assertEquals(
            expected = mapOf(
                path(value = "$GARDEN/Tomatoes.md") to
                    tagged.copy(file = tagged.file.copy(path = path(value = "$GARDEN/Tomatoes.md")))
            ),
            actual = tree.notes
        )
    }

    @Test
    fun `a renamed folder takes its subfolders and notes along, and nothing with a similar name`() {
        val tree = VaultTree.EMPTY
            .withNote(note = note(path = TOMATO_PLAN))
            .withNote(note = note(path = PROJECTS_PLAN))
            .withFolder(path = path(value = "Projects/Empty"))

        val renamed = tree.renamed(from = path(value = PROJECTS), to = path(value = "Work"))

        assertEquals(
            expected = setOf(path(value = "Work"), path(value = "Work/Garden"), path(value = "Work/Empty")),
            actual = renamed.folders
        )
        assertEquals(
            expected = setOf(path(value = "Work/Garden/Tomato plan.md"), path(value = PROJECTS_PLAN)),
            actual = renamed.notes.keys
        )
    }

    private fun note(
        path: String
    ): NoteEntry = NoteEntry(
        file = NoteFile(
            path = path(value = path),
            size = SIZE,
            modifiedAt = Instant.fromEpochMilliseconds(MODIFIED_MILLIS)
        ),
        metadata = NoteMetadata.NONE
    )

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    companion object {
        private const val GARDEN = "Projects/Garden"
        private const val MODIFIED_MILLIS = 1_800_000_000_000L
        private const val PROJECTS = "Projects"
        private const val PROJECTS_PLAN = "Projects plan.md"
        private const val SIZE = 120L
        private const val TITLE = "Tomatoes"
        private const val TOMATO_PLAN = "Projects/Garden/Tomato plan.md"
    }
}
