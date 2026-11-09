package app.notekea.graph.domain

import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The graph's shape and places (`ROADMAP.md` 2.7), on a small vault. */
class NoteGraphTest {
    private val graph = NoteGraph(
        nodes = listOf(A, B, C, D, ORPHAN).map { GraphNode(path = it, links = 1) },
        edges = listOf(GraphEdge.between(A, B), GraphEdge.between(B, C), GraphEdge.between(C, D))
    )

    @Test
    fun `depths count the links away, and stop where asked`() {
        assertEquals(
            expected = listOf(mapOf(A to 0, B to 1), mapOf(A to 0, B to 1, C to 2)),
            actual = listOf(graph.depthsFrom(center = A, depth = 1), graph.depthsFrom(center = A, depth = 2))
        )
    }

    @Test
    fun `an edge has no direction, and related notes never replace a link`() {
        val related = graph.withRelated(center = A, related = listOf(B, D))

        assertEquals(expected = GraphEdge.between(A, B), actual = GraphEdge.between(B, A))
        assertEquals(
            expected = listOf(GraphEdge(A, D, isRelated = true)),
            actual = related.edges.filter { it.isRelated }
        )
    }

    @Test
    fun `a link comes and goes with the count of both notes`() {
        val linked = graph.withLink(one = A, other = D)

        assertTrue(actual = GraphEdge.between(A, D) in linked.edges)
        assertEquals(expected = 2, actual = linked.nodes.first { it.path == A }.links)
        assertEquals(expected = graph, actual = linked.withoutLink(one = D, other = A))
    }

    @Test
    fun `folders and orphans can be left out`() {
        assertEquals(
            expected = listOf(listOf(A, B, D, ORPHAN), listOf(A, B, C, D)),
            actual = listOf(
                graph.withoutFolders(folders = setOf("Work")).nodes.map { it.path },
                graph.withoutOrphans().nodes.map { it.path }
            )
        )
    }

    @Test
    fun `rings put the center in the middle and each ring farther out, the same way every time`() {
        val depths = graph.depthsFrom(center = A, depth = 2)
        val around = graph.only(paths = depths.keys)

        val points = GraphLayout().rings(graph = around, depths = depths)

        assertEquals(expected = GraphPoint(x = 0f, y = 0f), actual = points[A])
        assertTrue(actual = distance(points.getValue(C)) > distance(points.getValue(B)))
        assertEquals(expected = points, actual = GraphLayout().rings(graph = around, depths = depths))
    }

    @Test
    fun `a spread keeps linked notes closer than the rest, and a large vault stays fast`() = runTest {
        val points = GraphLayout().spread(graph = graph)
        val large = NoteGraph(
            nodes = (0 until 2_000).map { GraphNode(path = RelativePath(value = "$it.md"), links = 0) },
            edges = emptyList()
        )

        val spread = GraphLayout().spread(graph = large)

        assertTrue(actual = gap(points, A, B) < gap(points, A, D))
        assertEquals(expected = 2_000, actual = spread.size)
    }

    private fun distance(
        point: GraphPoint
    ): Float = point.x * point.x + point.y * point.y

    private fun gap(
        points: Map<RelativePath, GraphPoint>,
        one: RelativePath,
        other: RelativePath
    ): Float {
        val first = points.getValue(one)
        val second = points.getValue(other)
        return distance(GraphPoint(x = first.x - second.x, y = first.y - second.y))
    }

    private companion object {
        val A = RelativePath(value = "A.md")
        val B = RelativePath(value = "B.md")
        val C = RelativePath(value = "Work/C.md")
        val D = RelativePath(value = "D.md")
        val ORPHAN = RelativePath(value = "Orphan.md")
    }
}
