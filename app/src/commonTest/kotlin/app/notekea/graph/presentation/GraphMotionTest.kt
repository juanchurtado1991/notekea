package app.notekea.graph.presentation

import androidx.compose.ui.geometry.Offset
import app.notekea.core.notes.domain.RelativePath
import app.notekea.graph.presentation.model.GraphDot
import app.notekea.graph.presentation.view.GraphMotion
import kotlin.test.Test
import kotlin.test.assertEquals

/** How the map moves between two sets of notes. */
class GraphMotionTest {
    @Test
    fun `the first notes arrive at once`() {
        val motion = GraphMotion.after(
            dots = listOf(dotAt(A, x = 40f)),
            previous = null,
            center = null
        )

        assertEquals(
            expected = Offset(
                x = 40f,
                y = 0f
            ),
            actual = motion.placeOf(dot = dotAt(A, x = 40f))
        )
    }

    @Test
    fun `a note found on the vault leads its neighbors out of its place, and the rest fade`() {
        val vault = GraphMotion.after(
            listOf(dotAt(A, x = 300f), dotAt(B, x = -80f)),
            null,
            center = null
        )

        val dots = listOf(dotAt(A, x = 0f), dotAt(C, x = 170f))

        val around = GraphMotion.after(
            dots = dots,
            previous = vault,
            center = A
        )

        assertEquals(
            expected = Offset(
                x = 300f,
                y = 0f
            ),
            actual = around.placeOf(dot = dotAt(A, x = 0f))
        )

        assertEquals(
            expected = Offset(
                x = 300f,
                y = 0f
            ),
            actual = around.placeOf(dot = dotAt(C, x = 170f))
        )

        assertEquals(
            expected = listOf(B),
            actual = around.leaving.map { it.path }
        )
    }

    @Test
    fun `notes come out of the middle when nothing was on the map`() {
        val empty = GraphMotion.after(
            dots = emptyList(),
            previous = null,
            center = null
        )

        val around = GraphMotion.after(
            dots = listOf(dotAt(C, x = 170f)),
            previous = empty,
            center = A
        )

        assertEquals(
            expected = Offset.Zero,
            actual = around.placeOf(dot = dotAt(C, x = 170f))
        )
    }

    private fun dotAt(
        path: RelativePath,
        x: Float
    ) = GraphDot(
        path,
        name = path.nameWithoutExtension,
        folder = "",
        x = x,
        y = 0f,
        radius = 10f,
        depth = 0
    )

    private companion object {
        val A = RelativePath(value = "A.md")
        val B = RelativePath(value = "B.md")
        val C = RelativePath(value = "C.md")
    }
}
