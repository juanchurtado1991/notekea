package app.notekea.chat.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.ActionKind
import app.notekea.chat.domain.ActionProblem
import app.notekea.chat.domain.ActionStep
import app.notekea.chat.domain.FoundNotes
import app.notekea.chat.domain.FoundRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_found_count
import app.notekea.resources.chat_found_none
import app.notekea.resources.chat_found_tasks
import app.notekea.resources.chat_problem_exists
import app.notekea.resources.chat_problem_failed
import app.notekea.resources.chat_problem_folder
import app.notekea.resources.chat_problem_long
import app.notekea.resources.chat_problem_many
import app.notekea.resources.chat_problem_missing
import app.notekea.resources.chat_problem_note
import app.notekea.resources.chat_problem_query
import app.notekea.resources.chat_problem_query_failed
import app.notekea.resources.chat_problem_refused
import app.notekea.resources.chat_problem_task
import app.notekea.resources.chat_shown_close_tabs
import app.notekea.resources.chat_shown_open_note
import app.notekea.resources.chat_shown_open_today
import app.notekea.resources.chat_shown_panel
import app.notekea.resources.chat_shown_search
import app.notekea.resources.chat_shown_source
import app.notekea.resources.chat_shown_split
import app.notekea.resources.chat_shown_theme
import org.jetbrains.compose.resources.stringResource

private const val CELL_SEPARATOR = " · "

/**
 * What the chat did for a request that is not a question (3.8, 3.9): the notes or tasks it found, each row opening its
 * note, a line for each thing it showed, the card of what it will write, or why it could not.
 */
@Composable
internal fun ChatActionView(
    entryId: Long,
    action: ChatAction,
    onIntent: (ChatIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        action.found.forEach { found ->
            FoundTable(
                found = found,
                onRowClick = { onIntent(ChatIntent.OnFoundClick(row = it)) }
            )
        }

        action.shown.forEach { ShownLine(step = it) }

        if (action.steps.isNotEmpty()) {
            ChatActionCard(
                action = action,
                onConfirm = { onIntent(ChatIntent.OnActionConfirm(id = entryId)) },
                onCancel = { onIntent(ChatIntent.OnActionCancel(id = entryId)) },
                onUndo = { onIntent(ChatIntent.OnActionUndo(id = entryId)) }
            )
        }

        action.problem?.let { problem ->
            Text(
                text = problemText(problem = problem),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** A table of notes (by their properties) or tasks, its count first, each row its note and its cells. */
@Composable
private fun FoundTable(
    found: FoundNotes,
    onRowClick: (FoundRow) -> Unit
) {
    val isTasks = found.rows.any {
        it.line != null
    }

    val count = if (isTasks) Res.string.chat_found_tasks else Res.string.chat_found_count

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        val heading = if (found.rows.isEmpty()) {
            stringResource(resource = Res.string.chat_found_none)
        } else {
            stringResource(count, found.rows.size)
        }

        Text(
            text = heading,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        found.rows.forEach { row ->
            FoundRowLine(
                row = row,
                columns = found.columns,
                onClick = { onRowClick(row) }
            )
        }
    }
}

@Composable
private fun FoundRowLine(
    row: FoundRow,
    columns: List<String>,
    onClick: () -> Unit
) {
    val cells = row
        .cells
        .mapIndexed { index, cell ->
            columns.getOrNull(index)?.let { "$it: $cell" } ?: cell
        }
        .filter { it.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = NotekeaDimensions.SpacingXs / 2)
    ) {
        Text(
            text = row.note.nameWithoutExtension,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        if (cells.isNotEmpty()) {
            Text(
                text = CELL_SEPARATOR + cells.joinToString(separator = CELL_SEPARATOR),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ShownLine(step: ActionStep) {
    val text = when (step.kind) {
        ActionKind.OPEN_NOTE -> stringResource(
            Res.string.chat_shown_open_note,
            step
                .notes
                .firstOrNull()
                ?.nameWithoutExtension
                .orEmpty()
        )

        ActionKind.OPEN_TODAY -> stringResource(resource = Res.string.chat_shown_open_today)

        ActionKind.SEARCH -> stringResource(Res.string.chat_shown_search, step.text.orEmpty())

        ActionKind.SHOW_PANEL -> stringResource(Res.string.chat_shown_panel, step.panel.orEmpty())

        ActionKind.SPLIT_PANE -> stringResource(resource = Res.string.chat_shown_split)

        ActionKind.CLOSE_OTHER_TABS -> stringResource(resource = Res.string.chat_shown_close_tabs)

        ActionKind.TOGGLE_SOURCE -> stringResource(resource = Res.string.chat_shown_source)

        else -> stringResource(Res.string.chat_shown_theme, step.theme.orEmpty())
    }

    Text(
        text = "✓ $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun problemText(problem: ActionProblem): String = when (problem) {
    is ActionProblem.UnknownNote -> stringResource(Res.string.chat_problem_note, problem.name)

    is ActionProblem.UnknownFolder -> stringResource(Res.string.chat_problem_folder, problem.name)

    is ActionProblem.Missing ->
        stringResource(Res.string.chat_problem_missing, problem.field.written)

    is ActionProblem.TooManyNotes -> stringResource(Res.string.chat_problem_many, problem.count)

    is ActionProblem.Exists ->
        stringResource(Res.string.chat_problem_exists, problem.path.nameWithoutExtension)

    is ActionProblem.UnknownTask -> stringResource(Res.string.chat_problem_task, problem.text)

    is ActionProblem.UnknownQuery -> problem.property?.let {
        stringResource(Res.string.chat_problem_query, it)
    }
        ?: stringResource(resource = Res.string.chat_problem_query_failed)

    is ActionProblem.TextTooLong -> stringResource(Res.string.chat_problem_long, problem.words)

    is ActionProblem.Failed -> stringResource(resource = Res.string.chat_problem_failed)

    is ActionProblem.Refused -> stringResource(resource = Res.string.chat_problem_refused)
}

@Preview
@Composable
private fun ChatActionViewPreview() {
    NotekeaPreviewThemes {
        ChatActionView(
            entryId = 1,
            action = ChatPreviewData.found,
            onIntent = {}
        )
    }
}
