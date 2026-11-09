package app.notekea.core.notes.data

import app.notekea.core.notes.domain.LinkKind
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
