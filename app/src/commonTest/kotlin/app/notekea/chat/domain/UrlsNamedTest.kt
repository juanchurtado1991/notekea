package app.notekea.chat.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The addresses a request writes, without what ends its sentence: a pasted "…" left the page refused. */
class UrlsNamedTest {
    @Test
    fun `an address keeps none of the punctuation or ellipsis written after it`() {
        assertEquals(
            expected = listOf(
                "https://kotlinlang.org/docs/kmp-overview.html",
                "https://x.com/a",
                "https://x.com/b"
            ),
            actual = UrlsNamed.writtenIn(
                text = "https://kotlinlang.org/docs/kmp-overview.html… and https://x.com/a. or (https://x.com/b)"
            )
        )
    }
}
