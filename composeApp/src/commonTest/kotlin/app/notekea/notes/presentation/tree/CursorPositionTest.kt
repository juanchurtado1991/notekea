package app.notekea.notes.presentation.tree

import kotlin.test.Test
import kotlin.test.assertEquals

class CursorPositionTest {
    @Test
    fun `the cursor's line and column count from one, at a line's start and end and past the text`() {
        val text = "# Tasks\n\nLine two"

        assertEquals(
            expected = listOf(1 to 1, 1 to 8, 2 to 1, 3 to 6, 3 to 9, 3 to 9),
            actual = listOf(0, 7, 8, 14, 17, 99).map { cursorOf(text = text, offset = it) }
        )
    }
}
