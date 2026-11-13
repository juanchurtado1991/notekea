package app.notekea.editor.presentation.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.markdown.LocalMarkdownTableCells
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.reading.ReadingView

private const val PREVIEW_TABLE = "| Item | Qty |\n| :-- | --: |\n| Corn flour | 2 |\n| Salt | 1 |"

/**
 * A block of the note that is one table, drawn for the editor: [text] starts [textStart] into the note, every cell
 * takes [cells] (its clicks) and the room over the table is the [bar]'s, kept while it is empty so nothing moves
 * when the cursor comes in.
 */
@Composable
internal fun EditorTable(
    text: String,
    textStart: Int,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit,
    cells: (row: Int, column: Int) -> Modifier,
    modifier: Modifier = Modifier,
    bar: @Composable () -> Unit = {}
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.height(height = NotekeaDimensions.CompactControlSize)) { bar() }

        CompositionLocalProvider(LocalMarkdownTableCells provides cells) {
            ReadingView(
                text = text,
                attachments = attachments,
                onIntent = onIntent,
                isSelectable = false,
                textStart = textStart
            )
        }
    }
}

/**
 * A press on a table's cell: [onPress] gets how far into the cell's text it was. The press is the cell's alone,
 * unless it is the right button's and [isMenuKept], which leaves it for the table's menu.
 */
internal fun Modifier.cellPress(
    isMenuKept: Boolean,
    onPress: (x: Float) -> Unit
): Modifier = pointerInput(isMenuKept) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val isForMenu = isMenuKept && currentEvent.buttons.isSecondaryPressed

        onPress(down.position.x - NotekeaDimensions.TableCellPadding.toPx())

        if (!isForMenu) down.consume()
    }
}

@Preview
@Composable
private fun EditorTablePreview() {
    NotekeaPreviewThemes {
        EditorTable(
            text = PREVIEW_TABLE,
            textStart = 0,
            attachments = emptyList(),
            onIntent = {},
            cells = { _, _ -> Modifier }
        )
    }
}
