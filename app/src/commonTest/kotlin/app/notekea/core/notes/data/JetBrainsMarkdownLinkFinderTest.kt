package app.notekea.core.notes.data

import app.notekea.core.notes.domain.links.LinkKind
import kotlin.test.Test
import kotlin.test.assertEquals

class JetBrainsMarkdownLinkFinderTest {
    private val finder = JetBrainsMarkdownLinkFinder()

    @Test
    fun `wiki links keep only their target, whatever comes with it`() {
        val markdown = "See [[Arepas]], [[Recipes/Arepas|my arepas]], [[Arepas#Dough]], " +
            "![[Garden plan^tasks]] and [[Café con leche.md]]."

        assertEquals(
            expected = listOf(
                "Arepas",
                "Recipes/Arepas",
                "Arepas",
                "Garden plan",
                "Café con leche.md"
            ),
            actual = finder.find(markdown = markdown).map { it.target }
        )
    }

    @Test
    fun `the target range points exactly at the target text`() {
        val markdown = "Read [[Reading list|the list]] first."

        val link = finder.find(markdown = markdown).single()

        assertEquals(
            expected = "Reading list",
            actual = markdown.substring(
                startIndex = link.targetStart,
                endIndex = link.targetEnd
            )
        )
    }

    @Test
    fun `markdown links to notes are found in both forms, web links are not`() {
        val markdown =
            "[trip](Travel/Lisbon%20trip.md#Day%201), [coffee](<Recipes/Café con leche.md>), " +
                "[site](https://example.com/notes.md), [image](photo.png)"

        assertEquals(
            expected = listOf(
                "Travel/Lisbon%20trip.md" to LinkKind.MARKDOWN_ENCODED,
                "Recipes/Café con leche.md" to LinkKind.MARKDOWN_ANGLE
            ),
            actual = finder.find(markdown = markdown).map {
                it.target to it.kind
            }
        )
    }

    // Audit: in a table the target came out as `Note\`, so the link had no backlink and renames missed it.
    @Test
    fun `the backslash of a wiki link's escaped bar in a table is not part of its target`() {
        val markdown = "| [[Note\\|alias]] | ![[Other#Part\\|alias]] |"

        val links = finder.find(markdown = markdown)

        assertEquals(
            expected = listOf("Note", "Other"),
            actual = links.map { it.target }
        )

        assertEquals(
            expected = "Note",
            actual = markdown.substring(
                startIndex = links[0].targetStart,
                endIndex = links[0].targetEnd
            )
        )
    }

    // Audit: an angle link with no `)` was found, and the mentions that looked for its end crashed.
    @Test
    fun `an angle link must be closed, and may carry a title`() {
        assertEquals(
            expected = emptyList(),
            actual = finder.find(markdown = "[x](<Target.md>")
        )

        assertEquals(
            expected = listOf("Target.md"),
            actual = finder.find(markdown = "[x](<Target.md> \"A title\")").map { it.target }
        )
    }

    // Bug hunt 2026-11-17 (G5): `[text](Note)` opened the note on a click and was missing from the graph.
    @Test
    fun `a markdown link with no extension or with a title is a note link, a page or a heading is not`() {
        val markdown = "[a](Arepas), [b](Recipes/Menu.md \"The menu\"), [c](<Garden plan>), " +
            "[d](www.example.com), [e](mailto:me), [f](#Steps)"

        assertEquals(
            expected = listOf("Arepas", "Recipes/Menu.md", "Garden plan"),
            actual = finder.find(markdown = markdown).map { it.target }
        )
    }

    // Bug hunt 2026-11-17 (G8): only the third kind of link was checked for a URL scheme.
    @Test
    fun `a web page in angle brackets is no note link, even when it ends in md`() {
        assertEquals(
            expected = emptyList(),
            actual = finder.find(markdown = "[site](<https://example.com/README.md>)")
        )
    }

    // Bug hunt 2026-11-17 (A5): links and embeds to attachments were never found, so a rename broke them.
    @Test
    fun `with attachments, links and embeds to any file of the vault are found, web pages still are not`() {
        val markdown = "![pic](assets/pic.png), [doc](<Files/Plan v2.pdf>), ![[assets/pic.png]], " +
            "[site](https://example.com/pic.png)"

        assertEquals(
            expected = listOf("assets/pic.png", "Files/Plan v2.pdf", "assets/pic.png"),
            actual = finder.findWithAttachments(markdown = markdown).map { it.target }
        )

        assertEquals(
            expected = listOf("assets/pic.png"),
            actual = finder.find(markdown = markdown).map { it.target }
        )
    }

    @Test
    fun `a label with escaped brackets still makes a link`() {
        assertEquals(
            expected = listOf("C%5B1%5D.md"),
            actual = finder.find(markdown = "[C\\[1\\]](C%5B1%5D.md)").map { it.target }
        )
    }

    @Test
    fun `links inside code are left alone`() {
        val markdown =
            """
            Real [[Arepas]].

            ```
            [[Not a link]]
            ```

                [[Indented code]]

            Inline `[[Code span]]` and [[Pasta e fagioli]].
            """.trimIndent()

        assertEquals(
            expected = listOf("Arepas", "Pasta e fagioli"),
            actual = finder.find(markdown = markdown).map { it.target }
        )
    }

    @Test
    fun `links in frontmatter properties are found too`() {
        val markdown = "---\nrelated: \"[[Lisbon trip]]\"\n---\n\n# Packing\n"

        assertEquals(
            expected = listOf("Lisbon trip"),
            actual = finder.find(markdown = markdown).map { it.target }
        )
    }
}
