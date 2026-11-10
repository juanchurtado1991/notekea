package app.notekea.editor.presentation.view

import androidx.compose.ui.text.TextRange
import app.notekea.editor.domain.NoteBlocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockItemsTest {
    private val text = "one\n\ntwo\n\nthree\n\nfour\n\nfive"
    private val blocks = NoteBlocks.of(text = text)

    @Test
    fun `without a window every block is an item`() {
        val items = BlockItems(
            blocks = blocks,
            window = null
        )

        assertEquals(
            expected = 5,
            actual = items.count
        )

        assertEquals(
            expected = TextRange.Zero,
            actual = items.shown
        )

        assertFalse(actual = items.isWindow(item = 0))

        (0 until 5).forEach {
            assertEquals(
                expected = it,
                actual = items.blockOf(item = it)
            )

            assertEquals(
                expected = it,
                actual = items.itemOf(block = it)
            )
        }
    }

    @Test
    fun `the blocks of the window share one item`() {
        val items = BlockItems(
            blocks = blocks,
            window = 1..3
        )

        assertEquals(
            expected = 3,
            actual = items.count
        )

        assertTrue(actual = items.isWindow(item = 1))

        assertEquals(
            expected = listOf(0, 1, 4),
            actual = (0 until 3).map { items.blockOf(item = it) }
        )

        assertEquals(
            expected = listOf(0, 1, 1, 1, 2),
            actual = (0 until 5).map { items.itemOf(block = it) }
        )
    }

    @Test
    fun `the window shows its blocks without the line break ending the last blank line`() {
        val items = BlockItems(
            blocks = blocks,
            window = 1..2
        )

        assertEquals(
            expected = "two\n\nthree\n",
            actual = text.substring(
                startIndex = items.shown.min,
                endIndex = items.shown.max
            )
        )
    }

    @Test
    fun `a block's text leaves out the line break ending its blank line, but the last block has none`() {
        val items = BlockItems(
            blocks = blocks,
            window = null
        )

        assertEquals(
            expected = "one\n",
            actual = items.textOf(
                text = text,
                block = 0
            )
        )

        assertEquals(
            expected = "five",
            actual = items.textOf(
                text = text,
                block = 4
            )
        )
    }
}
