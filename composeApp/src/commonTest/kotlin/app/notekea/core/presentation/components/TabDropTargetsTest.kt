package app.notekea.core.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A tab dragged along its row (`ROADMAP.md` 1.11): the others slide aside by its width to open the place it would take,
 * it stays on the row's line while the pointer is on the row, follows the pointer off it, and glides into its place.
 */
class TabDropTargetsTest {
    private val targets = TabDropTargets().apply {
        row = Rect(left = 0f, top = 0f, right = ROW_WIDTH, bottom = ROW_HEIGHT)
        (0..2).forEach {
            places[it] =
                Rect(left = it * TAB_WIDTH, top = 0f, right = (it + 1) * TAB_WIDTH, bottom = ROW_HEIGHT)
        }
    }

    @Test
    fun `the tabs between the dragged one and the place it would take slide aside by its width`() {
        val right = drag(index = 0, travel = Offset(x = 2.2f * TAB_WIDTH, y = 0f))
        val left = drag(index = 2, travel = Offset(x = -2.2f * TAB_WIDTH, y = 0f))

        assertEquals(
            expected = listOf(2, 0) to listOf(-TAB_WIDTH, -TAB_WIDTH, TAB_WIDTH, TAB_WIDTH, 0f),
            actual = listOf(targets.slotOf(drag = right), targets.slotOf(drag = left)) to listOf(
                targets.shiftOf(index = 1, drag = right),
                targets.shiftOf(index = 2, drag = right),
                targets.shiftOf(index = 0, drag = left),
                targets.shiftOf(index = 1, drag = left),
                targets.shiftOf(index = 1, drag = drag(index = 0, travel = Offset(x = 0.2f * TAB_WIDTH, y = 0f)))
            )
        )
    }

    @Test
    fun `the dragged tab keeps to the row's line over the row and follows the pointer off it, moving no other tab`() {
        val along = drag(index = 0, travel = Offset(x = TAB_WIDTH, y = ROW_HEIGHT / 4))
        val off = drag(index = 0, travel = Offset(x = TAB_WIDTH, y = ROW_HEIGHT * 3))

        assertEquals(
            expected = listOf(Offset(x = TAB_WIDTH, y = 0f), Offset(x = TAB_WIDTH, y = ROW_HEIGHT * 3)) to 0f,
            actual = listOf(targets.offsetOf(drag = along), targets.offsetOf(drag = off)) to
                targets.shiftOf(index = 1, drag = off)
        )
    }

    @Test
    fun `once let go, the tab glides into its place and that place holds while it moves`() {
        val drag = drag(index = 0, travel = Offset(x = 1.6f * TAB_WIDTH, y = 0f)).apply { landing = 1 }
        drag.travel = Offset(x = TAB_WIDTH * 2, y = 0f)

        assertEquals(
            expected = listOf(TAB_WIDTH, -TAB_WIDTH * 2) to 1,
            actual = listOf(
                targets.glideTo(slot = 1, drag = drag),
                targets.glideTo(slot = 0, drag = drag(index = 2, travel = Offset.Zero))
            ) to targets.slotOf(drag = drag)
        )
    }

    private fun drag(
        index: Int,
        travel: Offset
    ) = TabDrag(
        index = index,
        keys = KEYS,
        start = Offset(x = (index + HALF) * TAB_WIDTH, y = ROW_HEIGHT / 2)
    ).apply { this.travel = travel }

    companion object {
        private const val HALF = 0.5f
        private const val ROW_HEIGHT = 34f
        private const val ROW_WIDTH = 600f
        private const val TAB_WIDTH = 100f
        private val KEYS = listOf("a", "b", "c")
    }
}
