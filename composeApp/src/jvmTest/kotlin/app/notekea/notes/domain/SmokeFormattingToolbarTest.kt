package app.notekea.notes.domain

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The formatting toolbar cases of `MVP_SMOKE_TEST.md` (1.17) on the practice note itself: each case's steps are
 * applied with the toolbar's formatter, in the guide's order, so every text the guide promises is checked against the
 * file the Playground Vault gets. Placeholders are the app's English ones.
 */
class SmokeFormattingToolbarTest {
    private val formatter = MarkdownFormatter()
    private val listEditing = ListEditing()
    private val practice = File(SMOKE_NOTE).readText()

    @Test
    fun `1_17-02 headings toggle and resize, bold and italic wrap and unwrap, bold alone inserts text to type over`() {
        val steps = Steps(text = practice)
        val headings = listOf(1, 1, 2).map { level ->
            steps.atLineOf(line = "Weekend arepas", action = FormatAction.Heading(level = level, placeholder = HEADING))
            steps.lineWith(part = "Weekend arepas")
        }
        steps.select(part = "corn flour", action = BOLD)
        val bold = steps.lineWith(part = "corn flour")
        steps.select(part = "corn flour", action = BOLD)
        steps.select(part = "fresh", action = FormatAction.Italic(placeholder = "italic text"))
        steps.at(index = practice.indexOf(char = '\n'), action = BOLD)
        val inserted = steps.selected()
        steps.type(text = "soft")

        assertEquals(
            expected = listOf(
                listOf("# Weekend arepas", "Weekend arepas", "## Weekend arepas"),
                listOf(
                    "Buy **corn flour**",
                    "Buy corn flour",
                    "Buy *fresh* cheese"
                ),
                listOf("bold text", "Plain text to format with the toolbar: nothing below is Markdown yet.**soft**")
            ),
            actual = listOf(
                headings,
                listOf(bold, steps.lineWith(part = "corn flour"), steps.lineWith(part = "cheese")),
                listOf(inserted, steps.lineWith(part = "Plain text"))
            )
        )
    }

    @Test
    fun `1_17-02 with the cursor in a word, bold makes it bold and takes it off again`() {
        val steps = Steps(text = practice)
        steps.at(index = steps.text.indexOf(string = "beans") + 2, action = BOLD)
        val bold = steps.lineWith(part = "beans")
        steps.at(index = steps.text.indexOf(string = "beans") + 2, action = BOLD)

        assertEquals(
            expected = "Soak the **beans**" to "Soak the beans",
            actual = bold to steps.lineWith(part = "beans")
        )
    }

    @Test
    fun `1_17-03 the three lines become a checklist, a numbered list, bullets and a checklist again`() {
        val lines = "Buy corn flour\nBuy fresh cheese\nSoak the beans"
        val steps = Steps(text = practice)
        val results = listOf(
            CHECKLIST,
            FormatAction.NumberedList(placeholder = ITEM),
            BULLETS,
            CHECKLIST
        ).map { action ->
            steps.select(
                part = steps.text.substring(range = steps.rangeOfLines(first = "corn flour", last = "beans")),
                action = action
            )
            steps.text.substring(range = steps.rangeOfLines(first = "corn flour", last = "beans"))
        }

        assertEquals(
            expected = listOf(
                "- [ ] Buy corn flour\n- [ ] Buy fresh cheese\n- [ ] Soak the beans",
                "1. Buy corn flour\n2. Buy fresh cheese\n3. Soak the beans",
                "- Buy corn flour\n- Buy fresh cheese\n- Soak the beans",
                "- [ ] Buy corn flour\n- [ ] Buy fresh cheese\n- [ ] Soak the beans"
            ),
            actual = results
        )
        assertEquals(expected = true, actual = practice.contains(other = lines))
    }

    @Test
    fun `1_17-04 coffee links to Café con leche, keeping coffee as the label`() {
        val notePaths = File(PLAYGROUND_SOURCE).walkTopDown()
            .filter { it.isFile && it.extension == "md" && ".obsidian" !in it.path && ".trash" !in it.path }
            .map { RelativePath(value = it.relativeTo(base = File(PLAYGROUND_SOURCE)).invariantSeparatorsPath) }
            .toList() + RelativePath(value = "Editor/Formatting practice.md")
        val target = LinkResolver(
            notePaths = notePaths
        ).wikiTarget(path = RelativePath(value = "Recipes/Café con leche.md"))
        val steps = Steps(text = practice)
        steps.select(part = "coffee", action = FormatAction.NoteLink(target = target))

        assertEquals(
            expected = "Fry the arepas in a hot pan and serve them with [[Café con leche|coffee]].",
            actual = steps.lineWith(part = "Fry")
        )
    }

    @Test
    fun `1_17-05 a web link selects its address to type, and a vault tag and a new one end the heading line`() {
        val steps = Steps(text = practice)
        steps.atLineOf(line = "Weekend arepas", action = FormatAction.Heading(level = 2, placeholder = HEADING))
        steps.select(part = "hot pan", action = FormatAction.WebLink(placeholder = "link text"))
        val address = steps.selected()
        steps.type(text = "https://example.com")
        steps.at(index = steps.text.indexOf(string = "arepas\n") + "arepas".length, action = steps.tag(name = "recipe"))
        steps.at(index = steps.cursor, action = steps.tag(name = "weekend"))

        assertEquals(
            expected = listOf(
                "https://",
                "Fry the arepas in a [hot pan](https://example.com) and serve them with coffee.",
                "## Weekend arepas #recipe #weekend "
            ),
            actual = listOf(address, steps.lineWith(part = "Fry"), steps.lineWith(part = "Weekend"))
        )
    }

    @Test
    fun `1_17-06 the reminder becomes a folded warning and a tip is typed at the end`() {
        val steps = Steps(text = practice)
        steps.select(part = REMINDER, action = callout(type = "warning", title = "Warning", isFolded = true))
        steps.at(index = steps.text.length, action = callout(type = "tip", title = "Tip", isFolded = false))
        val placeholder = steps.selected()
        steps.type(text = "Use a cast-iron pan")

        assertEquals(
            expected = "Write here" to
                "\n\n> [!warning]- Warning\n> $REMINDER\n\n> [!tip] Tip\n> Use a cast-iron pan\n",
            actual = placeholder to steps.text.substringAfter(delimiter = "coffee.")
        )
    }

    @Test
    fun `1_17-07 a 3 x 2 table, a kotlin code block, a divider and a quote each go on lines of their own`() {
        val steps = Steps(text = practice)
        steps.at(
            index = steps.text.length,
            action = FormatAction.Table(
                columnNames = listOf("Column 1", "Column 2", "Column 3"),
                rows = 2,
                cellText = "Text"
            )
        )
        val header = steps.selected()
        steps.at(index = steps.text.length, action = FormatAction.CodeBlock(placeholder = "code", language = "kotlin"))
        val code = steps.selected()
        steps.at(index = steps.text.length, action = FormatAction.Divider)
        steps.at(index = steps.text.length, action = FormatAction.Quote(placeholder = "Quote"))

        assertEquals(
            expected = Triple(
                "| Column 1 | Column 2 | Column 3 |\n| --- | --- | --- |\n" +
                    "| Text | Text | Text |\n| Text | Text | Text |\n\n```kotlin\ncode\n```\n\n---\n> Quote",
                "Column 1",
                "code"
            ),
            actual = Triple(steps.text.substringAfter(delimiter = "$REMINDER\n\n"), header, code)
        )
    }

    @Test
    fun `1_17-10 enter continues and ends a checklist, tab nests, backspace removes a marker, quotes continue`() {
        val steps = Steps(text = practice)
        steps.select(
            part = steps.text.substring(range = steps.rangeOfLines(first = "corn flour", last = "beans")),
            action = CHECKLIST
        )
        steps.atEndOf(line = "Soak the beans", key = ListAction.NEW_ITEM)
        steps.type(text = "Buy salt")
        steps.key(action = ListAction.NEW_ITEM)
        val emptyItem = steps.lineAtCursor()
        steps.key(action = ListAction.NEW_ITEM)
        val ended = steps.lineAtCursor()
        steps.atEndOf(line = "Buy fresh cheese", key = ListAction.INDENT)
        val nested = steps.lineWith(part = "cheese")
        steps.key(action = ListAction.OUTDENT)
        steps.at(index = steps.text.indexOf(string = "Buy salt"), action = null)
        steps.key(action = ListAction.REMOVE_MARKER)
        steps.atEndOf(line = REMINDER, key = null)
        steps.select(part = REMINDER, action = callout(type = "tip", title = "Tip", isFolded = false))
        steps.atEndOf(line = REMINDER, key = ListAction.NEW_ITEM)

        assertEquals(
            expected = listOf("- [ ] ", "", "  - [ ] Buy fresh cheese", "- [ ] Buy fresh cheese", "Buy salt", "> "),
            actual = listOf(
                emptyItem,
                ended,
                nested,
                steps.lineWith(part = "cheese"),
                steps.lineWith(part = "salt"),
                steps.lineAtCursor()
            )
        )
    }

    private fun callout(
        type: String,
        title: String,
        isFolded: Boolean
    ): FormatAction.Callout = FormatAction.Callout(
        type = type,
        title = title,
        placeholder = "Write here",
        isFolded = isFolded
    )

    /** The note as a person edits it: the toolbar's edits at a selection or the cursor, and typing over a selection. */
    private inner class Steps(
        var text: String
    ) {
        // Nothing selected and the cursor at the start: an empty range whose last is -1.
        private var selection = IntRange(start = 0, endInclusive = -1)
        val cursor: Int get() = selection.last + 1

        fun select(
            part: String,
            action: FormatAction
        ) {
            val start = text.indexOf(string = part)
            apply(start = start, end = start + part.length, action = action)
        }

        fun at(
            index: Int,
            action: FormatAction?
        ) {
            if (action == null) selection = index..<index else apply(start = index, end = index, action = action)
        }

        /** The cursor at the end of [line], then [key] pressed there, if any. */
        fun atEndOf(
            line: String,
            key: ListAction?
        ) {
            val end = text.indexOf(string = line) + line.length
            selection = end..<end
            key?.let { key(action = it) }
        }

        fun key(
            action: ListAction
        ) {
            val edit = listEditing.edit(
                text = text,
                selectionStart = selection.first,
                selectionEnd = selection.last + 1,
                action = action
            ) ?: return
            text = text.replaceRange(startIndex = edit.start, endIndex = edit.end, replacement = edit.replacement)
            selection = edit.selectionStart..<edit.selectionEnd
        }

        fun lineAtCursor(): String {
            val start = text.lastIndexOf(char = '\n', startIndex = selection.first - 1) + 1
            val end = text.indexOf(char = '\n', startIndex = selection.first).takeIf { it >= 0 } ?: text.length
            return text.substring(startIndex = start, endIndex = end)
        }

        fun atLineOf(
            line: String,
            action: FormatAction
        ) = at(index = text.indexOf(string = line) + 1, action = action)

        fun type(
            text: String
        ) {
            this.text = this.text.replaceRange(range = selection, replacement = text)
            val end = selection.first + text.length
            selection = end..<end
        }

        fun selected(): String = text.substring(range = selection)

        fun lineWith(
            part: String
        ): String = text.lines().first { it.contains(other = part) }

        fun rangeOfLines(
            first: String,
            last: String
        ): IntRange {
            val start = text.lastIndexOf(char = '\n', startIndex = text.indexOf(string = first)) + 1
            val end = text.indexOf(char = '\n', startIndex = text.indexOf(string = last))
            return start..<end
        }

        fun tag(
            name: String
        ): FormatAction.Tag = FormatAction.Tag(name = name)

        private fun apply(
            start: Int,
            end: Int,
            action: FormatAction
        ) {
            val edit = formatter.format(text = text, selectionStart = start, selectionEnd = end, action = action)
            text = text.replaceRange(startIndex = edit.start, endIndex = edit.end, replacement = edit.replacement)
            selection = edit.selectionStart..<edit.selectionEnd
        }
    }

    companion object {
        private const val HEADING = "Heading"
        private const val ITEM = "Item"
        private const val PLAYGROUND_SOURCE = "../smoke-data/vaults/Personal Notes"
        private const val REMINDER = "Remember that the pan stays hot for a long time."
        private const val SMOKE_NOTE = "../smoke-data/notes/Formatting practice.md"
        private val BOLD = FormatAction.Bold(placeholder = "bold text")
        private val BULLETS = FormatAction.BulletList(placeholder = ITEM)
        private val CHECKLIST = FormatAction.Checklist(placeholder = "To do")
    }
}
