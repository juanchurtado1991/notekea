package app.notekea.notes.presentation.editor

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ToolbarFitTest {
    @Test
    fun `a wide toolbar shows every group, a narrower one moves the lists, then the links, into More`() {
        assertEquals(
            expected = listOf(ToolbarFit.ALL, ToolbarFit.ALL, ToolbarFit.WITHOUT_LISTS, ToolbarFit.TEXT_ONLY),
            actual = listOf(800.dp, 520.dp, 519.dp, 419.dp).map { ToolbarFit.of(width = it) }
        )
    }
}
