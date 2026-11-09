package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A note's sections: one per heading, with the titles above it, as the outline nests them. */
class NoteSectionsTest {
    private val sections = NoteSections(outlineFinder = OutlineFinder())

    @Test
    fun `each heading starts a section whose path holds the headings of a lower level above it`() {
        val body =
            "Intro line\n# Arepas\nCorn.\n## Dough\nKnead.\n### Rest\nWait.\n## Filling\nCheese.\n# Next\nEnd."

        assertEquals(
            expected = listOf(
                emptyList(),
                listOf("Arepas"),
                listOf("Arepas", "Dough"),
                listOf("Arepas", "Dough", "Rest"),
                listOf("Arepas", "Filling"),
                listOf("Next")
            ),
            actual = sections.of(body = body).map { it.headingPath }
        )
    }

    @Test
    fun `sections cover the body from each heading's line to the next heading`() {
        val body = "Intro\n# One\nA\n# Two\nB"

        assertEquals(
            expected = listOf("Intro\n", "# One\nA\n", "# Two\nB"),
            actual = sections.of(body = body).map { body.substring(range = it.range) }
        )
    }

    @Test
    fun `nothing but blank lines before the first heading is no section, and a heading in code is not a heading`() {
        val body = "\n\n# Code\n```\n# not a heading\n```\n"

        assertEquals(
            expected = listOf(listOf("Code") to "# Code\n```\n# not a heading\n```\n"),
            actual = sections.of(body = body).map {
                it.headingPath to body.substring(range = it.range)
            }
        )
    }

    @Test
    fun `a deeper heading right under a shallower one nests one step, as Obsidian's outline does`() {
        val body = "# Top\n### Deep\nText\n## Middle\nText"

        assertEquals(
            expected = listOf(listOf("Top"), listOf("Top", "Deep"), listOf("Top", "Middle")),
            actual = sections.of(body = body).map { it.headingPath }
        )
    }
}
