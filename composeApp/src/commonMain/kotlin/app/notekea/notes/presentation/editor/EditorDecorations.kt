package app.notekea.notes.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntOffset
import app.notekea.core.presentation.components.NotekeaCheckbox
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.TaskFinder
import app.notekea.resources.Res
import app.notekea.resources.editor_task
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private const val TASK_CHECKBOX_DP = 16
private const val TASK_CHECKBOX_RATIO = 0.8f

/**
 * A clickable checkbox over the `[ ]` / `[x]` of every task of the text (`ROADMAP.md` 1.7), placed with the text's
 * layout, so tasks can be checked in Edit mode as in Read mode; a click toggles the character in the file. The box
 * covers the brackets with the editor's background. It recomposes on every keystroke, which only moves these boxes.
 */
@Composable
private fun TaskCheckboxes(
    buffer: TextFieldState,
    layout: () -> TextLayoutResult?,
    onIntent: (EditorIntent) -> Unit
) {
    val taskFinder = remember { TaskFinder() }
    val text = buffer.text
    // Keyed by the content alone: the text's own equality counts the selection, and a caret move changes no task.
    val content = text.toString()
    val tasks = remember(content) { taskFinder.tasks(text = content) }
    val result = layout() ?: return
    val density = LocalDensity.current
    val label = stringResource(resource = Res.string.editor_task)
    val background = MaterialTheme.colorScheme.surface
    tasks.filter { it.stateIndex + 1 < result.layoutInput.text.length }.forEach { task ->
        val open = result.getBoundingBox(offset = task.stateIndex - 1)
        val close = result.getBoundingBox(offset = task.stateIndex + 1)
        val width = with(density) { (close.right - open.left).toDp() }
        val height = with(density) { (open.bottom - open.top).toDp() }
        key(task.stateIndex) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = open.left.roundToInt(), y = open.top.roundToInt()) }
                    .size(width = width, height = height)
                    .background(color = background),
                contentAlignment = Alignment.Center
            ) {
                NotekeaCheckbox(
                    checked = task.isDone,
                    onClick = { onIntent(EditorIntent.OnTaskToggle(stateIndex = task.stateIndex)) },
                    label = label,
                    size = minOf(TASK_CHECKBOX_DP, (height.value * TASK_CHECKBOX_RATIO).roundToInt())
                )
            }
        }
    }
}

/** The code blocks' backgrounds behind the text, and the tasks' checkboxes over it. */
internal fun decoratorOf(
    buffer: TextFieldState,
    layout: () -> TextLayoutResult?,
    onIntent: (EditorIntent) -> Unit,
    overlay: @Composable () -> Unit
): TextFieldDecorator = TextFieldDecorator { innerTextField ->
    Box {
        CodeBlockBackgrounds(buffer = buffer, layout = layout)
        innerTextField()
        TaskCheckboxes(buffer = buffer, layout = layout, onIntent = onIntent)
        overlay()
    }
}

/**
 * The background of every fenced code block, behind its whole lines from one fence to the other and a little beyond
 * the text on each side, as a block: shading only the characters left a ragged edge and hid the spaces ending a line.
 */
@Composable
private fun BoxScope.CodeBlockBackgrounds(
    buffer: TextFieldState,
    layout: () -> TextLayoutResult?
) {
    val highlighter = remember { MarkdownHighlighter() }
    val text = buffer.text
    val content = text.toString()
    val blocks = remember(content) { highlighter.codeBlocks(text = content) }
    val color = MaterialTheme.colorScheme.surfaceVariant
    val padding = with(LocalDensity.current) { NotekeaDimensions.SpacingXs.toPx() }
    val radius = with(LocalDensity.current) { NotekeaDimensions.ControlRadius.toPx() }
    Canvas(modifier = Modifier.matchParentSize()) {
        val result = layout() ?: return@Canvas
        val length = result.layoutInput.text.length
        blocks.filter { it.first <= length }.forEach { block ->
            val top = result.getLineTop(lineIndex = result.getLineForOffset(offset = block.first))
            val bottom = result.getLineBottom(
                lineIndex = result.getLineForOffset(offset = minOf(block.last + 1, length))
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x = -padding, y = top),
                size = Size(width = size.width + 2 * padding, height = bottom - top),
                cornerRadius = CornerRadius(x = radius, y = radius)
            )
        }
    }
}
