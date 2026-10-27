package app.notekea.notes.domain

import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import kotlin.test.Test
import kotlin.test.assertEquals

class GetNoteStatsUseCaseTest {
    private val getNoteStats = GetNoteStatsUseCase(linkFinder = JetBrainsMarkdownLinkFinder())

    @Test
    fun `words, links, tags and tasks are counted from the text, Markdown marks and code links left out`() {
        val body = "# Arepas\n\nMix the [[Flour]] and [salt](Salt.md) #recipe #Recipe.\n\n" +
            "- [x] Knead\n- [ ] Rest #weekend\n\n```\n[[Not a link]]\n```\n"

        assertEquals(
            expected = NoteStats(words = 14, links = 2, tags = 3, tasks = 2, doneTasks = 1),
            actual = getNoteStats(body = body, propertyTags = listOf("breakfast", "weekend"))
        )
    }

    @Test
    fun `reading time rounds up to whole minutes, and an empty note takes none`() {
        assertEquals(
            expected = listOf(0, 1, 1, 2),
            actual = listOf(0, 1, 200, 201).map {
                NoteStats(words = it, links = 0, tags = 0, tasks = 0, doneTasks = 0).readingMinutes
            }
        )
    }
}
