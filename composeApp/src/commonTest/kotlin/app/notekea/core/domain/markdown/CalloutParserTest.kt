package app.notekea.core.domain.markdown

import kotlin.test.Test
import kotlin.test.assertEquals

class CalloutParserTest {
    private val parser = CalloutParser()

    @Test
    fun `a header gives the kind, the title and how it folds`() {
        assertEquals(
            expected = listOf(
                CalloutHeader(kind = CalloutKind.TIP, title = "Soak the beans", fold = CalloutFold.NONE),
                CalloutHeader(kind = CalloutKind.WARNING, title = "Hot oil", fold = CalloutFold.COLLAPSED),
                CalloutHeader(kind = CalloutKind.NOTE, title = "Note", fold = CalloutFold.EXPANDED)
            ),
            actual = listOf("[!tip] Soak the beans", "[!WARNING]- Hot oil", "[!note]+").map { parser.header(line = it) }
        )
    }

    @Test
    fun `aliases mean their type, the alias is the title, and unknown types are notes`() {
        assertEquals(
            expected = listOf(
                CalloutKind.WARNING to "Caution",
                CalloutKind.ABSTRACT to "Tldr",
                CalloutKind.DANGER to "Error",
                CalloutKind.NOTE to "Recipe"
            ),
            actual = listOf("[!caution]", "[!tldr]", "[!error]", "[!recipe]").map { line ->
                parser.header(line = line)!!.let { it.kind to it.title }
            }
        )
    }

    @Test
    fun `lines that are not a callout header are not read as one`() {
        assertEquals(
            expected = listOf(null, null, null),
            actual = listOf("A quote", "[!] empty", "[link](url)").map { parser.header(line = it) }
        )
    }

    @Test
    fun `every callout header becomes a paragraph of its own, nested ones too, never in code`() {
        val markdown = "> [!note] Title\n> Body\n>\n> > [!tip]- Inner\n> > More\n```\n> [!bug] In code\n```"

        assertEquals(
            expected = "> [!note] Title\n>\n> Body\n>\n> > [!tip]- Inner\n> >\n> > More\n```\n> [!bug] In code\n```",
            actual = parser.separateHeaders(markdown = markdown)
        )
    }
}
