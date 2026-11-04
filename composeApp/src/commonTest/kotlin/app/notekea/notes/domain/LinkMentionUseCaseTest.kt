package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkMentionUseCaseTest {
    private val store = FakeNoteContentStore()
    private val linkMention = LinkMentionUseCase(
        noteContentStore = store,
        applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
    )
    private val note = entry(path = AREPAS, metadata = NoteMetadata.NONE.copy(aliases = listOf("Corn cakes")))

    @Test
    fun `the words become a link, keeping how they were written, and the frontmatter stays`() = runTest {
        val texts = listOf(
            "---\ntags: [trip]\n---\nWe ate Arepas.",
            "We ate arepas.",
            "We ate corn cakes."
        )

        val linked = texts.map { text ->
            link(text = text, words = text.substringAfter(delimiter = "ate ").dropLast(n = 1))
        }

        assertEquals(
            expected = listOf(
                true to "---\ntags: [trip]\n---\nWe ate [[Arepas]].",
                true to "We ate [[Arepas|arepas]].",
                true to "We ate [[Arepas|corn cakes]]."
            ),
            actual = linked
        )
    }

    // Audit: a plain `|` in a table row ended the cell in the middle of the link.
    @Test
    fun `in a table the bar before the words is escaped`() = runTest {
        assertEquals(
            expected = true to "| We ate [[Arepas\\|arepas]]. | x |",
            actual = link(text = "| We ate arepas. | x |", words = "arepas")
        )
    }

    @Test
    fun `nothing is written when the words there are no longer the note's name`() = runTest {
        store.texts[LISBON] = "We ate tacos."

        val linked = linkMention(vaultPath = VAULT, mention = mentionAt(range = 7..10), note = note, notes = NOTES)

        assertEquals(
            expected = false to "We ate tacos.",
            actual = linked to store.texts.getValue(LISBON)
        )
    }

    private suspend fun link(
        text: String,
        words: String
    ): Pair<Boolean, String> {
        store.texts[LISBON] = text
        val body = NoteDocument.fromText(text = text).body
        val start = body.indexOf(string = words)
        val linked = linkMention(
            vaultPath = VAULT,
            mention = mentionAt(range = start until start + words.length),
            note = note,
            notes = NOTES
        )
        return linked to store.texts.getValue(LISBON)
    }

    private fun mentionAt(
        range: IntRange
    ) = NoteMention(source = LISBON, range = range, count = 1, passage = "", highlight = IntRange.EMPTY)

    private companion object {
        const val AREPAS = "Recipes/Arepas.md"
        val LISBON = RelativePath(value = "Travel/Lisbon.md")
        val NOTES = listOf(RelativePath(value = AREPAS), LISBON)
    }
}
