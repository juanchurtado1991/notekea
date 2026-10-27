package app.notekea.notes.presentation.tree

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorContent
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Daily notes on screen (`ROADMAP.md` 1.10): "Today" in the sidebar asks for today's note, and a daily note's header
 * arrows step to the days around it, the one without a note on its side doing nothing.
 */
class DailyNoteScreenTest {
    @Test
    fun `today in the sidebar and the header's arrows ask for today and the day before, a missing side does nothing`() {
        val treeIntents = mutableListOf<VaultTreeIntent>()
        val editorIntents = mutableListOf<EditorIntent>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    DailyNoteWorkspace(onTreeIntent = { treeIntents += it }, onEditorIntent = { editorIntents += it })
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                listOf(TODAY_ROW, PREVIOUS_ARROW, NEXT_ARROW).forEachIndexed { index, position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Move, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = (index + 1) * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = listOf<VaultTreeIntent>(VaultTreeIntent.OnTodayClick) to
                listOf<EditorIntent>(EditorIntent.OnDayStep(step = -1)),
            actual = treeIntents.filter { it == VaultTreeIntent.OnTodayClick } to
                editorIntents.filterIsInstance<EditorIntent.OnDayStep>()
        )
    }

    /** The sidebar beside the daily note of 2026-10-22, which has a note the day before and none after. */
    @Composable
    private fun DailyNoteWorkspace(
        onTreeIntent: (VaultTreeIntent) -> Unit,
        onEditorIntent: (EditorIntent) -> Unit
    ) {
        VaultTreeContent(
            state = TreePreviewData.state(),
            vaultName = VAULT_NAME,
            onIntent = onTreeIntent,
            rail = SidebarRailActions()
        ) {
            EditorContent(
                state = EditorState(notePath = RelativePath(value = NOTE)),
                buffer = TextFieldState(initialText = BODY),
                platformConventions = PreviewPlatformConventions.linux,
                onIntent = onEditorIntent,
                dayNeighbors = DayNeighbors(
                    date = CalendarDate(year = 2026, month = 10, day = 22),
                    previous = RelativePath(value = PREVIOUS),
                    next = null
                )
            )
        }
    }

    companion object {
        private const val BODY = "# Wednesday, 22 October 2026\n\nRainy day.\n"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 560
        private const val NOTE = "Journal/2026-10-22.md"
        private const val PREVIOUS = "Journal/2026-10-21.md"
        private const val VAULT_NAME = "Personal Notes"
        private const val WIDTH = 1100
        private val NEXT_ARROW = Offset(x = 1018f, y = 65f)
        private val PREVIOUS_ARROW = Offset(x = 990f, y = 65f)
        private val TODAY_ROW = Offset(x = 20f, y = 154f)
    }
}
