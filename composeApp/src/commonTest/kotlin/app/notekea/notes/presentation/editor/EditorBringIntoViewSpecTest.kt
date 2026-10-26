package app.notekea.notes.presentation.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class EditorBringIntoViewSpecTest {
    @Test
    fun `it scrolls only as needed, below the toolbar, and ignores a request as tall as the editor`() {
        val spec = EditorBringIntoViewSpec().apply { topInset = TOOLBAR }

        assertEquals(
            expected = listOf(0f, -30f, 50f, 0f),
            actual = listOf(
                spec.calculateScrollDistance(offset = 200f, size = 20f, containerSize = 500f),
                spec.calculateScrollDistance(offset = 30f, size = 20f, containerSize = 500f),
                spec.calculateScrollDistance(offset = 530f, size = 20f, containerSize = 500f),
                spec.calculateScrollDistance(offset = -300f, size = 1_200f, containerSize = 500f)
            )
        )
    }

    companion object {
        private const val TOOLBAR = 60f
    }
}
