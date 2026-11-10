package app.notekea.editor.presentation.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import app.notekea.editor.domain.NoteBlocks
import app.notekea.editor.domain.TableCell
import app.notekea.editor.domain.TableText
import app.notekea.editor.presentation.markdown.ShownText
import app.notekea.editor.presentation.markdown.ShownTextBuilder

/**
 * What the pieces of the editor's page share: the note's text ([buffer]) cut into the list's [items], the list, and
 * the text field over the cursor's blocks, with its layout and its place, which clicks, keys and drags need to move
 * the cursor from one block to another. In a block that is one table the field is over one [cell] only.
 */
@Stable
internal class EditorPageState(
    val buffer: TextFieldState,
    val listState: LazyListState,
    val focusRequester: FocusRequester,
    private val isSource: () -> Boolean
) {
    private val shownTexts = ShownTextBuilder()

    private val blocks by derivedStateOf { NoteBlocks.of(text = buffer.text) }

    val items: BlockItems by derivedStateOf {
        val selection = buffer.selection

        val window = when {
            // Source shows the whole note as written, in the text field.
            isSource() -> 0 until blocks.size

            else -> blocks.window(
                selectionMin = selection.min,
                selectionMax = selection.max
            )
        }

        BlockItems(
            blocks = blocks,
            window = window
        )
    }

    /** The cursor's block if it is one table and nothing else, to edit by cells; never in Source. */
    private val blockTable: TableText? by derivedStateOf {
        val window = items.window
        val block = window.first

        if (isSource() || block != window.last) return@derivedStateOf null

        TableText.of(
            text = items.textOf(
                text = buffer.text,
                block = block
            ),
            offset = items.blocks.start(index = block)
        )
    }

    /** Whether the cursor is on the lines under the table of its block, where text is written as anywhere. */
    private val isUnderTable: Boolean by derivedStateOf {
        val after = blockTable?.afterStart

        after != null && buffer.selection.min >= after
    }

    /** The table's cell the text field is over: the cursor's, while the selection stays inside one cell. */
    val cell: TableCell? by derivedStateOf {
        val selection = buffer.selection

        blockTable
            ?.takeUnless { isUnderTable }
            ?.cellAt(offset = selection.start)
            ?.takeIf {
                selection.collapsed || (selection.min >= it.start && selection.max <= it.end)
            }
    }

    /** The table drawn in the cursor's block; null where the field shows the block's text as written. */
    val table: TableText? by derivedStateOf {
        blockTable?.takeIf {
            cell != null || isUnderTable
        }
    }

    /** The part of the note the text field shows: the cursor's blocks or, of a drawn [table], its cell only. */
    val fieldRange: TextRange by derivedStateOf {
        val cell = cell
        val after = table?.afterStart

        when {
            cell != null -> TextRange(
                start = cell.start,
                end = cell.end
            )

            after != null -> TextRange(
                start = after,
                end = items.shown.max
            )

            else -> items.shown
        }
    }

    /** Whether the text field has the keyboard: only then does the cursor's line show its marks. */
    var hasFocus: Boolean by mutableStateOf(value = false)

    /** What the text field shows of the note: its window, with the marks hidden away from the cursor's lines. */
    val shown: ShownText by derivedStateOf {
        val window = fieldRange

        val revealed = when {
            isSource() || table != null -> window
            hasFocus -> buffer.selection
            else -> null
        }

        shownTexts.of(
            note = buffer.text,
            window = window,
            revealed = revealed
        )
    }

    /** The text field's layout, set while the field is on the page. */
    var layout: () -> TextLayoutResult? by mutableStateOf(value = { null })

    /** A click waiting for the text field to show the block it was made on. */
    var placement: Placement? by mutableStateOf(value = null)
        private set

    /** The height of the toolbar stuck at the top of the list, set when it is laid out; read only while scrolling. */
    var toolbarHeight: Float = 0f

    var fieldPlace: LayoutCoordinates? = null
    var listPlace: LayoutCoordinates? = null

    /** The field's layout if it is the one of the text shown now: right after the window moves it is the old one. */
    fun currentLayout(): TextLayoutResult? {
        val text = shown.text

        return layout()?.takeIf {
            it
                .layoutInput
                .text
                .text == text
        }
    }

    /**
     * Puts the cursor in [block], drawn until now, at the [position] clicked in its text, which was drawn [drawnHeight]
     * tall; [isExtending] (Shift) selects from where the cursor was. The cursor goes to the block's start first, so
     * the text field shows it, and to the place clicked once the field has laid the block out ([place]).
     */
    fun click(
        block: Int,
        position: Offset,
        drawnHeight: Float,
        isExtending: Boolean
    ) {
        val start = items.blocks.start(index = block)

        val text = items.textOf(
            text = buffer.text,
            block = block
        )

        wait(
            placement = Placement(
                reference = start,
                position = position,
                anchor = buffer
                    .selection
                    .start
                    .takeIf { isExtending },
                drawnHeight = drawnHeight,
                textLength = text.trimEnd().length
            )
        )
    }

    /** Puts the cursor in the cell at [row] and [column] of the table that [block] is, [x] into the cell's text. */
    fun clickCell(
        block: Int,
        row: Int,
        column: Int,
        x: Float
    ) {
        val table = TableText.of(
            text = items.textOf(
                text = buffer.text,
                block = block
            ),
            offset = items.blocks.start(index = block)
        )

        val cell = table?.cell(
            row = row,
            column = column
        ) ?: return

        wait(
            placement = Placement(
                reference = cell.start,
                position = Offset(
                    x = x,
                    y = 0f
                ),
                anchor = null
            )
        )
    }

    /**
     * Where the cursor goes when it is alone in a [cell]'s row but outside its text (in the pipes or the spaces
     * around it, where an arrow, an undo or a click on the table's edge left it); null when it is in place.
     */
    fun strayCursor(): Int? {
        val cell = cell ?: return null
        val selection = buffer.selection

        return selection
            .start
            .coerceIn(
                minimumValue = cell.start,
                maximumValue = cell.end
            )
            .takeIf {
                selection.collapsed && it != selection.start
            }
    }

    /** A click in the room below the note's last line puts the cursor at the note's end. */
    fun clickAfterEnd() = wait(
        placement = Placement(
            reference = buffer.text.length,
            position = Offset(
                x = Float.MAX_VALUE,
                y = 0f
            ),
            anchor = null
        )
    )

    private fun wait(placement: Placement) {
        buffer.edit {
            selection = TextRange(
                start = placement.anchor ?: placement.reference,
                end = placement.reference
            )
        }

        this.placement = placement
    }

    /**
     * How far below the top of its list [item] the character at [offset] is: only the text field knows, of its text.
     */
    fun topIn(
        item: Int,
        offset: Int
    ): Float {
        val layout = currentLayout()?.takeIf {
            table == null && items.isWindow(item = item)
        } ?: return 0f

        return layout.getLineTop(
            lineIndex = layout.getLineForOffset(offset = shown.toShown(offset = offset))
        )
    }

    /** Ends the [placement] waiting, with the field's [layout] of the clicked block: the cursor goes where it was. */
    fun place(
        waiting: Placement,
        layout: TextLayoutResult
    ) {
        val shown = shown
        val reference = shown.toShown(offset = waiting.reference)

        val last = shown
            .toShown(offset = waiting.reference + waiting.textLength - 1)
            .coerceAtLeast(minimumValue = reference)

        val top = layout.getLineTop(lineIndex = layout.getLineForOffset(offset = reference))
        val bottom = layout.getLineBottom(lineIndex = layout.getLineForOffset(offset = last))
        val position = waiting.position.copy(y = top + waiting.yIn(fieldHeight = bottom - top))
        val offset = shown.toNote(offset = layout.getOffsetForPosition(position = position))

        buffer.edit {
            selection = TextRange(
                start = waiting.anchor ?: offset,
                end = offset
            )
        }

        placement = null
    }

    /**
     * A click on a drawn block: [position] is where, from the line of [reference]; [anchor] is kept with Shift. The
     * block's text, [textLength] characters, was drawn [drawnHeight] tall (zero when nothing was drawn).
     */
    data class Placement(
        val reference: Int,
        val position: Offset,
        val anchor: Int?,
        val drawnHeight: Float = 0f,
        val textLength: Int = 0
    ) {
        /**
         * How far down the click is in the text field, which shows the same text [fieldHeight] tall: a list is drawn
         * looser than the field writes it, so the click keeps its share of the text's height, not its distance.
         */
        fun yIn(fieldHeight: Float): Float = when {
            drawnHeight <= 0f -> position.y
            position.y < drawnHeight -> position.y / drawnHeight * fieldHeight
            else -> fieldHeight + position.y - drawnHeight
        }
    }

    companion object {
        /** The items the list has before the first block's: the header and the toolbar. */
        const val LEADING_ITEMS = 2
        const val WINDOW_KEY = "window"
    }
}
