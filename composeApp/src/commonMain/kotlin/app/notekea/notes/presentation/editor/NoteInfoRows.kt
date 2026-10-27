package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.NoteStats
import app.notekea.resources.Res
import app.notekea.resources.note_info_created
import app.notekea.resources.note_info_links
import app.notekea.resources.note_info_modified
import app.notekea.resources.note_info_reading
import app.notekea.resources.note_info_reading_minutes
import app.notekea.resources.note_info_tags
import app.notekea.resources.note_info_tasks
import app.notekea.resources.note_info_tasks_value
import app.notekea.resources.note_info_words
import org.jetbrains.compose.resources.stringResource

private const val CREATED_PATTERN = "MMM D, YYYY"
private const val MODIFIED_PATTERN = "MMM D, YYYY, HH:mm"
private const val PREVIEW_WIDTH_DP = 300

/**
 * Note info's rows, a label on the left and its value on the right, each with its icon: words, reading time, links,
 * tags and, when the note has any, tasks done of all; then when the file was created and modified, where known.
 */
@Composable
internal fun NoteInfoRows(
    info: NoteInfo
) {
    Column(modifier = Modifier.padding(vertical = NotekeaDimensions.SpacingXs)) {
        info.stats?.let { StatsRows(stats = it) }
        val format = remember { MomentFormat() }
        info.createdAt?.let {
            InfoRow(
                icon = NotekeaIcons.InfoCreated,
                label = stringResource(resource = Res.string.note_info_created),
                value = format.format(moment = it, pattern = CREATED_PATTERN)
            )
        }
        info.modifiedAt?.let {
            InfoRow(
                icon = NotekeaIcons.InfoModified,
                label = stringResource(resource = Res.string.note_info_modified),
                value = format.format(moment = it, pattern = MODIFIED_PATTERN)
            )
        }
    }
}

@Composable
private fun StatsRows(
    stats: NoteStats
) {
    InfoRow(
        icon = NotekeaIcons.InfoWords,
        label = stringResource(resource = Res.string.note_info_words),
        value = stats.words.toString()
    )
    InfoRow(
        icon = NotekeaIcons.InfoReading,
        label = stringResource(resource = Res.string.note_info_reading),
        value = stringResource(Res.string.note_info_reading_minutes, stats.readingMinutes)
    )
    InfoRow(
        icon = NotekeaIcons.InfoLinks,
        label = stringResource(resource = Res.string.note_info_links),
        value = stats.links.toString()
    )
    InfoRow(
        icon = NotekeaIcons.InfoTags,
        label = stringResource(resource = Res.string.note_info_tags),
        value = stats.tags.toString()
    )
    if (stats.tasks > 0) {
        InfoRow(
            icon = NotekeaIcons.InfoTasks,
            label = stringResource(resource = Res.string.note_info_tasks),
            value = stringResource(Res.string.note_info_tasks_value, stats.doneTasks, stats.tasks)
        )
    }
}

/** One row: [icon] and [label] in the muted color, [value] in ink at the end. */
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = muted
        )
        Text(
            text = label,
            modifier = Modifier.weight(weight = 1f).padding(start = NotekeaDimensions.SpacingS),
            style = MaterialTheme.typography.bodyMedium,
            color = muted
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
private fun NoteInfoRowsPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NoteInfoRows(info = EditorPreviewData.info)
        }
    }
}
