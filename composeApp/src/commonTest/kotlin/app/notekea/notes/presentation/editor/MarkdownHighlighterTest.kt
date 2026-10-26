package app.notekea.notes.presentation.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownHighlighterTest {
    private val highlighter = MarkdownHighlighter()

    @Test
    fun `a heading line takes its level's size and mutes its marker`() {
        assertEquals(
            expected = listOf("# Title" to HighlightKind.HEADING_1, "# " to HighlightKind.SYNTAX),
            actual = shown(text = "# Title")
        )
        assertEquals(
            expected = listOf("### Deep" to HighlightKind.HEADING_3, "### " to HighlightKind.SYNTAX),
            actual = shown(text = "### Deep")
        )
        assertEquals(
            expected = listOf("#hashtag" to HighlightKind.TAG),
            actual = shown(text = "#hashtag"),
            message = "A # without a space is a tag, not a heading"
        )
    }

    @Test
    fun `emphasis, code, links and tags are found inside a line`() {
        assertEquals(
            expected = listOf(
                "`" to HighlightKind.SYNTAX,
                "code" to HighlightKind.CODE_INLINE,
                "`" to HighlightKind.SYNTAX,
                "[[" to HighlightKind.SYNTAX,
                "Arepas" to HighlightKind.WIKI_LINK,
                "]]" to HighlightKind.SYNTAX,
                "[" to HighlightKind.SYNTAX,
                "site" to HighlightKind.LINK,
                "](https://a.b)" to HighlightKind.SYNTAX,
                "**" to HighlightKind.SYNTAX,
                "bold" to HighlightKind.BOLD,
                "**" to HighlightKind.SYNTAX,
                "_" to HighlightKind.SYNTAX,
                "soft" to HighlightKind.ITALIC,
                "_" to HighlightKind.SYNTAX,
                "#recipe" to HighlightKind.TAG
            ),
            actual = shown(text = "`code` [[Arepas]] [site](https://a.b) **bold** _soft_ #recipe")
        )
    }

    @Test
    fun `nothing is highlighted inside code or a link target`() {
        assertEquals(
            expected = listOf(
                "`" to HighlightKind.SYNTAX,
                "**not bold** #not-a-tag" to HighlightKind.CODE_INLINE,
                "`" to HighlightKind.SYNTAX,
                "[" to HighlightKind.SYNTAX,
                "x" to HighlightKind.LINK,
                "](https://a.b/#anchor_one_)" to HighlightKind.SYNTAX
            ),
            actual = shown(text = "`**not bold** #not-a-tag` [x](https://a.b/#anchor_one_)")
        )
    }

    @Test
    fun `fenced code blocks are code from fence to fence, and nothing inside is searched`() {
        val text = "Before\n```kotlin\n# not a heading\n**not bold**\n```\nAfter #tag"

        assertEquals(
            expected = listOf(
                "```kotlin" to HighlightKind.CODE_BLOCK,
                "# not a heading" to HighlightKind.CODE_BLOCK,
                "**not bold**" to HighlightKind.CODE_BLOCK,
                "```" to HighlightKind.CODE_BLOCK,
                "#tag" to HighlightKind.TAG
            ),
            actual = shown(text = text)
        )
    }

    @Test
    fun `spaces ending a code line keep the text's font, so the cursor moves past them`() {
        assertEquals(
            expected = listOf("```" to HighlightKind.CODE_BLOCK, "val" to HighlightKind.CODE_BLOCK),
            actual = shown(text = "```\nval \n").filter { it.second == HighlightKind.CODE_BLOCK }
        )
    }

    @Test
    fun `code blocks span from fence to fence, an unclosed one to the end`() {
        val text = "Intro\n```\nval a\n\n```\nText\n~~~\nopen"

        assertEquals(
            expected = listOf("```\nval a\n\n```", "~~~\nopen"),
            actual = highlighter.codeBlocks(text = text).map { text.substring(range = it) }
        )
    }

    @Test
    fun `quote, list and task markers are muted`() {
        assertEquals(
            expected = listOf(
                "> " to HighlightKind.SYNTAX,
                "- " to HighlightKind.SYNTAX,
                "- [ ] " to HighlightKind.SYNTAX
            ),
            actual = shown(text = "> Quote\n- Item\n- [ ] Task")
        )
    }

    @Test
    fun `look-alikes stay plain text`() {
        val plain = listOf(
            "2 * 3 * 4",
            "snake_case_name and file_name.md",
            "Issue #42",
            "C# and a&#38;b",
            "** not bold **",
            "`unclosed code",
            "[[unclosed link"
        )

        assertEquals(
            expected = plain.map { emptyList<Pair<String, HighlightKind>>() },
            actual = plain.map { shown(text = it) }
        )
    }

    @Test
    fun `an embed and a bold link keep both styles`() {
        assertEquals(
            expected = listOf(
                "![[" to HighlightKind.SYNTAX,
                "chart.png" to HighlightKind.WIKI_LINK,
                "]]" to HighlightKind.SYNTAX,
                "[" to HighlightKind.SYNTAX,
                "**docs**" to HighlightKind.LINK,
                "](a.md)" to HighlightKind.SYNTAX,
                "**" to HighlightKind.SYNTAX,
                "docs" to HighlightKind.BOLD,
                "**" to HighlightKind.SYNTAX
            ),
            actual = shown(text = "![[chart.png]] [**docs**](a.md)")
        )
    }

    private fun shown(
        text: String
    ): List<Pair<String, HighlightKind>> = highlighter.highlight(text = text).map {
        text.substring(startIndex = it.start, endIndex = it.end) to it.kind
    }
}
