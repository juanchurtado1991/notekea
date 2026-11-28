package app.notekea.chat.domain.loop

import kotlin.test.Test
import kotlin.test.assertEquals

/** The names, numbers and codes a text writes, which tell one entry from another. */
class FrameWordsTest {
    @Test
    fun `a name is a word with a capital, and a number or a code is taken whole`() {
        assertEquals(
            expected = listOf(
                setOf("ana", "11:00", "2026-038"),
                setOf("11:00", "2026-038")
            ),
            actual = listOf(
                FrameWords.namesOf(text = "call Ana at 11:00 about 2026-038"),
                FrameWords.codesOf(text = "call Ana at 11:00 about 2026-038")
            )
        )
    }
}
