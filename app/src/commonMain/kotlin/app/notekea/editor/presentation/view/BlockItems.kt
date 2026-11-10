package app.notekea.editor.presentation.view

import androidx.compose.ui.text.TextRange
import app.notekea.editor.domain.NoteBlocks

/**
 * The note's [blocks] as the items of the editor's list: one item per block, except the blocks of the [window] (the
 * ones the selection touches), which share one item, the text field. No window (Read mode) is every block drawn.
 */
internal data class BlockItems(
    val blocks: NoteBlocks,
    val window: IntRange?
) {
    private val span: Int get() = window?.let {
        it.last - it.first
    } ?: 0

    val count: Int get() = blocks.size - span

    /** The part of the note's text the text field shows. */
    val shown: TextRange
        get() = window?.let {
            TextRange(
                start = blocks.start(index = it.first),
                end = blocks.textEnd(window = it)
            )
        } ?: TextRange.Zero

    fun isWindow(item: Int): Boolean = item == window?.first

    /** The block the [item] draws; the window's first block for the text field. */
    fun blockOf(item: Int): Int = if (window == null || item <= window.first) item else item + span

    fun itemOf(block: Int): Int = when {
        window == null || block < window.first -> block
        block <= window.last -> window.first
        else -> block - span
    }

    /** The text of [block] as it is drawn: without the line break that ends its blank line. */
    fun textOf(
        text: CharSequence,
        block: Int
    ): String = text.substring(
        startIndex = blocks.start(index = block),
        endIndex = blocks.textEnd(window = block..block)
    )
}
