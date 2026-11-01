package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A text's size in tokens, estimated (`ROADMAP.md` 2.1): never less than its characters or words suggest. */
class TokenEstimateTest {
    @Test
    fun `prose counts about four characters a token, short words count by words, and nothing counts nothing`() {
        assertEquals(
            expected = listOf(0, 4, 7),
            actual = listOf(
                "",
                "Arepas are good",
                "a b c d e"
            ).map { TokenEstimate.of(text = it) }
        )
    }
}
