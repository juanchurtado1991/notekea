package app.notekea.search.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** A note's text fingerprint (`ROADMAP.md` 2.3): the same for the same text, another for any change. */
class ContentHashTest {
    @Test
    fun `the same text has the same fingerprint, a one-letter change another`() {
        val text = "# Arepas\nCorn dough."

        assertEquals(expected = ContentHash.of(text = text), actual = ContentHash.of(text = "# Arepas\nCorn dough."))
        assertNotEquals(illegal = ContentHash.of(text = text), actual = ContentHash.of(text = "# Arepas\nCorn dougH."))
        assertNotEquals(illegal = ContentHash.of(text = ""), actual = ContentHash.of(text = " "))
    }
}
