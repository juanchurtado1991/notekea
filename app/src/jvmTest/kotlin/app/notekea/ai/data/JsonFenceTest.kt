package app.notekea.ai.data

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonFenceTest {
    @Test
    fun `a fenced reply loses its fence, wherever the pieces are cut`() {
        val reply = "```json\n$JSON\n```"

        (1..reply.length).forEach { size ->
            assertEquals(
                expected = JSON,
                actual = read(pieces = reply.chunked(size = size)),
                message = "pieces of $size"
            )
        }
    }

    @Test
    fun `a fence with no language and blanks around it is taken off too`() {
        assertEquals(
            expected = JSON,
            actual = read(pieces = listOf("\n ``", "`\n", JSON, "\n```\n"))
        )
    }

    @Test
    fun `a bare reply passes as it is, with the backticks its text holds`() {
        (1..JSON.length).forEach { size ->
            assertEquals(
                expected = JSON,
                actual = read(pieces = JSON.chunked(size = size)),
                message = "pieces of $size"
            )
        }
    }

    @Test
    fun `a bare reply streams as it is written, not at its end`() {
        val fence = JsonFence()

        assertEquals(
            expected = "{\"text\":",
            actual = fence.taken(piece = "{\"text\":")
        )
    }

    @Test
    fun `a reply cut before its fence closes keeps what was written`() {
        assertEquals(
            expected = "{\"text\": \"cut",
            actual = read(pieces = listOf("```json\n{\"text\": \"cut"))
        )
    }

    private fun read(pieces: List<String>): String {
        val fence = JsonFence()

        return pieces.joinToString(separator = "") { fence.taken(piece = it) } + fence.ended()
    }

    private companion object {
        const val JSON =
            "{\"tool\": \"answer\", \"text\": \"Run ```kotlin\\nval a = 1\\n``` and `b`\"}"
    }
}
