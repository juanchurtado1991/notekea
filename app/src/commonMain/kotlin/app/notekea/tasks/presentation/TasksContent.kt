package app.notekea.tasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.presentation.designsystem.components.feedback.EmptyState
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaNotice
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSegment
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSegmentedControl
import app.notekea.core.presentation.designsystem.components.layout.NotekeaLazyScrollArea
import app.notekea.core.presentation.designsystem.components.layout.NotekeaToolButton
import app.notekea.core.presentation.designsystem.components.layout.NotekeaToolMenu
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.tasks_all_folders
import app.notekea.resources.tasks_all_tags
import app.notekea.resources.tasks_by_date
import app.notekea.resources.tasks_by_note
import app.notekea.resources.tasks_count
import app.notekea.resources.tasks_done_label
import app.notekea.resources.tasks_empty_message
import app.notekea.resources.tasks_empty_title
import app.notekea.resources.tasks_filter_folder
import app.notekea.resources.tasks_filter_tag
import app.notekea.resources.tasks_hide_done
import app.notekea.resources.tasks_notice_dismiss
import app.notekea.resources.tasks_show_done
import app.notekea.resources.tasks_when_done
import app.notekea.resources.tasks_when_later
import app.notekea.resources.tasks_when_no_date
import app.notekea.resources.tasks_when_overdue
import app.notekea.resources.tasks_when_this_week
import app.notekea.resources.tasks_when_today
import app.notekea.resources.tasks_write_failed
import app.notekea.tasks.domain.TaskGroup
import app.notekea.tasks.domain.TaskGrouping
import app.notekea.tasks.domain.TaskList
import app.notekea.tasks.domain.TaskRow
import app.notekea.tasks.domain.TaskView
import app.notekea.tasks.domain.TaskWhen
import org.jetbrains.compose.resources.stringResource

private const val CARD_MAX_WIDTH_DP = 760
private const val HEAD = "head"
private const val ROW = "row"

/**
 * The Tasks tab: the vault's open tasks in cards, one per note or per date ([TaskCardHead]), with what limits
 * them above and how many at the foot. A box checks its task in its note, and a task's text opens it there.
 */
@Composable
fun TasksContent(
    state: TasksState,
    onIntent: (TasksIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TasksBar(
            view = state.view,
            list = state.list,
            onIntent = onIntent
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (state.isWriteFailed) WriteFailed(onIntent = onIntent)

        when {
            state.isLoading -> Spacer(modifier = Modifier.weight(weight = 1f))

            state.list.groups.isEmpty() -> EmptyTasks(modifier = Modifier.weight(weight = 1f))

            else -> TaskGroups(
                list = state.list,
                onIntent = onIntent,
                modifier = Modifier.weight(weight = 1f)
            )
        }

        if (!state.isLoading) TasksFoot(list = state.list)
    }
}

@Composable
private fun WriteFailed(onIntent: (TasksIntent) -> Unit) {
    NotekeaNotice(
        message = stringResource(resource = Res.string.tasks_write_failed),
        icon = NotekeaIcons.CalloutWarning,
        dismissLabel = stringResource(resource = Res.string.tasks_notice_dismiss),
        onDismiss = { onIntent(TasksIntent.OnNoticeDismiss) },
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS)
    )
}

@Composable
private fun TasksBar(
    view: TaskView,
    list: TaskList,
    onIntent: (TasksIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = NotekeaDimensions.SpacingM,
            vertical = NotekeaDimensions.SpacingXs
        ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaSegmentedControl(
            segments = listOf(
                NotekeaSegment(
                    icon = NotekeaIcons.Note,
                    label = stringResource(Res.string.tasks_by_note)
                ),
                NotekeaSegment(
                    icon = NotekeaIcons.Today,
                    label = stringResource(Res.string.tasks_by_date)
                )
            ),
            selectedIndex = view.grouping.ordinal,
            onSelect = {
                onIntent(TasksIntent.OnGroupingChange(grouping = TaskGrouping.entries[it]))
            }
        )

        Spacer(modifier = Modifier.weight(weight = 1f))

        LimitMenus(
            view = view,
            list = list,
            onIntent = onIntent
        )

        val doneLabel =
            if (view.showsDone) Res.string.tasks_hide_done else Res.string.tasks_show_done

        NotekeaToolButton(
            icon = NotekeaIcons.TasksShowDone,
            text = stringResource(resource = doneLabel),
            onClick = { onIntent(TasksIntent.OnShowDoneToggle) }
        )
    }
}

@Composable
private fun LimitMenus(
    view: TaskView,
    list: TaskList,
    onIntent: (TasksIntent) -> Unit
) {
    NotekeaToolMenu(
        icon = NotekeaIcons.Folder,
        text = view.folder ?: stringResource(resource = Res.string.tasks_filter_folder),
        sections = limitSections(
            all = stringResource(resource = Res.string.tasks_all_folders),
            values = list.folders,
            chosen = view.folder,
            onChoose = { onIntent(TasksIntent.OnFolderChange(folder = it)) }
        )
    )

    NotekeaToolMenu(
        icon = NotekeaIcons.Tag,
        text = view.tag ?: stringResource(resource = Res.string.tasks_filter_tag),
        sections = limitSections(
            all = stringResource(resource = Res.string.tasks_all_tags),
            values = list.tags,
            chosen = view.tag,
            onChoose = { onIntent(TasksIntent.OnTagChange(tag = it)) }
        )
    )
}

/** "All", which takes the limit away, then the [values] it can be limited to. */
private fun limitSections(
    all: String,
    values: List<String>,
    chosen: String?,
    onChoose: (String?) -> Unit
): List<List<NotekeaMenuItem>> = listOf(
    listOf(
        NotekeaMenuItem(
            label = all,
            onClick = { onChoose(null) },
            isChecked = chosen == null
        )
    ),
    values.map {
        NotekeaMenuItem(
            label = it,
            onClick = { onChoose(it) },
            isChecked = it == chosen
        )
    }
).filter { it.isNotEmpty() }

@Composable
private fun EmptyTasks(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            title = stringResource(resource = Res.string.tasks_empty_title),
            message = stringResource(resource = Res.string.tasks_empty_message)
        )
    }
}

@Composable
private fun TaskGroups(
    list: TaskList,
    onIntent: (TasksIntent) -> Unit,
    modifier: Modifier
) {
    val scroll = rememberLazyListState()

    NotekeaLazyScrollArea(
        state = scroll,
        modifier = modifier
    ) {
        LazyColumn(
            state = scroll,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
            contentPadding = PaddingValues(all = NotekeaDimensions.SpacingM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            list.groups.forEach { group ->
                item(
                    key = keyOf(group = group),
                    contentType = HEAD
                ) {
                    TaskCardHead(
                        group = group,
                        modifier = Modifier.widthIn(max = CARD_MAX_WIDTH_DP.dp)
                    )
                }

                itemsIndexed(
                    items = group.rows,
                    key = { _, row ->
                        row.path.value + row.task.stateIndex
                    },
                    contentType = { _, _ -> ROW }
                ) { index, row ->
                    TaskCardRow(
                        row = row,
                        isInNote = group is TaskGroup.OfNote,
                        isLast = index == group.rows.lastIndex,
                        onIntent = onIntent,
                        modifier = Modifier.widthIn(max = CARD_MAX_WIDTH_DP.dp)
                    )
                }
            }
        }
    }
}

private fun keyOf(group: TaskGroup): String = when (group) {
    is TaskGroup.OfNote -> group.path.value
    is TaskGroup.OfDate -> group.date.name
}

@Composable
private fun TasksFoot(list: TaskList) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    Text(
        text = stringResource(resource = Res.string.tasks_count, list.open, list.total),
        modifier = Modifier.fillMaxWidth().padding(vertical = NotekeaDimensions.SpacingXs),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun TasksContentPreview() {
    NotekeaPreviewThemes {
        TasksContent(
            state = TasksPreviewData.state,
            onIntent = {}
        )
    }
}
