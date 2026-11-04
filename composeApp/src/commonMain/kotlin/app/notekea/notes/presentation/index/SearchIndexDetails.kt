package app.notekea.notes.presentation.index

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.elapsedTimeText
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.IndexProgress
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SkipReason
import app.notekea.notes.domain.SkippedNote
import app.notekea.resources.Res
import app.notekea.resources.search_index_error_busy
import app.notekea.resources.search_index_error_model
import app.notekea.resources.search_index_last_run
import app.notekea.resources.search_index_more
import app.notekea.resources.search_index_never
import app.notekea.resources.search_index_progress
import app.notekea.resources.search_index_rebuild
import app.notekea.resources.search_index_skipped
import app.notekea.resources.search_index_skipped_files
import app.notekea.resources.search_index_title
import app.notekea.resources.search_index_took
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import app.notekea.resources.search_index_unreadable
import app.notekea.resources.search_index_took_seconds
import app.notekea.resources.search_index_took_minutes
import app.notekea.resources.search_index_too_long
import app.notekea.resources.search_index_too_large
import app.notekea.resources.search_index_minutes_left
import app.notekea.resources.search_index_less_than_a_minute
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration

private const val FACT_LABEL_DP = 90
private const val SECONDS_PER_MINUTE = 60
private const val SHOWN_SKIPPED = 5
private const val PREVIEW_TOTAL = 1204
private const val PREVIEW_DONE = 340
private const val PREVIEW_MINUTES = 42
private const val PREVIEW_TOOK_SECONDS = 108

/**
 * The search index's details (`ROADMAP.md` 2.3, design B), in the status bar's popover: while a run goes on its bar, how
 * many notes of how many and the time left; then when the last run was, how long it took, the notes it skipped (the
 * first five, with why) and Rebuild index; why the last run stopped, if it did.
 */
@Composable
internal fun ColumnScope.SearchIndexDetails(
    state: SearchIndexState,
    onIntent: (SearchIndexIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        Text(
            text = stringResource(Res.string.search_index_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        state.progress?.takeIf { state.isRunning }?.let { RunProgress(state = state, progress = it) }
        state.error?.let { ErrorText(error = it) }
        val never = stringResource(Res.string.search_index_never)
        Fact(
            label = stringResource(Res.string.search_index_last_run),
            value = state.lastRun?.let { elapsedTimeText(elapsed = it) } ?: never
        )
        state.took?.let { Fact(label = stringResource(Res.string.search_index_took), value = tookText(took = it)) }
        Skipped(skipped = state.progress?.skipped.orEmpty())
        Row {
            Spacer(modifier = Modifier.weight(weight = 1f))
            NotekeaButton(
                text = stringResource(Res.string.search_index_rebuild),
                onClick = { onIntent(SearchIndexIntent.OnRebuildClick) },
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

@Composable
private fun RunProgress(
    state: SearchIndexState,
    progress: IndexProgress
) {
    LinearProgressIndicator(
        progress = { state.fraction },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    val counts = stringResource(
        Res.string.search_index_progress,
        grouped(count = progress.done),
        grouped(count = progress.total)
    )
    val left = state.remaining?.let { " · " + remainingText(remaining = it) }.orEmpty()
    MutedText(text = counts + left)
}

@Composable
private fun ErrorText(
    error: IndexRunError
) {
    Text(
        text = when (error) {
            IndexRunError.Model -> stringResource(Res.string.search_index_error_model)
            IndexRunError.Index -> stringResource(Res.string.search_index_error_busy)
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

/** How many notes were skipped, then the first [SHOWN_SKIPPED] of them with why, and how many more. */
@Composable
private fun Skipped(
    skipped: List<SkippedNote>
) {
    if (skipped.isEmpty()) return
    Fact(
        label = stringResource(Res.string.search_index_skipped),
        value = pluralStringResource(Res.plurals.search_index_skipped_files, skipped.size, skipped.size)
    )
    skipped.take(n = SHOWN_SKIPPED).forEach {
        MutedText(text = "${it.path.value} — ${skipReasonText(reason = it.reason)}")
    }
    val more = skipped.size - SHOWN_SKIPPED
    if (more > 0) MutedText(text = pluralStringResource(Res.plurals.search_index_more, more, more))
}

@Composable
private fun Fact(
    label: String,
    value: String
) {
    Row {
        Text(
            text = label,
            modifier = Modifier.width(width = FACT_LABEL_DP.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MutedText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** How long a run took: "1 min 48 s", or "12 s" under a minute. */
@Composable
private fun tookText(
    took: Duration
): String {
    val seconds = took.inWholeSeconds.toInt()
    val minutes = seconds / SECONDS_PER_MINUTE
    return if (minutes == 0) {
        stringResource(Res.string.search_index_took_seconds, seconds)
    } else {
        stringResource(Res.string.search_index_took_minutes, minutes, seconds % SECONDS_PER_MINUTE)
    }
}

/** How long a run has left: "about 2 minutes left", or "less than a minute left". */
@Composable
private fun remainingText(
    remaining: Duration
): String {
    val minutes = remaining.inWholeMinutes.toInt()
    return if (minutes < 1) {
        stringResource(Res.string.search_index_less_than_a_minute)
    } else {
        pluralStringResource(Res.plurals.search_index_minutes_left, minutes, minutes)
    }
}

/** Why a note was skipped, after its path: "larger than 5 MB". */
@Composable
private fun skipReasonText(
    reason: SkipReason
): String = when (reason) {
    SkipReason.TOO_LARGE -> stringResource(Res.string.search_index_too_large)
    SkipReason.UNREADABLE -> stringResource(Res.string.search_index_unreadable)
    SkipReason.TOO_LONG -> stringResource(Res.string.search_index_too_long)
}

@Preview
@Composable
private fun SearchIndexDetailsPreview() {
    NotekeaPreviewThemes {
        Column {
            SearchIndexDetails(
                state = SearchIndexState(
                    isRunning = true,
                    progress = IndexProgress(
                        total = PREVIEW_TOTAL,
                        done = PREVIEW_DONE,
                        skipped = listOf(
                            SkippedNote(path = RelativePath(value = "Huge export.md"), reason = SkipReason.TOO_LARGE)
                        )
                    ),
                    remaining = PREVIEW_TOOK_SECONDS.seconds,
                    lastRun = ElapsedTime.Minutes(count = PREVIEW_MINUTES),
                    took = PREVIEW_TOOK_SECONDS.seconds
                ),
                onIntent = {}
            )
        }
    }
}
