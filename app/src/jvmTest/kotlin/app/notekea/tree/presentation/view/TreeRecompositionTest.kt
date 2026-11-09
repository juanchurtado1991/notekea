package app.notekea.tree.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionTracer
import androidx.compose.runtime.InternalComposeTracingApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.SidePanelPreferences
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.sidepanel.SidePanel
import app.notekea.editor.presentation.sidepanel.SidePanelActions
import app.notekea.tree.presentation.model.TreePreviewData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What a change recomposes, counted with Compose's tracer: moving the tree's selection touches the two rows concerned,
 * not all of them, and the editor's scrolling moves the outline's highlight without recomposing the side panel.
 */
@OptIn(InternalComposeTracingApi::class)
class TreeRecompositionTest {
    private var frame = 0L

    @Test
    fun `moving the selection recomposes only the two rows it leaves and reaches`() {
        val rows = TreePreviewData.state().rows

        var state by mutableStateOf(
            value = TreePreviewData.state().copy(selectedPath = rows.first().path)
        )

        val traced = mutableListOf<String>()

        UiThread.run {
            ImageComposeScene(
                width = WIDTH,
                height = HEIGHT,
                density = Density(density = 1f)
            ) {
                NotekeaTheme(darkTheme = false) {
                    TreeList(
                        state = state,
                        onIntent = {}
                    )
                }
            }.use { scene ->
                settle(scene = scene)

                tracing(into = traced) {
                    state = state.copy(selectedPath = rows.last().path)
                    settle(scene = scene)
                }
            }
        }

        assertTrue(
            actual = rows.size > 2,
            message = "the preview tree has ${rows.size} rows"
        )

        assertEquals(
            expected = 2,
            actual = traced.count { it.contains(other = ".TreeEntryRow (") }
        )
    }

    @Test
    fun `the view scrolling to another heading never recomposes the side panel`() {
        var current by mutableIntStateOf(value = 0)
        val traced = mutableListOf<String>()

        UiThread.run {
            ImageComposeScene(
                width = WIDTH,
                height = HEIGHT,
                density = Density(density = 1f)
            ) {
                NotekeaTheme(darkTheme = false) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SidePanel(
                            outline = OutlineFinder().headings(text = BODY),
                            current = { current },
                            preferences = SidePanelPreferences.DEFAULT,
                            platformConventions = PreviewPlatformConventions.linux,
                            actions = SidePanelActions(
                                onHeadingClick = {},
                                onModeSelect = {},
                                onSectionToggle = {},
                                onHide = {},
                                onLeave = {}
                            ),
                            info = null
                        )
                    }
                }
            }.use { scene ->
                settle(scene = scene)

                tracing(into = traced) {
                    current = 1
                    settle(scene = scene)
                }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = traced.filter {
                it.contains(other = ".SidePanel (") || it.contains(other = ".OutlineList (")
            }
        )
    }

    private fun tracing(
        into: MutableList<String>,
        block: () -> Unit
    ) {
        Composer.setTracer(
            object : CompositionTracer {
                override fun isTraceInProgress(): Boolean = true

                override fun traceEventEnd() = Unit

                override fun traceEventStart(
                    key: Int,
                    dirty1: Int,
                    dirty2: Int,
                    info: String
                ) {
                    into += info
                }
            }
        )

        try {
            block()
        } finally {
            Composer.setTracer(
                object : CompositionTracer {
                    override fun isTraceInProgress(): Boolean = false

                    override fun traceEventEnd() = Unit

                    override fun traceEventStart(
                        key: Int,
                        dirty1: Int,
                        dirty2: Int,
                        info: String
                    ) = Unit
                }
            )
        }
    }

    private fun settle(scene: ImageComposeScene) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val BODY = "# One\n\n## Two\n\n## Three\n"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 480
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 340
    }
}
