package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ChunkBlocksTest {
    @Test
    fun `a shorter fence inside a longer one does not end the code, so its blank lines stay in the block`() {
        val code = "````\n```\n\nstill code\n````"
        val body = "$code\n\nafter"

        assertEquals(
            expected = listOf(code, "after"),
            actual = ChunkBlocks.of(body = body, range = body.indices).map { body.substring(range = it) }
        )
    }
}
