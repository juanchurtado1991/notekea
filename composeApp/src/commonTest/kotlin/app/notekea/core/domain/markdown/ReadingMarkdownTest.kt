package app.notekea.core.domain.markdown

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingMarkdownTest {
    private val reading = ReadingMarkdown()

    @Test
    fun `every callout header becomes a paragraph of its own, nested ones too, never in code`() {
        val markdown = "> [!note] Title\n> Body\n>\n> > [!tip]- Inner\n> > More\n```\n> [!bug] In code\n```"

        assertEquals(
            expected = "> [!note] Title\n>\n> Body\n>\n> > [!tip]- Inner\n> >\n> > More\n```\n> [!bug] In code\n```",
            actual = reading.prepare(markdown = markdown)
        )
    }

    @Test
    fun `code inside quotes loses only as many quote marks as it is deep`() {
        assertEquals(
            expected = listOf("val servings = 6\nprintln(servings)", "> kept\ncode", "no quotes"),
            actual = listOf(
                reading.unquoted(code = "> val servings = 6\n> println(servings)", depth = 1),
                reading.unquoted(code = "> > > kept\n> > code", depth = 2),
                reading.unquoted(code = "no quotes", depth = 0)
            )
        )
    }

    @Test
    fun `tasks checked with a capital X read as checked, other X stay`() {
        val markdown = "- [X] Done\n1. [X] Numbered\n> - [X] Quoted\n[X] not a task\n```\n- [X] code\n```"

        assertEquals(
            expected = "- [x] Done\n1. [x] Numbered\n> - [x] Quoted\n[X] not a task\n```\n- [X] code\n```",
            actual = reading.prepare(markdown = markdown)
        )
    }
}
