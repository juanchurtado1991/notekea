package app.notekea.core.crash.presentation

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.crash.domain.RecordedCrash
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The crash notice rendered headless and clicked (crash report smoke: the first, Swing notice looked pixelated): each
 * button does its action, in the order the person clicks them.
 */
class CrashNoticeContentTest {
    @Test
    fun `copy, report and close each do their action`() {
        val actions = mutableListOf<String>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    CrashNoticeContent(
                        crash = RecordedCrash(text = REPORT, path = PATH),
                        buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
                        onCopy = { actions += COPY },
                        onReport = { actions += REPORT_ACTION },
                        onClose = { actions += CLOSE }
                    )
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                listOf(COPY_BUTTON, REPORT_BUTTON, CLOSE_BUTTON).forEachIndexed { index, position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = (index + 1) * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = listOf(COPY, REPORT_ACTION, CLOSE),
            actual = actions
        )
    }

    companion object {
        private const val CLOSE = "close"
        private const val COPY = "copy"
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val PATH = "/home/ana/.local/share/notekea/crashes/crash-2026-10-26T10-15-30Z.txt"
        private const val REPORT = "Notekea crash report"
        private const val REPORT_ACTION = "report"
        private const val WINDOW_HEIGHT = 480
        private const val WINDOW_WIDTH = 900
        private val CLOSE_BUTTON = Offset(x = 753f, y = 408f)
        private val COPY_BUTTON = Offset(x = 135f, y = 408f)
        private val REPORT_BUTTON = Offset(x = 370f, y = 408f)
    }
}
