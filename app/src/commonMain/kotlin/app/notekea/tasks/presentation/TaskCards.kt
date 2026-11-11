package app.notekea.tasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.presentation.designsystem.components.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.NotekeaProgressBar
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalNoteLooks
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.readableDate
import app.notekea.resources.Res
import app.notekea.resources.tasks_done_label
import app.notekea.resources.tasks_progress
import app.notekea.resources.tasks_when_done
import app.notekea.resources.tasks_when_later
import app.notekea.resources.tasks_when_no_date
import app.notekea.resources.tasks_when_overdue
import app.notekea.resources.tasks_when_this_week
import app.notekea.resources.tasks_when_today
import app.notekea.tasks.domain.TaskGroup
import app.notekea.tasks.domain.TaskRow
import app.notekea.tasks.domain.TaskWhen
import org.jetbrains.compose.resources.stringResource

private const val PROGRESS_WIDTH_DP = 72

/**
 * A piece of a group's card in the Tasks tab: the list draws a card as its head and one piece per task, so a long
 * group stays lazy. The first piece rounds the card's top and the last its bottom, with room under it.
 */
private fun Modifier.cardPiece(
    surface: Color,
    isFirst: Boolean,
    isLast: Boolean
): Modifier {
    val top = if (isFirst) NotekeaDimensions.CardRadius else 0.dp
    val bottom = if (isLast) NotekeaDimensions.CardRadius else 0.dp

    return fillMaxWidth()
        .padding(bottom = if (isLast) NotekeaDimensions.SpacingM else 0.dp)
        .background(
            color = surface,
            shape = RoundedCornerShape(
                topStart = top,
                topEnd = top,
                bottomEnd = bottom,
                bottomStart = bottom
            )
        )
}

/**
 * The head of a group's card: a note with its icon, its folder and how many of its tasks are done, or a date's name
 * (Overdue in the error color, Today in the theme's) with how many tasks it holds.
 */
@Composable
internal fun TaskCardHead(
    group: TaskGroup,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .cardPiece(
                surface = colors.surface,
                isFirst = true,
                isLast = group.rows.isEmpty()
            )
            .padding(
                start = NotekeaDimensions.SpacingM,
                end = NotekeaDimensions.SpacingM,
                top = NotekeaDimensions.SpacingM,
                bottom = NotekeaDimensions.SpacingXs
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (group) {
            is TaskGroup.OfNote -> NoteHead(group = group)
            is TaskGroup.OfDate -> DateHead(group = group)
        }
    }
}

@Composable
private fun RowScope.NoteHead(group: TaskGroup.OfNote) {
    val colors = MaterialTheme.colorScheme
    val looks = LocalNoteLooks.current
    val path = group.path.value
    val folder = group.path.parent?.value

    val progress = stringResource(
        Res.string.tasks_progress,
        group.done,
        group.total
    )

    Icon(
        imageVector = looks.iconOf(path = path),
        contentDescription = null,
        modifier = Modifier.size(size = NotekeaDimensions.IconSize),
        tint = looks.colorOf(path = path) ?: colors.onSurfaceVariant
    )

    // The name and the folder take the room left, so the progress stays at the card's end.
    Row(
        modifier = Modifier.weight(weight = 1f),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.path.nameWithoutExtension,
            modifier = Modifier.weight(
                weight = 1f,
                fill = false
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!folder.isNullOrEmpty()) HeadDetail(text = folder)
    }

    NotekeaProgressBar(
        label = progress,
        fraction = group.done.toFloat() / group.total,
        modifier = Modifier.width(width = PROGRESS_WIDTH_DP.dp)
    )

    HeadDetail(text = progress)
}

@Composable
private fun RowScope.DateHead(group: TaskGroup.OfDate) {
    val colors = MaterialTheme.colorScheme

    val titleResource = when (group.date) {
        TaskWhen.OVERDUE -> Res.string.tasks_when_overdue
        TaskWhen.TODAY -> Res.string.tasks_when_today
        TaskWhen.THIS_WEEK -> Res.string.tasks_when_this_week
        TaskWhen.LATER -> Res.string.tasks_when_later
        TaskWhen.NO_DATE -> Res.string.tasks_when_no_date
        TaskWhen.DONE -> Res.string.tasks_when_done
    }

    Text(
        text = stringResource(resource = titleResource),
        modifier = Modifier.weight(weight = 1f),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = dueColor(due = group.date) ?: colors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )

    HeadDetail(
        text = group
            .rows
            .size
            .toString()
    )
}

@Composable
private fun HeadDetail(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** The color that says a task is late or for today; null for any other day. */
@Composable
private fun dueColor(due: TaskWhen): Color? = when (due) {
    TaskWhen.OVERDUE -> MaterialTheme.colorScheme.error
    TaskWhen.TODAY -> MaterialTheme.colorScheme.primary
    else -> null
}

/**
 * A task in its group's card: its box, its text, which opens it, its date (red when late, in the theme's color for
 * today) and where it is: its heading in a note's card, its note in a date's. [isLast] closes the card.
 */
@Composable
internal fun TaskCardRow(
    row: TaskRow,
    isInNote: Boolean,
    isLast: Boolean,
    onIntent: (TasksIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = row.task
    val colors = MaterialTheme.colorScheme
    val place = if (isInNote) task.heading else row.path.nameWithoutExtension

    Row(
        modifier = modifier
            .cardPiece(
                surface = colors.surface,
                isFirst = false,
                isLast = isLast
            )
            .padding(
                start = NotekeaDimensions.SpacingM,
                end = NotekeaDimensions.SpacingM,
                top = NotekeaDimensions.SpacingXs,
                bottom = if (isLast) NotekeaDimensions.SpacingM else NotekeaDimensions.SpacingXs
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaCheckbox(
            checked = task.isDone,
            onClick = { onIntent(TasksIntent.OnTaskToggle(row = row)) },
            label = stringResource(resource = Res.string.tasks_done_label, task.text)
        )

        Text(
            text = task.text,
            modifier = Modifier
                .weight(weight = 1f)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .clickable(role = Role.Button) { onIntent(TasksIntent.OnTaskOpen(row = row)) },
            style = MaterialTheme.typography.bodyMedium,
            color = if (task.isDone) colors.onSurfaceVariant else colors.onSurface,
            textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        task.date?.let { date ->
            Text(
                text = readableDate(
                    date = date,
                    withYear = false
                ),
                style = MaterialTheme.typography.bodySmall,
                color = dueColor(due = row.due) ?: colors.onSurfaceVariant,
                maxLines = 1
            )
        }

        place?.let { HeadDetail(text = it) }
    }
}

@Preview
@Composable
private fun TaskCardHeadPreview() {
    NotekeaPreviewThemes {
        Column {
            TasksPreviewData
                .state
                .list
                .groups
                .forEach { TaskCardHead(group = it) }

            TaskCardHead(
                group = TaskGroup.OfDate(
                    date = TaskWhen.OVERDUE,
                    rows = listOf(TasksPreviewData.row)
                )
            )
        }
    }
}

@Preview
@Composable
private fun TaskCardRowPreview() {
    NotekeaPreviewThemes {
        TaskCardRow(
            row = TasksPreviewData.row,
            isInNote = true,
            isLast = true,
            onIntent = {}
        )
    }
}
