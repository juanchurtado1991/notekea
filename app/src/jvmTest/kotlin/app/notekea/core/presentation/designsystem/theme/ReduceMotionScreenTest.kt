package app.notekea.core.presentation.designsystem.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.components.NotekeaSplitPane
import kotlin.test.Test
import kotlin.test.assertEquals

/** With reduce motion on, by the setting or the system, a panel closes at once instead of sliding (audit 50). */
class ReduceMotionScreenTest {
    @Test
    fun `motion is reduced by the setting or by the system, and otherwise not`() {
        val reduced = listOf(
            false to false,
            true to false,
            false to true
        ).map { (setting, system) ->
            var isReduced = false

            UiThread.run {
                ImageComposeScene(
                    width = SIZE,
                    height = SIZE
                ) {
                    CompositionLocalProvider(
                        LocalAppearance provides Appearance(reduceMotion = setting),
                        LocalSystemReduceMotion provides system
                    ) {
                        NotekeaTheme { isReduced = LocalReduceMotion.current }
                    }
                }.use { it.render() }
            }

            isReduced
        }

        assertEquals(
            expected = listOf(false, true, true),
            actual = reduced
        )
    }

    @Test
    fun `a panel slides closed, and closes at once with motion reduced`() {
        assertEquals(
            expected = listOf(true, false),
            actual = listOf(false, true).map { reduceMotion ->
                isSlidingAfterClose(reduceMotion = reduceMotion)
            }
        )
    }

    /** Whether the content, one frame after the panel is told to close, has not reached the pane's whole width yet. */
    private fun isSlidingAfterClose(reduceMotion: Boolean): Boolean {
        var isVisible by mutableStateOf(value = true)
        var contentWidth = 0
        var frame = 0L

        UiThread.run {
            ImageComposeScene(
                width = SIZE,
                height = SIZE,
                density = Density(density = 1f)
            ) {
                CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                    NotekeaSplitPane(
                        panelWidth = PANEL_WIDTH.dp,
                        onPanelWidthChange = {},
                        isPanelVisible = isVisible,
                        minPanelWidth = PANEL_WIDTH.dp,
                        maxPanelWidth = PANEL_WIDTH.dp,
                        panel = { Text(text = PANEL) },
                        content = {
                            Box(
                                modifier = Modifier.fillMaxSize().onSizeChanged {
                                    contentWidth =
                                        it.width
                                }
                            )
                        }
                    )
                }
            }.use { scene ->
                scene.render(nanoTime = frame++ * FRAME_NANOS)
                isVisible = false

                repeat(times = FRAMES) { scene.render(nanoTime = frame++ * FRAME_NANOS) }
            }
        }

        return contentWidth < SIZE
    }

    private companion object {
        const val FRAMES = 3
        const val FRAME_NANOS = 16_000_000L
        const val PANEL = "Panel"
        const val PANEL_WIDTH = 100
        const val SIZE = 400
    }
}
