package app.notekea.graph.presentation

import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.graph.domain.GraphNode
import app.notekea.graph.domain.GraphPoint
import app.notekea.graph.domain.NoteGraph
import app.notekea.graph.presentation.viewmodel.dotsOf
import app.notekea.graph.presentation.viewmodel.matchesOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** What the graph tab draws. */
class GraphReducersTest {
    private val matcher = FuzzyMatcher(textFolder = TextFolder())

    @Test
    fun `notes with as many links are smaller on a farther ring`() {
        val paths = listOf(NEAR, FAR)

        val dots = dotsOf(
            graph = NoteGraph(
                nodes = paths.map {
                    GraphNode(
                        path = it,
                        links = 4
                    )
                },
                edges = emptyList()
            ),
            points = paths.associateWith {
                GraphPoint(
                    x = 0f,
                    y = 0f
                )
            },
            depths = mapOf(NEAR to 1, FAR to 2)
        )

        assertTrue(
            actual = dots.first {
                it.path == FAR
            }.radius < dots.first {
                it.path == NEAR
            }.radius
        )
    }

    @Test
    fun `matches go best first, ignore accents and need a search`() {
        val paths = listOf(FAR, NEAR, CAFE)

        assertEquals(
            expected = listOf(NEAR),
            actual = matchesOf(
                query = "are",
                paths = paths,
                matcher = matcher
            )
        )

        assertEquals(
            expected = listOf(CAFE),
            actual = matchesOf(
                query = "cafe",
                paths = paths,
                matcher = matcher
            )
        )

        assertEquals(
            expected = emptyList(),
            actual = matchesOf(
                query = " ",
                paths = paths,
                matcher = matcher
            )
        )
    }

    private companion object {
        val NEAR = RelativePath(value = "Arepas.md")
        val FAR = RelativePath(value = "Menu.md")
        val CAFE = RelativePath(value = "Café.md")
    }
}
