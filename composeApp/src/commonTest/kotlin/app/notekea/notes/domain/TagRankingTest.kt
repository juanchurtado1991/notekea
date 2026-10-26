package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.entry
import app.notekea.notes.domain.NoteFixtures.tags
import kotlin.test.Test
import kotlin.test.assertEquals

class TagRankingTest {
    @Test
    fun `the most used tags come first, ties in alphabetical order ignoring case`() {
        val notes = listOf(
            entry(path = "Arepas.md", metadata = tags("recipe", "venezuelan")),
            entry(path = "Pasta.md", metadata = tags("recipe", "Italian")),
            entry(path = "Lisbon.md", metadata = tags("travel")),
            entry(path = "Welcome.md")
        )

        assertEquals(
            expected = listOf("recipe", "Italian", "travel", "venezuelan"),
            actual = TagRanking().byUse(notes = notes)
        )
    }
}
