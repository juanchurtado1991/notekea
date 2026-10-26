package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownFormatterTest {
    private val formatter = MarkdownFormatter()

    @Test
    fun `bold wraps the selection, unwraps it again, and inserts a placeholder to type over`() {
        assertEquals(
            expected = listOf(
                "Mix **corn** flour" to "corn",
                "Mix corn flour" to "corn",
                "Mix **bold text** flour" to "bold text"
            ),
            actual = listOf(
                apply(text = "Mix corn flour", selected = "corn", action = BOLD),
                apply(text = "Mix **corn** flour", selected = "corn", action = BOLD),
                apply(text = "Mix  flour", cursor = 4, action = BOLD)
            )
        )
    }

    @Test
    fun `with the cursor in a word bold formats the word, and a selection holding the marks is unwrapped`() {
        assertEquals(
            expected = listOf(
                "Mix **corn** flour" to "corn",
                "Mix corn flour" to "corn",
                "Mix corn flour" to "corn",
                "Mix corn flour" to "corn"
            ),
            actual = listOf(
                apply(text = "Mix corn flour", cursor = 6, action = BOLD),
                apply(text = "Mix **corn** flour", cursor = 8, action = BOLD),
                apply(text = "Mix **corn** flour", selected = "**corn**", action = BOLD),
                apply(text = "Mix *corn* flour", cursor = 7, action = FormatAction.Italic(placeholder = "x"))
            )
        )
    }

    @Test
    fun `italic does not take the stars of bold as its own`() {
        assertEquals(
            expected = "Mix ***corn*** flour" to "corn",
            actual = apply(
                text = "Mix **corn** flour",
                selected = "corn",
                action = FormatAction.Italic(placeholder = "x")
            )
        )
    }

    @Test
    fun `list styles apply to every selected line, replace other lists, and come off when all have them`() {
        val lines = "Flour\nWater\nSalt"

        assertEquals(
            expected = listOf(
                "- Flour\n- Water\n- Salt",
                "1. Flour\n2. Water\n3. Salt",
                "- [ ] Flour\n- [ ] Water\n- [ ] Salt",
                "Flour\nWater\nSalt"
            ),
            actual = listOf(
                apply(text = lines, selected = lines, action = FormatAction.BulletList(placeholder = "Item")).first,
                apply(
                    text = "- Flour\n- Water\n- Salt",
                    selected = "- Flour\n- Water\n- Salt",
                    action = NUMBERED
                ).first,
                apply(
                    text = "1. Flour\n2. Water\n3. Salt",
                    selected = "1. Flour\n2. Water\n3. Salt",
                    action = CHECKLIST
                ).first,
                apply(
                    text = "- [ ] Flour\n- [ ] Water\n- [ ] Salt",
                    selected = "- [ ] Flour\n- [ ] Water\n- [ ] Salt",
                    action = CHECKLIST
                ).first
            )
        )
    }

    @Test
    fun `an empty line gets a checklist item to type over, a written line keeps the cursor on its text`() {
        assertEquals(
            expected = listOf("Intro\n- [ ] To do" to "To do", "- [ ] Buy flour" to ""),
            actual = listOf(
                apply(text = "Intro\n", cursor = 6, action = CHECKLIST),
                apply(text = "Buy flour", cursor = 3, action = CHECKLIST)
            )
        )
    }

    @Test
    fun `a heading replaces another level, and the same level removes it`() {
        assertEquals(
            expected = listOf("## Arepas", "# Arepas", "Arepas"),
            actual = listOf(
                apply(text = "Arepas", cursor = 0, action = HEADING_2).first,
                apply(
                    text = "### Arepas",
                    cursor = 0,
                    action = FormatAction.Heading(level = 1, placeholder = "Heading")
                ).first,
                apply(text = "## Arepas", cursor = 0, action = HEADING_2).first
            )
        )
    }

    @Test
    fun `a callout sits on lines of its own, holding the selection or a placeholder to type over`() {
        val tip = FormatAction.Callout(type = "tip", title = "Tip", placeholder = "Write here", isFolded = false)
        val folded = tip.copy(type = "faq", title = "Question", isFolded = true)

        assertEquals(
            expected = listOf(
                "Intro\n\n> [!tip] Tip\n> Write here\n" to "Write here",
                "Intro\n\n> [!faq]- Question\n> Soak\n> overnight\n\nEnd" to ""
            ),
            actual = listOf(
                apply(text = "Intro", cursor = 5, action = tip),
                apply(text = "Intro\nSoak\novernight\nEnd", selected = "Soak\novernight", action = folded)
            )
        )
    }

    @Test
    fun `a table, a code block and a divider are blocks too`() {
        assertEquals(
            expected = listOf(
                "| A | B |\n| --- | --- |\n| Text | Text |\n| Text | Text |\n| Text | Text |\n" to "A",
                "```\ncode\n```\n" to "code",
                "```kotlin\ncode\n```\n" to "code",
                "Intro\n\n---\n" to ""
            ),
            actual = listOf(
                apply(
                    text = "",
                    cursor = 0,
                    action = FormatAction.Table(columnNames = listOf("A", "B"), rows = 3, cellText = "Text")
                ),
                apply(text = "", cursor = 0, action = FormatAction.CodeBlock(placeholder = "code")),
                apply(
                    text = "",
                    cursor = 0,
                    action = FormatAction.CodeBlock(placeholder = "code", language = "kotlin")
                ),
                apply(text = "Intro", cursor = 5, action = FormatAction.Divider)
            )
        )
    }

    @Test
    fun `links to a note keep the selection as their label, web links select what is left to type`() {
        val link = FormatAction.NoteLink(target = "Arepas")

        assertEquals(
            expected = listOf(
                "See [[Arepas]]" to "",
                "See [[Arepas|the recipe]]" to "",
                "Open [example](https://)" to "https://",
                "Open [link text](https://)" to "link text"
            ),
            actual = listOf(
                apply(text = "See ", cursor = 4, action = link),
                apply(text = "See the recipe", selected = "the recipe", action = link),
                apply(
                    text = "Open example",
                    selected = "example",
                    action = FormatAction.WebLink(placeholder = "link text")
                ),
                apply(text = "Open ", cursor = 5, action = FormatAction.WebLink(placeholder = "link text"))
            )
        )
    }

    @Test
    fun `a tag gets a space before it when needed and one after it, in place of the selection`() {
        assertEquals(
            expected = listOf("Dinner #recipe ", "Dinner #weekend ", "Dinner #weekly-plan "),
            actual = listOf(
                apply(text = "Dinner", cursor = 6, action = FormatAction.Tag(name = "recipe")).first,
                apply(text = "Dinner ", cursor = 7, action = FormatAction.Tag(name = "weekend")).first,
                apply(
                    text = "Dinner weekly plan",
                    selected = "weekly plan",
                    action = FormatAction.Tag(name = "weekly-plan")
                )
                    .first
            )
        )
    }

    /** The text after [action] and what ends up selected, from a [selected] text or a [cursor] position. */
    private fun apply(
        text: String,
        action: FormatAction,
        selected: String? = null,
        cursor: Int = 0
    ): Pair<String, String> {
        val start = selected?.let { text.indexOf(string = it) } ?: cursor
        val end = selected?.let { start + it.length } ?: cursor
        val edit = formatter.format(text = text, selectionStart = start, selectionEnd = end, action = action)
        val result = text.replaceRange(startIndex = edit.start, endIndex = edit.end, replacement = edit.replacement)
        return result to result.substring(startIndex = edit.selectionStart, endIndex = edit.selectionEnd)
    }

    companion object {
        private val BOLD = FormatAction.Bold(placeholder = "bold text")
        private val CHECKLIST = FormatAction.Checklist(placeholder = "To do")
        private val HEADING_2 = FormatAction.Heading(level = 2, placeholder = "Heading")
        private val NUMBERED = FormatAction.NumberedList(placeholder = "Item")
    }
}
