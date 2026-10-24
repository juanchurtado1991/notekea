package app.notekea.notes.data

import app.notekea.notes.domain.NoteMetadata
import kotlin.test.Test
import kotlin.test.assertEquals

class GhostFrontmatterParserTest {
    private val parser = GhostFrontmatterParser()

    @Test
    fun `title, flow and block lists are read`() {
        val yaml =
            """
            title: Pricing ideas
            tags: [product, "pricing plans"]
            aliases:
              - Prices
              - Café prices
            """.trimIndent()

        assertEquals(
            expected = NoteMetadata(
                tags = listOf("product", "pricing plans"),
                aliases = listOf("Prices", "Café prices"),
                title = "Pricing ideas"
            ),
            actual = parser.parse(yaml = yaml)
        )
    }

    @Test
    fun `text values are split, hashes dropped and legacy singular keys read`() {
        val yaml =
            """
            tag: "#recipe, #venezuelan dinner"
            alias: Arepa, Arepas reina
            """.trimIndent()

        assertEquals(
            expected = NoteMetadata(
                tags = listOf("recipe", "venezuelan", "dinner"),
                aliases = listOf("Arepa", "Arepas reina"),
                title = null
            ),
            actual = parser.parse(yaml = yaml)
        )
    }

    @Test
    fun `other keys of any shape do not get in the way, nor nested keys with the same name`() {
        val yaml =
            """
            created: 2026-10-18
            servings: 6
            source: {book: Cocina, page: 12}
            notes: |
              tags: not-a-tag
            tags:
              - garden
            title: 2026
            """.trimIndent()

        assertEquals(
            expected = NoteMetadata(tags = listOf("garden"), aliases = emptyList(), title = "2026"),
            actual = parser.parse(yaml = yaml)
        )
    }

    @Test
    fun `a value of the wrong shape is ignored without losing the other keys`() {
        val yaml =
            """
            tags: {a: b}
            aliases: [unclosed
            title: "   "
            """.trimIndent()

        assertEquals(
            expected = NoteMetadata.NONE,
            actual = parser.parse(yaml = yaml)
        )
        assertEquals(
            expected = "Kept",
            actual = parser.parse(yaml = "tags: {a: b}\ntitle: Kept").title
        )
    }

    @Test
    fun `duplicates and empty entries are dropped`() {
        assertEquals(
            expected = listOf("a", "b"),
            actual = parser.parse(yaml = "tags: [a, \"#a\", \"\", b, a]").tags
        )
    }
}
