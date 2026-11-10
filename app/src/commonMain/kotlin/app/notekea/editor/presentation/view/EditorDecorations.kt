package app.notekea.editor.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntOffset
import app.notekea.core.notes.domain.TaskFinder
import app.notekea.core.presentation.designsystem.components.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.presentation.markdown.MarkdownHighlighter
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.resources.Res
import app.notekea.resources.editor_task
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val TASK_CHECKBOX_DP = 16
private const val TASK_CHECKBOX_RATIO = 0.8f

/**
 * A clickable checkbox over the `[ ]` / `[x]` of every task of the text the field shows, placed with that text's
 * layout and covering the brackets; a click toggles the character in the file, [windowStart] further in the note.
 * It recomposes on every keystroke, which only moves these boxes.
 */
@Composable
private fun TaskCheckboxes(
    layout: () -> TextLayoutResult?,
    windowStart: () -> Int,
    onIntent: (EditorIntent) -> Unit
) {
    val taskFinder = remember { TaskFinder() }

    val result = layout() ?: return
    val content = result.layoutInput.text.text

    val tasks = remember(content) { taskFinder.tasks(text = content) }

    val density = LocalDensity.current
    val label = stringResource(resource = Res.string.editor_task)
    val background = MaterialTheme.colorScheme.surface

    tasks.forEach { task ->
        val open = result.getBoundingBox(offset = task.stateIndex - 1)
        val close = result.getBoundingBox(offset = task.stateIndex + 1)

        val width = with(density) { (close.right - open.left).toDp() }

        val height = with(density) { (open.bottom - open.top).toDp() }

        key(task.stateIndex) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = open.left.roundToInt(),
                            y = open.top.roundToInt()
                        )
                    }
                    .size(
                        width = width,
                        height = height
                    )
                    .background(color = background),
                contentAlignment = Alignment.Center
            ) {
                NotekeaCheckbox(
                    checked = task.isDone,
                    onClick = {
                        onIntent(
                            EditorIntent.OnTaskToggle(stateIndex = windowStart() + task.stateIndex)
                        )
                    },
                    label = label,
                    size = minOf(
                        TASK_CHECKBOX_DP,
                        (height.value * TASK_CHECKBOX_RATIO).roundToInt()
                    )
                )
            }
        }
    }
}

/** The code blocks' backgrounds behind the text, and the tasks' checkboxes over it. */
internal fun decoratorOf(
    layout: () -> TextLayoutResult?,
    windowStart: () -> Int,
    onIntent: (EditorIntent) -> Unit
): TextFieldDecorator = TextFieldDecorator { innerTextField ->
    Box {
        CodeBlockBackgrounds(layout = layout)
        innerTextField()

        TaskCheckboxes(
            layout = layout,
            windowStart = windowStart,
            onIntent = onIntent
        )
    }
}

/**
 * The background of every fenced code block, behind its whole lines from one fence to the other and a little beyond
 * the text on each side, as a block: shading only the characters left a ragged edge and hid the spaces ending a line.
 */
@Composable
private fun BoxScope.CodeBlockBackgrounds(layout: () -> TextLayoutResult?) {
    val highlighter = remember { MarkdownHighlighter() }

    val result = layout() ?: return
    val content = result.layoutInput.text.text

    val blocks = remember(content) { highlighter.codeBlocks(text = content) }

    val color = MaterialTheme.colorScheme.surfaceVariant

    val padding = with(LocalDensity.current) { NotekeaDimensions.SpacingXs.toPx() }

    val radius = with(LocalDensity.current) { NotekeaDimensions.ControlRadius.toPx() }

    Canvas(modifier = Modifier.matchParentSize()) {
        val length = content.length

        blocks.forEach { block ->
            val top = result.getLineTop(lineIndex = result.getLineForOffset(offset = block.first))

            val bottom = result.getLineBottom(
                lineIndex = result.getLineForOffset(offset = minOf(block.last + 1, length))
            )

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = -padding,
                    y = top
                ),
                size = Size(
                    width = size.width + 2 * padding,
                    height = bottom - top
                ),
                cornerRadius = CornerRadius(
                    x = radius,
                    y = radius
                )
            )
        }
    }
}
