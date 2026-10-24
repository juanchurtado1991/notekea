package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `adding a note again replaces it`() {
        val updated = note(path = TOMATO_PLAN).copy(metadata = NoteMetadata.NONE.copy(title = TITLE))

        val tree = VaultTree.EMPTY.withNote(note = note(path = TOMATO_PLAN)).withNote(note = updated)

        assertEquals(
            expected = mapOf(path(value = TOMATO_PLAN) to updated),
            actual = tree.notes
        )
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
