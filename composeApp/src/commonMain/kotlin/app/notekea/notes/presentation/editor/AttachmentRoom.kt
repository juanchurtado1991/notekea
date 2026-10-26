package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextLayoutResult
import app.notekea.core.presentation.theme.NotekeaDimensions

/**
 * What Edit mode needs to show embedded files under their lines (`ROADMAP.md` 1.8): the heights they measured, the
 * styles that make their lines tall enough ([paragraphs], read by the highlighting as the text changes), and the
 * text's own line height and the gap around a file, in pixels.
 */
class AttachmentRoom(
    val heights: SnapshotStateMap<String, Int>,
    val lineHeight: Float,
    val gap: Float,
    val paragraphs: (String) -> List<Pair<IntRange, ParagraphStyle>>
) {
    /** The files drawn in the room made for them ([AttachmentOverlay]). */
    @Composable
    fun Overlay(
        buffer: TextFieldState,
        attachments: List<AttachmentView>,
        layout: () -> TextLayoutResult?,
        onIntent: (EditorIntent) -> Unit
    ) {
        AttachmentOverlay(
            buffer = buffer,
            attachments = attachments,
            heights = heights,
            layout = layout,
            lineHeight = lineHeight,
            gap = gap,
            onIntent = onIntent
        )
    }

    companion object {
        /** The room for [attachments] in the editor's text, kept across recompositions. */
        @Composable
        fun remember(
            attachments: List<AttachmentView>
        ): AttachmentRoom {
            val density = LocalDensity.current
            val style = MaterialTheme.typography.bodyLarge
            val lineHeight =
                with(density) { if (style.lineHeight.isSp) style.lineHeight.toPx() else style.fontSize.toPx() }
            val gap = with(density) { NotekeaDimensions.SpacingS.toPx() }
            val current = rememberUpdatedState(newValue = attachments)
            val heights = remember { mutableStateMapOf<String, Int>() }
            return remember(density, lineHeight, gap) {
                AttachmentRoom(heights = heights, lineHeight = lineHeight, gap = gap) { text ->
                    AttachmentSpace.of(text = text, views = current.value, heights = heights).mapNotNull { space ->
                        space.paragraphStyle(lineHeight = lineHeight, gap = gap, density = density)
                            ?.let { (space.lineStart until space.lineEnd) to it }
                    }
                }
            }
        }
    }
}
