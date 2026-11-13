package app.notekea.core.presentation.platform

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import java.awt.Canvas
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The drag area of macOS's unified title bar (smoke of #142): a press on it must still reach the window's drag, which
 * waits for a press no one consumed, as `WindowDraggableArea` does, and a double click (the system's click count)
 * zooms. The drag here is that gesture, since the real one needs a window, which a headless test cannot open.
 */
class MacTitleBarTest {
    @Test
    fun `a press on the title bar moves the window and a double click zooms it`() {
        var drags = 0
        var zooms = 0

        UiThread.run {
            ImageComposeScene(
                width = SIZE,
                height = SIZE,
                density = Density(density = 1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown()
                                drags++
                            }
                        },
                    propagateMinConstraints = true
                ) {
                    MacTitleBar.TitleBarZoom(onZoom = {
                        zooms++
                    })
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)

                listOf(1, 2).forEachIndexed { index, clicks ->
                    val time = index * CLICK_GAP_MILLIS

                    scene.sendPointerEvent(
                        eventType = PointerEventType.Press,
                        position = POINT,
                        timeMillis = time,
                        nativeEvent = mouseEvent(
                            id = MouseEvent.MOUSE_PRESSED,
                            clicks = clicks
                        )
                    )

                    scene.sendPointerEvent(
                        eventType = PointerEventType.Release,
                        position = POINT,
                        timeMillis = time + 1,
                        nativeEvent = mouseEvent(
                            id = MouseEvent.MOUSE_RELEASED,
                            clicks = clicks
                        )
                    )

                    scene.render(nanoTime = (index + 1) * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = 2 to 1,
            actual = drags to zooms
        )
    }

    private fun mouseEvent(
        id: Int,
        clicks: Int
    ): MouseEvent = MouseEvent(
        Canvas(),
        id,
        0L,
        InputEvent.BUTTON1_DOWN_MASK,
        POINT.x.toInt(),
        POINT.y.toInt(),
        clicks,
        false,
        MouseEvent.BUTTON1
    )

    private companion object {
        const val CLICK_GAP_MILLIS = 100L
        const val FRAME_NANOS = 16_000_000L
        const val SIZE = 200
        val POINT = Offset(
            x = 50f,
            y = 10f
        )
    }
}
