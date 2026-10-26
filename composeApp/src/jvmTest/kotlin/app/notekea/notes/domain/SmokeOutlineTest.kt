package app.notekea.notes.domain

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The outline cases of `MVP_SMOKE_TEST.md` (1.9) on the smoke note itself, as the editor opens it (its body, without
 * the frontmatter), so the guide never promises headings, indentation or leftovers the note does not give.
 */
class SmokeOutlineTest {
    private val body = NoteDocument.fromText(text = File(SMOKE_NOTE).readText()).body
    private val finder = OutlineFinder()

    @Test
    fun `1_9-01 the outline lists the headings as the guide says, indented, without the tag and the code comment`() {
        assertEquals(
            expected = listOf(
                "Arepas guide" to 0,
                "Ingredients" to 1,
                "For the filling and the shopping list" to 2,
                "Steps" to 1,
                "Tip: resting the dough" to 2,
                "Cooking" to 2,
                "Not headings" to 1,
                "Serving" to 0,
                "Last section" to 1
            ),
            actual = finder.headings(text = body).map { it.text to it.depth }
        )
    }

    @Test
    fun `1_9-02 typing today at the end of the Steps heading renames it in the outline`() {
        val steps = finder.headings(text = body)[STEPS]
        val lineEnd = body.indexOf(char = '\n', startIndex = steps.offset)

        val typed = body.substring(startIndex = 0, endIndex = lineEnd) + " today" + body.substring(startIndex = lineEnd)

        assertEquals(
            expected = "Steps today",
            actual = finder.headings(text = typed)[STEPS].text
        )
    }

    companion object {
        private const val SMOKE_NOTE = "../smoke-data/notes/Outline practice.md"
        private const val STEPS = 3
    }
}
