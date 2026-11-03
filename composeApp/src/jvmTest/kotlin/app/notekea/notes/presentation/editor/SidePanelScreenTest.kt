package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.SidePanelMode
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The side panel as an inspector (`ROADMAP.md` 1.9, 1.14, 2.4), rendered headless: each section's header folds it, a
 * heading is chosen in the outline, Chat is chosen, the minimize button hides the panel, and Outline and Related share
 * the height Note info leaves.
 */
class SidePanelScreenTest {
    private var frame = 0L

    @Test
    fun `the headers fold their sections, a heading and Chat are chosen, and the panel is minimized`() {
        val actions = mutableListOf<Any>()
        UiThread.run {
            scene(actions = actions).use { scene ->
                settle(scene = scene)
                listOf(STEPS_ROW, NOTE_INFO_HEADER, OUTLINE_HEADER, CHAT, HIDE).forEach {
                    click(scene = scene, position = it)
                }
            }
        }

        assertEquals(
            expected = listOf<Any>(3, SidePanelSection.NOTE_INFO, SidePanelSection.OUTLINE, SidePanelMode.CHAT, HIDDEN),
            actual = actions
        )
    }

    // Smoke 1.14-09: a disabled text field in Chat left stale pixels on Linux once the panel went back to the inspector.
    @Test
    fun `Chat says it is coming soon and holds no text field`() {
        var editable = emptyList<SemanticsNode>()
        UiThread.run {
            scene(actions = mutableListOf(), mode = SidePanelMode.CHAT).use { scene ->
                settle(scene = scene)
                editable = scene.semanticsOwners.flatMap { it.getAllSemanticsNodes(mergingEnabled = false) }
                    .filter { SemanticsProperties.EditableText in it.config }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = editable
        )
    }

    @Test
    fun `Related takes the room a short outline leaves, so nothing is left empty under Note info`() {
        val (related, panel) = relatedBounds(body = SHORT_BODY, relatedHeight = TALL)

        assertTrue(actual = panel.bottom - related.bottom < NOTE_INFO_MAX, message = "$related in $panel")
        assertTrue(actual = related.height > panel.height / 2 - NOTE_INFO_MAX, message = "$related in $panel")
    }

    @Test
    fun `a long outline takes the room a short Related leaves`() {
        val longBody = (1..LONG_OUTLINE).joinToString(separator = "\n\n") { "## Heading $it" }

        val (related, panel) = relatedBounds(body = longBody, relatedHeight = SHORT)

        assertEquals(expected = SHORT.toFloat(), actual = related.height)
        assertTrue(actual = related.top > panel.height - NOTE_INFO_MAX - SHORT * 2, message = "$related in $panel")
    }

    /** Where a Related section that wants [relatedHeight], weighted as the real one, lands beside an outline of [body], and the panel. */
    private fun relatedBounds(
        body: String,
        relatedHeight: Int
    ): Pair<Rect, Rect> {
        var bounds = Rect.Zero to Rect.Zero
        UiThread.run {
            scene(actions = mutableListOf(), body = body, related = { _, _ ->
                Box(modifier = Modifier.weight(weight = 1f, fill = false).fillMaxWidth().testTag(tag = RELATED)) {
                    Box(modifier = Modifier.fillMaxWidth().height(height = relatedHeight.dp))
                }
            }).use { scene ->
                settle(scene = scene)
                val nodes = scene.semanticsOwners.flatMap { it.getAllSemanticsNodes(mergingEnabled = false) }
                val related = nodes.first { it.config.getOrNull(SemanticsProperties.TestTag) == RELATED }
                bounds =
                    related.boundsInRoot to
                    Rect(left = 0f, top = 0f, right = WIDTH.toFloat(), bottom = HEIGHT.toFloat())
            }
        }
        return bounds
    }

    private fun scene(
        actions: MutableList<Any>,
        mode: SidePanelMode = SidePanelMode.INSPECTOR,
        body: String = BODY,
        related: @Composable ColumnScope.(isOpen: Boolean, onToggle: () -> Unit) -> Unit = { _, _ -> }
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        NotekeaTheme(darkTheme = false) {
            SidePanel(
                outline = OutlineFinder().headings(text = body),
                current = 0,
                preferences = SidePanelPreferences(mode = mode),
                platformConventions = PreviewPlatformConventions.linux,
                actions = SidePanelActions(
                    onHeadingClick = { actions += it },
                    onModeSelect = { actions += it },
                    onSectionToggle = { actions += it },
                    onHide = { actions += HIDDEN },
                    onLeave = {}
                ),
                info = EditorPreviewData.info,
                related = related
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
        private const val LONG_OUTLINE = 40
        private const val NOTE_INFO_MAX = 260
        private const val RELATED = "related"
        private const val SHORT = 40
        private const val SHORT_BODY = "# Arepas guide\n"
        private const val TALL = 2000
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 340
        private val CHAT = Offset(x = 137f, y = 18f)
        private val HIDE = Offset(x = 322f, y = 18f)
        private val NOTE_INFO_HEADER = Offset(x = 60f, y = 270f)
        private val OUTLINE_HEADER = Offset(x = 60f, y = 49f)
        private val STEPS_ROW = Offset(x = 80f, y = 165f)
    }
}
