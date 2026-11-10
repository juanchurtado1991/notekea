package app.notekea.editor.presentation.markdown

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ShownTextBuilderTest {
    private val builder = ShownTextBuilder()

    @Test
    fun `the marks of the lines away from the cursor are hidden`() {
        val shown = shown(
            note = NOTE,
            cursor = null
        )

        assertEquals(
            expected = "Title\nSome bold and soft text, code, Arepas and a site.\n• one\n1. two\n> said",
            actual = shown.text
        )

        assertEquals(
            expected = listOf(
                "Title" to HighlightKind.HEADING_1,
                "bold" to HighlightKind.BOLD,
                "soft" to HighlightKind.ITALIC,
                "code" to HighlightKind.CODE_INLINE,
                "Arepas" to HighlightKind.WIKI_LINK,
                "site" to HighlightKind.LINK,
                "> " to HighlightKind.MARKER
            ),
            actual = shown
                .spans
                .sortedBy { it.start }
                .map {
                    shown.text.substring(
                        startIndex = it.start,
                        endIndex = it.end
                    ) to it.kind
                }
        )
    }

    @Test
    fun `the cursor's line shows its marks, and only that line`() {
        val shown = shown(
            note = NOTE,
            cursor = NOTE.indexOf(string = "bold")
        )

        assertEquals(
            expected = "Title\n$PROSE\n• one\n1. two\n> said",
            actual = shown.text
        )

        val list = shown(
            note = NOTE,
            cursor = NOTE.indexOf(string = "one")
        )

        assertEquals(
            expected = "Title\nSome bold and soft text, code, Arepas and a site.\n- one\n1. two\n> said",
            actual = list.text
        )
    }

    @Test
    fun `a selection shows the marks of every line it touches`() {
        val shown = builder.of(
            note = NOTE,
            window = TextRange(
                start = 0,
                end = NOTE.length
            ),
            revealed = TextRange(
                start = 2,
                end = NOTE.indexOf(string = "bold")
            )
        )

        assertEquals(
            expected = "# Title\n$PROSE\n• one\n1. two\n> said",
            actual = shown.text
        )
    }

    @Test
    fun `a task away from the cursor keeps its brackets, for the box drawn over them, without its mark`() {
        val note = "- [ ] buy\n  - [x] done\n* plain"

        assertEquals(
            expected = "[ ] buy\n  [x] done\n• plain",
            actual = shown(
                note = note,
                cursor = null
            ).text
        )
    }

    @Test
    fun `code blocks and what is not a mark stay as written`() {
        val note = "```kotlin\nval a = **b**\n```\n---\n| a | b |"

        assertEquals(
            expected = note,
            actual = shown(
                note = note,
                cursor = null
            ).text
        )
    }

    @Test
    fun `offsets go from the note to the shown text and back`() {
        val note = "intro\n\n# Title\n**bold** end"
        val start = note.indexOf(string = "# Title")

        val shown = builder.of(
            note = note,
            window = TextRange(
                start = start,
                end = note.length
            ),
            revealed = null
        )

        assertEquals(
            expected = "Title\nbold end",
            actual = shown.text
        )

        assertEquals(
            expected = 0,
            actual = shown.toShown(offset = start),
            message = "A hidden mark's start"
        )

        assertEquals(
            expected = 0,
            actual = shown.toShown(offset = start + 2),
            message = "A hidden mark's end"
        )

        assertEquals(
            expected = "Title\nbold".length,
            actual = shown.toShown(offset = note.indexOf(string = " end"))
        )

        assertEquals(
            expected = 0,
            actual = shown.toShown(offset = 0),
            message = "Before the window"
        )

        assertEquals(
            expected = start + 2,
            actual = shown.toNote(offset = 0),
            message = "A line's start is after its hidden mark"
        )

        assertEquals(
            expected = note.indexOf(string = "old"),
            actual = shown.toNote(offset = "Title\nb".length)
        )

        assertEquals(
            expected = note.indexOf(string = " end"),
            actual = shown.toNote(offset = "Title\nbold".length),
            message = "After the closing mark"
        )

        assertEquals(
            expected = note.length,
            actual = shown.toNote(offset = shown.text.length)
        )
    }

    @Test
    fun `the cursor moving inside its line builds nothing new`() {
        val first = shown(
            note = NOTE,
            cursor = NOTE.indexOf(string = "bold")
        )

        assertSame(
            expected = first,
            actual = shown(
                note = NOTE,
                cursor = NOTE.indexOf(string = "soft")
            )
        )

        assertNotSame(
            illegal = first,
            actual = shown(
                note = NOTE,
                cursor = 1
            )
        )
    }

    private fun shown(
        note: String,
        cursor: Int?
    ): ShownText = builder.of(
        note = note,
        window = TextRange(
            start = 0,
            end = note.length
        ),
        revealed = cursor?.let { TextRange(index = it) }
    )

    companion object {
        private const val PROSE =
            "Some **bold** and _soft_ text, `code`, [[Recipes/Arepas|Arepas]] and a [site](https://a.b)."
        private const val NOTE = "# Title\n$PROSE\n- one\n1. two\n> said"
    }
}
