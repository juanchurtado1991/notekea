package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A section larger than the maximum (`ROADMAP.md` 2.1): split between its paragraphs, code blocks kept whole, each
 * chunk after the first starting with the previous one's last sentence, none larger than the maximum.
 */
class ChunkLongSectionTest {
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))

    @Test
    fun `a long section is split between its paragraphs, each chunk starting with the last sentence before it`() {
        val paragraphs = (1..6).map { paragraph(number = it) }
        val text = "# Long\n" + paragraphs.joinToString(separator = "\n\n")

        val chunks = chunkNote(note = NOTE, text = text)

        assertTrue(actual = chunks.size > 1, message = "${chunks.size} chunks")
        assertTrue(actual = chunks.all { it.tokens <= ChunkSize.MAX && it.headingPath == listOf("Long") })
        chunks.zipWithNext().forEach { (previous, next) ->
            val lastSentence = previous.text.substringAfterLast(delimiter = ". ").trim()
            assertTrue(actual = next.text.startsWith(prefix = lastSentence), message = next.text.take(n = 80))
        }
    }

    // Found on smoke-data with the multilingual model's token count: a paragraph just under the maximum, after another,
    // went over it once the previous chunk's last sentence was added in front of it.
    @Test
    fun `the previous chunk's last sentence is added only while the chunk still fits the maximum`() {
        val nearlyFull = (1..20).joinToString(separator = " ") { "Long paragraph sentence $it has words in it." }
        val text = "# Long\n${paragraph(number = 1)}\n\n$nearlyFull"

        val chunks = chunkNote(note = NOTE, text = text)

        assertEquals(expected = 2, actual = chunks.size)
        assertTrue(actual = chunks.all { it.tokens <= ChunkSize.MAX }, message = chunks.map { it.tokens }.toString())
    }

    @Test
    fun `a code block that fits is never split, even across its blank lines`() {
        val code =
            "```kotlin\n" + List(size = 20) { "val line$it = $it\n\nprintln(line$it)" }.joinToString("\n") + "\n```"
        val text = "# Code\n${paragraph(number = 1)}\n\n$code\n\n${paragraph(number = 2)}"

        val chunks = chunkNote(note = NOTE, text = text)

        assertEquals(
            expected = 1,
            actual = chunks.count {
                code in it.text
            },
            message = chunks.joinToString { it.text.take(30) }
        )
    }

    @Test
    fun `a code block larger than the maximum is split between its lines`() {
        val code = "```\n" + List(size = 400) { "line $it of the code" }.joinToString(separator = "\n") + "\n```"

        val chunks = chunkNote(note = NOTE, text = "# Code\n$code")

        assertTrue(actual = chunks.size > 1)
        assertTrue(actual = chunks.all { it.tokens <= ChunkSize.MAX })
        assertTrue(actual = chunks.all { chunk -> chunk.text.lines().all { it.matches(regex = CODE_LINE) } })
    }

    @Test
    fun `a sentence without an end larger than the maximum is cut at spaces`() {
        val text = "# Run-on\n" + List(size = 900) { "word" }.joinToString(separator = " ")

        val chunks = chunkNote(note = NOTE, text = text)

        assertTrue(actual = chunks.size > 1)
        assertTrue(actual = chunks.all { it.tokens <= ChunkSize.MAX })
        assertTrue(
            actual = chunks.all { chunk ->
                chunk.text.split(" ").all {
                    it == "word" || it == "#" ||
                        it == "Run-on\nword"
                }
            }
        )
    }

    private companion object {
        val NOTE = RelativePath(value = "Notes/Long.md")
        val CODE_LINE = Regex(pattern = "```|# Code|line \\d+ of the code")

        /** A paragraph of about 130 tokens: ten sentences numbered after [number]. */
        fun paragraph(number: Int): String =
            (1..10).joinToString(separator = " ") { "Paragraph $number sentence $it has some words in it." }
    }
}
