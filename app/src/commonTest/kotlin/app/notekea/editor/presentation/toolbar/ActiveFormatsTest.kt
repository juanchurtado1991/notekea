package app.notekea.editor.presentation.toolbar

import kotlin.test.Test
import kotlin.test.assertEquals

class ActiveFormatsTest {
    private val formats = ActiveFormats()

    @Test
    fun `plain text has no format`() {
        assertEquals(
            expected = emptySet(),
            actual = at(
                text = "Mix the flour.",
                words = "flour"
            )
        )
    }

    @Test
    fun `bold, italic, a wiki link and a tag mark their formats while the cursor is in them`() {
        val text = "Mix the **corn flour** with *warm* water, see [[Masa]] #recipe"

        assertEquals(
            expected = listOf(
                setOf(ToolbarFormat.BOLD),
                setOf(ToolbarFormat.ITALIC),
                setOf(ToolbarFormat.NOTE_LINK),
                setOf(ToolbarFormat.TAG),
                emptySet()
            ),
            actual = listOf("flour", "arm", "asa", "ecipe", "water").map {
                at(
                    text = text,
                    words = it
                )
            }
        )
    }

    @Test
    fun `a heading, a bulleted list and a checklist mark their line, with the words' formats`() {
        val text = "## Dough\n- **corn** flour\n- [x] warm water\nplain"

        assertEquals(
            expected = listOf(
                setOf(ToolbarFormat.HEADING),
                setOf(ToolbarFormat.BULLET_LIST, ToolbarFormat.BOLD),
                setOf(ToolbarFormat.BULLET_LIST),
                setOf(ToolbarFormat.CHECKLIST),
                emptySet()
            ),
            actual = listOf("ough", "orn", "flour", "warm", "plain").map {
                at(
                    text = text,
                    words = it
                )
            }
        )
    }

    @Test
    fun `only the cursor's line is read`() {
        assertEquals(
            expected = emptySet(),
            actual = at(
                text = "**bold**\nplain\n- list",
                words = "plain"
            )
        )
    }

    /** The formats with the cursor at the start of [words]. */
    private fun at(
        text: String,
        words: String
    ): Set<ToolbarFormat> = formats.at(
        text = text,
        cursor = text.indexOf(string = words)
    )
}
