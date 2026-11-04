package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A note split into chunks (`ROADMAP.md` 2.1): its body only, each chunk exactly its part of the body; short sections
 * join the next ones under the same parent heading, a section that fits is one chunk.
 */
class ChunkNoteUseCaseTest {
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))

    @Test
    fun `the frontmatter is left out, and short sections join into one chunk with the headings they share`() {
        val text = "---\ntags: [food]\n---\n# Arepas\nCorn dough.\n\n## Dough\nKnead it.\n\n"

        val chunks = chunkNote(note = NOTE, text = text)

        val body = "# Arepas\nCorn dough.\n\n## Dough\nKnead it.\n\n"
        assertEquals(
            expected = listOf(Triple(listOf("Arepas"), "# Arepas\nCorn dough.\n\n## Dough\nKnead it.", 0..39)),
            actual = chunks.map { Triple(it.headingPath, it.text, it.range) }
        )
        assertEquals(expected = chunks.map { it.text }, actual = chunks.map { body.substring(range = it.range) })
    }

    @Test
    fun `a note saved with Windows line breaks has the ranges of the body the editor shows`() {
        val chunks = chunkNote(note = NOTE, text = "# One\r\nFirst.\r\n# Two\r\nSecond.\r\n")

        assertEquals(
            expected = listOf(emptyList<String>() to 0..25),
            actual = chunks.map {
                it.headingPath to it.range
            }
        )
    }

    @Test
    fun `an empty note or one with only frontmatter has no chunks`() {
        assertEquals(
            expected = listOf(emptyList(), emptyList()),
            actual = listOf("", "---\ntitle: Empty\n---\n\n").map { chunkNote(note = NOTE, text = it) }
        )
    }

    @Test
    fun `a short section never joins one outside its parent heading`() {
        val text = "# Food\n${words(count = 120)}\n## Arepas\nCorn.\n# Travel\nLisbon."

        assertEquals(
            expected = listOf(listOf("Food"), listOf("Food", "Arepas"), listOf("Travel")),
            actual = chunkNote(note = NOTE, text = text).map { it.headingPath }
        )
    }

    @Test
    fun `sections that fit on their own stay apart, numbered in order`() {
        val text = "# One\n${words(count = 120)}\n# Two\n${words(count = 120)}"

        assertEquals(
            expected = listOf(0 to listOf("One"), 1 to listOf("Two")),
            actual = chunkNote(note = NOTE, text = text).map { it.index to it.headingPath }
        )
    }

    @Test
    fun `short sections stop joining before they would be larger than the maximum`() {
        val text = "# One\n${words(count = 70)}\n# Two\n${words(count = 100)}"

        assertEquals(
            expected = listOf(listOf("One"), listOf("Two")),
            actual = chunkNote(note = NOTE, text = text).map { it.headingPath }
        )
    }

    private companion object {
        val NOTE = RelativePath(value = "Recipes/Arepas.md")

        /** [count] words of prose, one sentence. */
        fun words(count: Int): String = List(size = count) { "word" }.joinToString(separator = " ") + "."
    }
}
