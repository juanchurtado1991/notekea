package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import kotlin.math.roundToInt

private const val PREVIEW_WIDTH_DP = 480

/**
 * The embedded files of the text in Edit mode (`ROADMAP.md` 1.8), each drawn under its own line, in the room its
 * [AttachmentSpace] makes there: [lineHeight] and [gap] down from the line's top, as wide as the text. Each one tells
 * [heights] how tall it is, so its line grows to fit it.
 */
@Composable
fun AttachmentOverlay(
    buffer: TextFieldState,
    attachments: List<AttachmentView>,
    heights: SnapshotStateMap<String, Int>,
    layout: () -> TextLayoutResult?,
    lineHeight: Float,
    gap: Float,
    onIntent: (EditorIntent) -> Unit
) {
    if (attachments.none { it.embed.isOwnLine }) return
    val result = layout() ?: return
    val length = result.layoutInput.text.length
    AttachmentSpace.of(text = buffer.text.toString(), views = attachments, heights = heights)
        .filter { it.lineStart < length }
        .forEach { space ->
            val top =
                result.getLineTop(lineIndex = result.getLineForOffset(offset = space.lineStart)) + lineHeight + gap
            key(space.key) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = 0, y = top.roundToInt()) }
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            if (heights[space.key] !=
                                size.height
                            ) {
                                heights[space.key] = size.height
                            }
                        }
                ) {
                    AttachmentBlock(view = space.view, onIntent = onIntent)
                }
            }
        }
}

@Preview
@Composable
private fun AttachmentOverlayPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            AttachmentOverlay(
                buffer = TextFieldState(),
                attachments = emptyList(),
                heights = SnapshotStateMap(),
                layout = { null },
                lineHeight = 0f,
                gap = 0f,
                onIntent = {}
            )
        }
    }
}
