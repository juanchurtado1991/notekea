package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class OutlineFinderTest {
    private val finder = OutlineFinder()

    @Test
    fun `headings are found in order with their level, text as it reads and where their line starts`() {
        val text = "# Arepas\n\nIntro\n## **Dough** and [[Fillings|fillings]]\n### 1. Mix ###\n"

        assertEquals(
            expected = listOf(
                OutlineHeading(level = 1, text = "Arepas", offset = 0, depth = 0),
                OutlineHeading(level = 2, text = "Dough and fillings", offset = 16, depth = 1),
                OutlineHeading(level = 3, text = "1. Mix", offset = 55, depth = 2)
            ),
            actual = finder.headings(text = text)
        )
    }

    @Test
    fun `a heading is one step deeper than the lower levels open above it, as in Obsidian`() {
        val text = "### Notes\n# Title\n### Skipped a level\n## Section\n#### Deep\n# Next"

        assertEquals(
            expected = listOf(0, 0, 1, 1, 2, 0),
            actual = finder.headings(text = text).map { it.depth }
        )
    }

    @Test
    fun `code blocks, tags, empty headings and seven marks are not headings`() {
        val text = "```\n# not a heading\n```\n#tag\n#\n## \n####### seven\n~~~~\n# still code\n~~~\n~~~~\n## After"

        assertEquals(
            expected = listOf("After"),
            actual = finder.headings(text = text).map { it.text }
        )
    }

    @Test
    fun `windows line endings are read like the others`() {
        assertEquals(
            expected = listOf(OutlineHeading(level = 2, text = "Dairy", offset = 7, depth = 0)),
            actual = finder.headings(text = "Intro\r\n## Dairy\r\nCheese")
        )
    }
}
