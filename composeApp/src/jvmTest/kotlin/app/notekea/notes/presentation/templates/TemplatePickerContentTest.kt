package app.notekea.notes.presentation.templates

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The template picker palette (`ROADMAP.md` 1.12), rendered headless on the AWT event thread: a click on a row chooses
 * that template, and Create, without a templates folder, asks for it with the example template in the app's language.
 */
class TemplatePickerContentTest {
    @Test
    fun `clicking a template chooses it, and create asks for the folder with its example`() {
        val rows = listOf("Meeting notes", "Weekly review").map {
            TemplatePickerRow(path = RelativePath(value = "Templates/$it.md"), name = it)
        }
        val listed =
            clicks(state = TemplatePickerState(isOpen = true, rows = rows, hasTemplates = true), listOf(FIRST, SECOND))
        val created = clicks(state = TemplatePickerState(isOpen = true, hasFolder = false), positions = listOf(CREATE))

        assertEquals(
            expected = listOf(TemplatePickerIntent.OnRowClick(index = 0), TemplatePickerIntent.OnRowClick(index = 1)) to
                listOf("Meeting notes" to true),
            actual = listed to created.filterIsInstance<TemplatePickerIntent.OnCreateFolderClick>().map {
                it.exampleName to it.exampleText.startsWith(prefix = "---\ntags: meeting\n---\n# {{title}}\n")
            }
        )
    }

    private fun clicks(
        state: TemplatePickerState,
        positions: List<Offset>
    ): List<TemplatePickerIntent> {
        val intents = mutableListOf<TemplatePickerIntent>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    TemplatePickerContent(
                        state = state,
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = { if (it !is TemplatePickerIntent.OnQueryChange) intents += it }
                    )
                }
            }.use { scene ->
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
                positions.forEach { position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = SETTLE_FRAMES * FRAME_NANOS)
                }
            }
        }
        return intents
    }

    companion object {
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val SETTLE_FRAMES = 3
        private const val WINDOW_HEIGHT = 640
        private const val WINDOW_WIDTH = 1200
        private val CREATE = Offset(x = 600f, y = 404f)
        private val FIRST = Offset(x = 600f, y = 227f)
        private val SECOND = Offset(x = 600f, y = 287f)
    }
}
