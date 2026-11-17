package app.notekea.graph.presentation

import androidx.compose.ui.geometry.Offset
import app.notekea.core.notes.domain.RelativePath
import app.notekea.graph.presentation.model.GraphDot
import app.notekea.graph.presentation.view.noteAt
import kotlin.test.Test
import kotlin.test.assertEquals

/** Which dot a click picks where several overlap. */
class GraphHitTest {
    @Test
    fun `a click where dots overlap picks the one drawn on top, not the last in the vault`() {
        val center = dotOf(
            path = CENTER,
            depth = 0
        )

        val neighbor = dotOf(
            path = NEIGHBOR,
            depth = 1
        )

        val picked = noteAt(
            point = Offset.Zero,
            dots = listOf(center, neighbor),
            places = mapOf(CENTER to Offset.Zero, NEIGHBOR to Offset(2f, 0f)),
            scale = 1f
        )

        assertEquals(
            expected = CENTER,
            actual = picked
        )
    }

    private fun dotOf(
        path: RelativePath,
        depth: Int
    ) = GraphDot(
        path = path,
        name = path.nameWithoutExtension,
        folder = "",
        x = 0f,
        y = 0f,
        radius = 10f,
        depth = depth
    )

    private companion object {
        val CENTER = RelativePath(value = "Center.md")
        val NEIGHBOR = RelativePath(value = "Neighbor.md")
    }
}
