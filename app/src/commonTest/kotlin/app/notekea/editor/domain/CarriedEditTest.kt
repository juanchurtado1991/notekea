package app.notekea.editor.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CarriedEditTest {
    @Test
    fun `a rewrite before or after the person's edit is made on their text too`() {
        assertEquals(
            expected = listOf(
                "Intro, mine.\n\nSee [[Reina]].\n",
                "See [[Reina]].\n\nMine at the end.\n"
            ),
            actual = listOf(
                carried(
                    base = "Intro.\n\nSee [[Arepas]].\n",
                    theirs = "Intro.\n\nSee [[Reina]].\n",
                    mine = "Intro, mine.\n\nSee [[Arepas]].\n"
                ),
                carried(
                    base = "See [[Arepas]].\n",
                    theirs = "See [[Reina]].\n",
                    mine = "See [[Arepas]].\n\nMine at the end.\n"
                )
            )
        )
    }

    @Test
    fun `a rewrite that leaves the text alone carries nothing, whatever was typed`() {
        assertEquals(
            expected = "Typed at the end.",
            actual = carried(
                base = "Typed",
                theirs = "Typed",
                mine = "Typed at the end."
            )
        )
    }

    @Test
    fun `an edit to the part the rewrite changed, or right next to it, is left to the person`() {
        assertNull(
            actual = CarriedEdit.of(
                base = "See [[Arepas]].\n",
                theirs = "See [[Reina]].\n",
                mine = "See [[Arepa]].\n"
            )
        )

        assertNull(
            actual = CarriedEdit.of(
                base = "See [[Arepas]].\n",
                theirs = "See [[Reina]].\n",
                mine = "See [[Arepas2]].\n"
            )
        )
    }

    private fun carried(
        base: String,
        theirs: String,
        mine: String
    ): String? {
        val edit = CarriedEdit.of(
            base = base,
            theirs = theirs,
            mine = mine
        ) ?: return null

        return mine.replaceRange(
            startIndex = edit.start,
            endIndex = edit.end,
            replacement = edit.replacement
        )
    }
}
