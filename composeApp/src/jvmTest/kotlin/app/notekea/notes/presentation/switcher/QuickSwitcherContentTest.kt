package app.notekea.notes.presentation.switcher

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The palette rendered headless and clicked like a user would, in every state it can show. Every scene runs on the
 * AWT event thread, as in the app: Compose may resume effects there (the scroll to the selection), and a scene measured
 * from another thread then fails with a multithreaded access error that surfaces in whichever test runs next
 * ([UiThread]).
 */
class QuickSwitcherContentTest {
    private val intents = mutableListOf<QuickSwitcherIntent>()
    private val state = mutableStateOf(value = QuickSwitcherPreviewData.notes())

    @Test
    fun `clicking a result opens it and clicking outside the palette closes it`() {
        UiThread.run {
            palette(height = WINDOW_HEIGHT).use { scene ->
                settle(scene = scene)
                click(scene = scene, at = Offset(x = ROW_X, y = SECOND_ROW_Y))
                click(scene = scene, at = Offset(x = OUTSIDE_X, y = OUTSIDE_Y))
            }
        }

        assertEquals(
            expected = listOf(
                QuickSwitcherIntent.OnRowClick(path = RelativePath(value = "Recipes/Café con leche.md")),
                QuickSwitcherIntent.OnClose
            ),
            actual = intents
        )
    }

    @Test
    fun `the selected result is scrolled into view in a short window`() {
        val rows = (1..LONG_LIST).map {
            QuickSwitcherRow(path = RelativePath(value = "Note $it.md"), name = "Note $it")
        }
        state.value = QuickSwitcherState(isOpen = true, query = "note", rows = rows, selectedIndex = rows.lastIndex)
        UiThread.run {
            palette(height = SHORT_WINDOW_HEIGHT).use { scene ->
                settle(scene = scene)
                click(scene = scene, at = Offset(x = ROW_X, y = SHORT_WINDOW_LAST_ROW_Y))
            }
        }

        assertEquals(
            expected = listOf<QuickSwitcherIntent>(QuickSwitcherIntent.OnRowClick(path = rows.last().path)),
            actual = intents
        )
    }

    @Test
    fun `every state renders, switching between them while shown`() {
        val states = listOf(
            QuickSwitcherPreviewData.notes(),
            QuickSwitcherPreviewData.content().copy(isSearching = true),
            QuickSwitcherPreviewData.content(),
            QuickSwitcherState(isOpen = true, query = "zzz"),
            QuickSwitcherState(isOpen = true, mode = QuickSwitcherMode.CONTENT),
            QuickSwitcherState(isOpen = true),
            QuickSwitcherState(isOpen = true, query = "corn", hasUnexpectedError = true),
            QuickSwitcherState()
        )
        UiThread.run {
            palette(height = WINDOW_HEIGHT).use { scene ->
                states.forEachIndexed { index, next ->
                    state.value = next
                    scene.render(nanoTime = index * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = emptyList<QuickSwitcherIntent>(),
            actual = intents
        )
    }

    private fun palette(
        height: Int
    ): ImageComposeScene = ImageComposeScene(
        width = WINDOW_WIDTH,
        height = height,
        density = Density(density = DENSITY)
    ) {
        NotekeaTheme(darkTheme = false) {
            QuickSwitcherContent(state = state.value, onIntent = { intents += it })
        }
    }

    /** A few frames so the field takes focus and the list scrolls to the selection. */
    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
    }

    private fun click(
        scene: ImageComposeScene,
        at: Offset
    ) {
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = at)
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = at)
        scene.render(nanoTime = SETTLE_FRAMES * FRAME_NANOS)
    }

    companion object {
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val LONG_LIST = 10
        private const val OUTSIDE_X = 1_000f
        private const val OUTSIDE_Y = 700f
        private const val ROW_X = 500f
        private const val SECOND_ROW_Y = 360f
        private const val SETTLE_FRAMES = 4
        private const val SHORT_WINDOW_HEIGHT = 420
        private const val SHORT_WINDOW_LAST_ROW_Y = 380f
        private const val WINDOW_HEIGHT = 780
        private const val WINDOW_WIDTH = 1_140
    }
}
