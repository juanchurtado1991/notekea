package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A note split into chunks (`ROADMAP.md` 2.1): its body only, each chunk exactly its part of the body. */
class ChunkNoteUseCaseTest {
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))

    @Test
    fun `the frontmatter is left out and each chunk is exactly its trimmed range of the body`() {
        val text = "---\ntags: [food]\n---\n# Arepas\nCorn dough.\n\n## Dough\nKnead it.\n\n"

        val chunks = chunkNote(note = NOTE, text = text)

        assertEquals(
            expected = listOf(
                Triple(0, listOf("Arepas"), "# Arepas\nCorn dough."),
                Triple(1, listOf("Arepas", "Dough"), "## Dough\nKnead it.")
            ),
            actual = chunks.map { Triple(it.index, it.headingPath, it.text) }
        )
        val body = "# Arepas\nCorn dough.\n\n## Dough\nKnead it.\n\n"
        assertEquals(expected = chunks.map { it.text }, actual = chunks.map { body.substring(range = it.range) })
    }

    @Test
    fun `a note saved with Windows line breaks has the ranges of the body the editor shows`() {
        val chunks = chunkNote(note = NOTE, text = "# One\r\nFirst.\r\n# Two\r\nSecond.\r\n")

        assertEquals(
            expected = listOf(0..11, 13..25),
            actual = chunks.map { it.range }
        )
    }

    @Test
    fun `an empty note or one with only frontmatter has no chunks`() {
        assertEquals(
            expected = listOf(emptyList(), emptyList()),
            actual = listOf("", "---\ntitle: Empty\n---\n\n").map { chunkNote(note = NOTE, text = it) }
        )
    }

    private companion object {
        val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
