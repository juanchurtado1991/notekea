package app.notekea.editor.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class NoteBlocksTest {
    @Test
    fun `a note is cut at its blank lines and each block keeps the blank lines after it`() {
        assertEquals(
            expected = listOf("# Arepas\n\n", "Mix the flour.\nAdd water.\n\n\n", "Rest the dough.\n"),
            actual = blocksOf(text = "# Arepas\n\nMix the flour.\nAdd water.\n\n\nRest the dough.\n")
        )
    }

    @Test
    fun `an empty note and a note of blank lines are one block`() {
        assertEquals(
            expected = listOf(listOf(""), listOf("\n\n"), listOf("\n\nArepas")),
            actual = listOf("", "\n\n", "\n\nArepas").map { blocksOf(text = it) }
        )
    }

    @Test
    fun `a blank line inside a code block does not cut it`() {
        val code = "```kotlin\nval a = 1\n\nval b = 2\n```\n\n"
        val tildes = "~~~\none\n\ntwo\n~~~\n\n"

        assertEquals(
            expected = listOf("Before\n\n", code, tildes, "After"),
            actual = blocksOf(text = "Before\n\n$code${tildes}After")
        )
    }

    @Test
    fun `a heading opens a block with no blank line before it, but not inside a code block`() {
        val code = "```sh\n# a comment\n```\n"

        assertEquals(
            expected = listOf("Intro\n", "## Dough\nMix.\n", "###### Rest\n$code", "#hashtag and ####### seven\n"),
            actual = blocksOf(text = "Intro\n## Dough\nMix.\n###### Rest\n$code\n#hashtag and ####### seven\n")
                .map {
                    it.trimEnd('\n') + "\n"
                }
        )
    }

    @Test
    fun `two cuts are equal when their blocks are the same`() {
        assertEquals(
            expected = listOf(true, false, false),
            actual = listOf("One\n\nTwo", "One\n\nTwo!", "On\n\nTwoo").map {
                NoteBlocks.of(text = it) == NoteBlocks.of(text = "One\n\nTwo")
            }
        )
    }

    @Test
    fun `an indented line after a blank one stays with the list item above it`() {
        val list = "- Buy flour\n\n    The yellow one.\n\n"

        assertEquals(
            expected = listOf(list, "- Buy cheese\n"),
            actual = blocksOf(text = "$list- Buy cheese\n")
        )
    }

    @Test
    fun `the block of an offset is the one holding that character, and the last one at the end of the text`() {
        val blocks = NoteBlocks.of(text = TEXT)

        assertEquals(
            expected = listOf(0, 0, 0, 1, 1, 2, 2),
            actual = listOf(0, 1, 4, 5, 9, 10, TEXT.length).map { blocks.at(offset = it) }
        )
    }

    @Test
    fun `the window is the blocks the selection touches`() {
        val blocks = NoteBlocks.of(text = TEXT)

        assertEquals(
            expected = listOf(1..1, 0..1, 0..2, 2..2),
            actual = listOf(5 to 5, 4 to 5, 0 to TEXT.length, 12 to 12).map { (min, max) ->
                blocks.window(
                    selectionMin = min,
                    selectionMax = max
                )
            }
        )
    }

    @Test
    fun `the window's text leaves out the line break of its last blank line, except in the note's last block`() {
        val blocks = NoteBlocks.of(text = TEXT)

        assertEquals(
            expected = listOf("One\n", "One\n\nTwo\n", "Three"),
            actual = listOf(0..0, 0..1, 2..2).map { window ->
                TEXT.substring(
                    startIndex = blocks.start(index = window.first),
                    endIndex = blocks.textEnd(window = window)
                )
            }
        )
    }

    private fun blocksOf(text: String): List<String> {
        val blocks = NoteBlocks.of(text = text)

        return List(size = blocks.size) {
            text.substring(
                startIndex = blocks.start(index = it),
                endIndex = blocks.end(index = it)
            )
        }
    }

    companion object {
        private const val TEXT = "One\n\nTwo\n\nThree"
    }
}
