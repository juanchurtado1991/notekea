package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A text's size in tokens, estimated: never less than its characters or words suggest, at the
 * multilingual model's worst (`docs/spikes/2026-11-01-embedding-models.md`).
 */
class TokenEstimateTest {
    // Audit: without spaces a run of ideographs was one word, and counted half of what the model reads.
    @Test
    fun `scripts written without spaces count a token a character`() {
        assertEquals(
            expected = 8,
            actual = TokenEstimate.of(text = "日本語のテキスト")
        )

        assertEquals(
            expected = 5,
            actual = TokenEstimate.of(text = "สวัสดี".take(n = 5))
        )

        assertEquals(
            expected = 2 + 2,
            actual = TokenEstimate.of(text = "中文abcd")
        )
    }

    @Test
    fun `a token every two characters, at least two a word, and nothing counts nothing`() {
        assertEquals(
            expected = listOf(0, 8, 10),
            actual = listOf(
                "",
                "Arepas are good",
                "a b c d e"
            ).map { TokenEstimate.of(text = it) }
        )
    }
}
