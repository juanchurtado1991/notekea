package app.notekea.chat.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.LineEdit
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val REMOVED = "−"
private const val ADDED = "+"

/**
 * A changed line on the card (4.2, design B): the note, then the line as it is (−) and as it will be (+), each without
 * its list or heading marks, the words that change struck through in the one and marked in the other.
 */
@Composable
internal fun ChatLineChange(step: ActionStep) {
    val note = step.notes.firstOrNull() ?: return
    val before = LineEdit.textOf(line = step.line.orEmpty())
    val after = LineEdit.textOf(line = step.text.orEmpty())

    val change = LineDiff.of(
        old = before,
        new = after
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(all = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = note.bare(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ChangedLine(
            mark = REMOVED,
            text = before,
            changed = change.old,
            isNew = false
        )

        ChangedLine(
            mark = ADDED,
            text = after,
            changed = change.new,
            isNew = true
        )
    }
}

@Composable
private fun ChangedLine(
    mark: String,
    text: String,
    changed: IntRange,
    isNew: Boolean
) {
    val colors = MaterialTheme.colorScheme
    val strong = if (isNew) colors.primary else colors.error
    val plain = if (isNew) colors.onSurface else colors.onSurfaceVariant

    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Text(
            text = mark,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = strong
        )

        Text(
            text = marked(
                text = text,
                changed = changed,
                color = strong,
                isStruck = !isNew
            ),
            style = MaterialTheme.typography.bodySmall,
            color = plain
        )
    }
}

private fun marked(
    text: String,
    changed: IntRange,
    color: Color,
    isStruck: Boolean
) = buildAnnotatedString {
    append(
        text.substring(
            startIndex = 0,
            endIndex = changed.first
        )
    )

    withStyle(
        style = SpanStyle(
            color = color,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.LineThrough.takeIf { isStruck }
        )
    ) {
        append(
            text.substring(
                startIndex = changed.first,
                endIndex = changed.last + 1
            )
        )
    }

    append(text.substring(startIndex = changed.last + 1))
}

@Preview
@Composable
private fun ChatLineChangePreview() {
    NotekeaPreviewThemes {
        ChatLineChange(
            step = ActionStep(
                kind = ActionKind.CHANGE_LINE,
                notes = listOf(RelativePath(value = "Journal/2026-11-20.md")),
                line = "- 10:00 Interview with Ana",
                text = "- 16:00 Interview with Ana"
            )
        )
    }
}
