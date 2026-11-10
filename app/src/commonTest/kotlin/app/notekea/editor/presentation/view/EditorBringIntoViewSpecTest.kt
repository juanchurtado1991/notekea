package app.notekea.editor.presentation.view

import kotlin.test.Test
import kotlin.test.assertEquals

class EditorBringIntoViewSpecTest {
    @Test
    fun `it scrolls only as needed and ignores a request as tall as the editor`() {
        assertEquals(
            expected = listOf(0f, 0f, -30f, 50f, 0f),
            actual = listOf(
                EditorBringIntoViewSpec.calculateScrollDistance(
                    offset = 200f,
                    size = 20f,
                    containerSize = 500f
                ),
                EditorBringIntoViewSpec.calculateScrollDistance(
                    offset = 0f,
                    size = 20f,
                    containerSize = 500f
                ),
                EditorBringIntoViewSpec.calculateScrollDistance(
                    offset = -30f,
                    size = 20f,
                    containerSize = 500f
                ),
                EditorBringIntoViewSpec.calculateScrollDistance(
                    offset = 530f,
                    size = 20f,
                    containerSize = 500f
                ),
                EditorBringIntoViewSpec.calculateScrollDistance(
                    offset = -300f,
                    size = 1_200f,
                    containerSize = 500f
                )
            )
        )
    }
}
