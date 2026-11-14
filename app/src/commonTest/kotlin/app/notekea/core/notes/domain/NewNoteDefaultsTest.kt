package app.notekea.core.notes.domain

import app.notekea.core.domain.time.FakeLocalCalendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

/** A new note starts with the day it was created and an empty list of tags (the request of 2026-11-14). */
class NewNoteDefaultsTest {
    private val defaults = NewNoteDefaults(
        localCalendar = FakeLocalCalendar(),
        clock = object : Clock {
            override fun now(): Instant = Instant.parse(input = "2026-03-05T10:00:00Z")
        }
    )

    @Test
    fun `a new note gets the day it was created and an empty list of tags`() {
        assertEquals(
            expected = "---\ncreated: 2026-03-05\ntags: []\n---\n# Ideas\n",
            actual = defaults.on(text = "# Ideas\n")
        )
    }

    @Test
    fun `a note that brings its own properties keeps them`() {
        val clipped = "---\nsource: https://example.com\n---\n# A page\n"

        assertEquals(
            expected = clipped,
            actual = defaults.on(text = clipped)
        )
    }
}
