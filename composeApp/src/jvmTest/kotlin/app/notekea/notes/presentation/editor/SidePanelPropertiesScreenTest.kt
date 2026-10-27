package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.FrontmatterReader
import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelTab
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The side panel's Properties tab (`ROADMAP.md` 1.14), rendered headless beside a note whose properties are only
 * there: a property is edited in the panel, "Show in the note" moves them back, and the Outline tab is chosen; the
 * panel's requests go to the tabs, the edits to the note's editor.
 */
class SidePanelPropertiesScreenTest {
    private var frame = 0L

    @Test
    fun `the Properties tab edits the note's properties, moves them back to the note and switches to the outline`() {
        val panelRequests = mutableListOf<EditorIntent>()
        val edits = mutableListOf<EditorIntent>()
        UiThread.run {
            scene(panelRequests = panelRequests, edits = edits).use { scene ->
                settle(scene = scene)
                listOf(FAVORITE_BOX, IN_NOTE, OUTLINE_TAB).forEach { click(scene = scene, position = it) }
            }
        }

        assertEquals(
            expected = listOf<EditorIntent>(
                PropertyIntent.OnValueChange(key = "favorite", value = PropertyValue.Checkbox(isChecked = false))
            ) to listOf(
                EditorIntent.OnPropertiesPlacementToggle,
                EditorIntent.OnSidePanelTabSelect(tab = SidePanelTab.OUTLINE)
            ),
            actual = edits to panelRequests
        )
    }

    private fun scene(
        panelRequests: MutableList<EditorIntent>,
        edits: MutableList<EditorIntent>
    ): ImageComposeScene {
        val state = EditorState(
            notePath = RelativePath(value = NOTE),
            properties = FrontmatterReader().read(header = HEADER_TEXT)
        )
        val host = SidePanelHost(send = { panelRequests += it })
        val route = { intent: EditorIntent ->
            if (SidePanelHost.isRequest(intent)) {
                host.onIntent(intent)
            } else {
                edits +=
                    intent
            }
        }
        return ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
            NotekeaTheme(darkTheme = false) {
                WithSidePanel(
                    preferences = SidePanelPreferences(tab = SidePanelTab.PROPERTIES, arePropertiesInNote = false),
                    sidePanel = host,
                    outline = emptyList(),
                    current = 0,
                    platformConventions = PreviewPlatformConventions.linux,
                    onHeadingClick = {},
                    onLeave = {},
                    properties = { PropertiesPanel(arePropertiesInNote = false, onIntent = route, state = state) }
                ) {
                    EditorContent(
                        state = state,
                        buffer = TextFieldState(initialText = BODY),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = route,
                        arePropertiesInNote = false
                    )
                }
            }
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
        private const val BODY = "# Arepas\n\nMix the flour.\n"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEADER_TEXT =
            "---\ntags: [recipe, venezuelan]\nstatus: Tested\nservings: 4\nfavorite: true\n" +
                "cooked: 2026-10-27\nsource:\n  book: Abuela\n---\n"
        private const val HEIGHT = 520
        private const val NOTE = "Recipes/Arepas.md"
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 1100
        private val FAVORITE_BOX = Offset(x = 884f, y = 228f)
        private val IN_NOTE = Offset(x = 830f, y = 61f)
        private val OUTLINE_TAB = Offset(x = 808f, y = 22f)
    }
}
