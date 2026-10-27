package app.notekea.notes.presentation.editor

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.SidePanelSection
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The side panel as an inspector (`ROADMAP.md` 1.9, 1.14), rendered headless: each section's header folds it, a
 * heading is chosen in the outline, and the hide button hides the panel.
 */
class SidePanelScreenTest {
    private var frame = 0L

    @Test
    fun `the section headers fold their sections, a heading is chosen and the panel is hidden`() {
        val actions = mutableListOf<Any>()
        UiThread.run {
            scene(actions = actions).use { scene ->
                settle(scene = scene)
                listOf(STEPS_ROW, NOTE_INFO_HEADER, OUTLINE_HEADER, HIDE).forEach {
                    click(scene = scene, position = it)
                }
            }
        }

        assertEquals(
            expected = listOf<Any>(3, SidePanelSection.NOTE_INFO, SidePanelSection.OUTLINE, HIDDEN),
            actual = actions
        )
    }

    private fun scene(
        actions: MutableList<Any>
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        NotekeaTheme(darkTheme = false) {
            SidePanel(
                outline = OutlineFinder().headings(text = BODY),
                current = 0,
                foldedSections = emptySet(),
                platformConventions = PreviewPlatformConventions.linux,
                actions = SidePanelActions(
                    onHeadingClick = { actions += it },
                    onSectionToggle = { actions += it },
                    onHide = { actions += HIDDEN },
                    onLeave = {}
                ),
                info = EditorPreviewData.info
            )
        }
    }

    private fun click(
        scene: ImageComposeScene,
        position: Offset
    ) {
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
        settle(scene = scene)
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val BODY =
            "# Arepas guide\n\n## Ingredients\n\n### For the filling\n\n## Steps\n\n### Cooking\n\n## Serving\n"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 560
        private const val HIDDEN = "hidden"
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 340
        private val HIDE = Offset(x = 322f, y = 18f)
        private val NOTE_INFO_HEADER = Offset(x = 60f, y = 239f)
        private val OUTLINE_HEADER = Offset(x = 60f, y = 18f)
        private val STEPS_ROW = Offset(x = 80f, y = 133f)
    }
}
