package app.notekea.chat.domain.loop

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An answer of the loop is shown with the sources of the notes it names, as an answer from the notes always was
 * (`docs/agent-loop.md`, "Step 6, finished"): a passage its search gave, or the note it read whole.
 */
class TurnSourcesTest {
    private val arepas = RelativePath(value = "Recipes/Arepas.md")
    private val soup = RelativePath(value = "Recipes/Soup.md")
    private val lease = RelativePath(value = "Home/Lease.md")

    private val passage = AnswerSource(
        number = 1,
        note = arepas,
        headingPath = listOf("Arepas", "Dough"),
        range = 10..40,
        text = "2½ cups of warm water"
    )

    @Test
    fun `only the notes the answer names are its sources, numbered as it names them, a whole note opening as one`() {
        val sources = TurnSources.of(
            answer = "The Soup note serves 4; Arepas take 2½ cups of water.",
            passages = listOf(passage),
            read = listOf(arepas, soup, lease)
        )

        assertEquals(
            expected = listOf(
                Triple(1, soup, IntRange.EMPTY),
                Triple(2, arepas, 10..40)
            ),
            actual = sources.map { Triple(it.number, it.note, it.range) }
        )
    }

    @Test
    fun `the not found reply has no sources`() {
        assertEquals(
            expected = emptyList(),
            actual = TurnSources.of(
                answer = "",
                passages = listOf(passage),
                read = listOf(arepas)
            )
        )
    }
}
