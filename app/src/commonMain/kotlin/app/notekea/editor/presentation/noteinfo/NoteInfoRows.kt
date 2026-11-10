package app.notekea.editor.presentation.noteinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.domain.NoteStats
import app.notekea.editor.presentation.model.EditorPreviewData
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

private const val INFO_LABEL_WIDTH_DP = 88
private const val INFO_ROW_PADDING_DP = 3
private const val MUTED_ALPHA = 0.5f
private const val PROGRESS_HEIGHT_DP = 3
private const val PROGRESS_WIDTH_DP = 56
private const val CREATED_PATTERN = "MMM D, YYYY"
private const val MODIFIED_PATTERN = "MMM D, YYYY, HH:mm"
private const val PREVIEW_WIDTH_DP = 300

/**
 * Note info's rows as a compact table, each label with its value beside it: words, reading
 * time, links, tags and, when the note has any, tasks done of all; then when the file was created and modified, where
 * known.
 */
@Composable
internal fun NoteInfoRows(info: NoteInfo) {
    Column(modifier = Modifier.padding(bottom = NotekeaDimensions.SpacingS)) {
        info.stats?.let { StatsRows(stats = it) }

        val format = remember { MomentFormat() }

        info.createdAt?.let {
            InfoRow(
                label = stringResource(resource = Res.string.note_info_created),
                value = format.format(
                    moment = it,
                    pattern = CREATED_PATTERN
                )
            )
        }

        info.modifiedAt?.let {
            InfoRow(
                label = stringResource(resource = Res.string.note_info_modified),
                value = format.format(
                    moment = it,
                    pattern = MODIFIED_PATTERN
                )
            )
        }
    }
}

@Composable
private fun StatsRows(stats: NoteStats) {
    InfoRow(
        label = stringResource(resource = Res.string.note_info_words),
        value = stats.words.toString()
    )

    InfoRow(
        label = stringResource(resource = Res.string.note_info_reading),
        value = stringResource(Res.string.note_info_reading_minutes, stats.readingMinutes)
    )

    InfoRow(
        label = stringResource(resource = Res.string.note_info_links),
        value = stats.links.toString(),
        isMuted = stats.links == 0
    )

    InfoRow(
        label = stringResource(resource = Res.string.note_info_tags),
        value = stats.tags.toString(),
        isMuted = stats.tags == 0
    )

    if (stats.tasks > 0) {
        InfoRow(
            label = stringResource(resource = Res.string.note_info_tasks),
            value = stringResource(Res.string.note_info_tasks_value, stats.doneTasks, stats.tasks),
            progress = stats.doneTasks.toFloat() / stats.tasks
        )
    }
}

/**
 * One row, as an inspector's table: [label] in the muted color in a column of its own, [value] in ink beside it, or
 * muted too when it says nothing ([isMuted]: no links, no tags), and a thin bar of how much is done ([progress]).
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    isMuted: Boolean = false,
    progress: Float? = null
) {
    val colors = MaterialTheme.colorScheme

    val valueColor = if (isMuted) {
        colors.onSurfaceVariant.copy(alpha = MUTED_ALPHA)
    } else {
        colors.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = INFO_ROW_PADDING_DP.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(width = INFO_LABEL_WIDTH_DP.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )

        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .padding(start = NotekeaDimensions.SpacingS)
                    .width(width = PROGRESS_WIDTH_DP.dp)
                    .height(height = PROGRESS_HEIGHT_DP.dp),
                color = colors.primary,
                trackColor = colors.surfaceVariant,
                drawStopIndicator = {}
            )
        }
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
