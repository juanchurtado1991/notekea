package app.notekea.editor.presentation.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.presentation.designsystem.components.markdown.TextMarks
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.editor.domain.TableText
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.reading.ReadingView

private const val PREVIEW_TEXT = "Mix the **corn flour** with warm water.\n"

/**
 * One block of the note the cursor is not in, drawn as it reads, with Find's [marks]; [text] starts [textStart] into
 * the note, and its last blank lines keep their height. [onClick] (where, how tall the text is drawn, whether Shift
 * was held) puts the cursor in it, or [onCellClick] in a cell of a block that is a table; links take their clicks.
 */
@Composable
internal fun EditorBlock(
    text: String,
    textStart: Int,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit,
    onClick: (position: Offset, drawnHeight: Float, isExtending: Boolean) -> Unit,
    onCellClick: (row: Int, column: Int, x: Float) -> Unit,
    marks: TextMarks?,
    modifier: Modifier = Modifier
) {
    val content = text.trimEnd()

    val blankLines = text.count {
        it == NEW_LINE_CHAR
    } - content.count {
        it == NEW_LINE_CHAR
    }

    val lineHeight = with(LocalDensity.current) {
        MaterialTheme
            .typography
            .bodyLarge
            .lineHeight
            .toDp()
    }

    val blankHeight by rememberUpdatedState(newValue = lineHeight * blankLines)
    val currentOnClick by rememberUpdatedState(newValue = onClick)
    val currentOnCellClick by rememberUpdatedState(newValue = onCellClick)

    val isTable = remember(content) {
        TableText.of(text = content) != null
    }

    val cells = remember {
        { row: Int, column: Int ->
            Modifier.cellPress(isMenuKept = false) { currentOnCellClick(row, column, it) }
        }
    }

    val clicks = Modifier
        .pointerHoverIcon(icon = PointerIcon.Text)
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()

                waitForUpOrCancellation()?.let {
                    currentOnClick(
                        it.position,
                        size.height - blankHeight.toPx(),
                        currentEvent.keyboardModifiers.isShiftPressed
                    )
                }
            }
        }

    Column(modifier = modifier.fillMaxWidth().then(other = clicks)) {
        if (isTable) {
            EditorTable(
                text = content,
                textStart = textStart,
                attachments = attachments,
                onIntent = onIntent,
                cells = cells
            )
        } else {
            ReadingView(
                text = content,
                attachments = attachments,
                onIntent = onIntent,
                marks = marks,
                isSelectable = false,
                textStart = textStart
            )
        }

        Spacer(modifier = Modifier.height(height = blankHeight))
    }
}

@Preview
@Composable
private fun EditorBlockPreview() {
    NotekeaPreviewThemes {
        EditorBlock(
            text = PREVIEW_TEXT,
            textStart = 0,
            attachments = emptyList(),
            onIntent = {},
            onClick = { _, _, _ -> },
            onCellClick = { _, _, _ -> },
            marks = null
        )
    }
}
