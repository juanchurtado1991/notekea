package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A text's size in tokens, estimated (`ROADMAP.md` 2.1): never less than its characters or words suggest, at the
 * multilingual model's worst (`docs/spikes/2026-11-01-embedding-models.md`).
 */
class TokenEstimateTest {
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
