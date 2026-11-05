package app.notekea.query.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaLazyScrollArea
import app.notekea.core.presentation.components.NotekeaNotice
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.query.domain.QueryResult
import app.notekea.resources.Res
import app.notekea.resources.query_no_matches
import app.notekea.resources.query_no_matches_message
import app.notekea.resources.query_notice_dismiss
import app.notekea.resources.query_saved
import app.notekea.resources.query_unknown_property
import app.notekea.resources.query_write_failed
import org.jetbrains.compose.resources.stringResource

/**
 * The Query tab (`ROADMAP.md` 2.10): the query in chips over a table of the notes it finds, computed exactly from the
 * notes' properties. A property no note has says so, and no match says what to do. [saveTarget] is the note a query
 * can be saved in, the one beside the tab.
 */
@Composable
fun QueryContent(
    state: QueryState,
    saveTarget: RelativePath?,
    onIntent: (QueryIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        QueryBar(state = state, saveTarget = saveTarget, onIntent = onIntent)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        QueryNoticeLine(notice = state.notice, onIntent = onIntent)
        UnknownProperties(unknown = state.result.unknown)
        when {
            state.isLoading -> Unit

            state.result.rows.isEmpty() -> Box(
                modifier = Modifier.weight(weight = 1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = stringResource(resource = Res.string.query_no_matches),
                    message = stringResource(resource = Res.string.query_no_matches_message)
                )
            }

            else -> ResultTable(state = state, onIntent = onIntent, modifier = Modifier.weight(weight = 1f))
        }
    }
}

@Composable
private fun ResultTable(
    state: QueryState,
    onIntent: (QueryIntent) -> Unit,
    modifier: Modifier
) {
    val result: QueryResult = state.result
    val list = rememberLazyListState()
    Column(modifier = modifier) {
        QueryHeader(
            columns = result.columns,
            sort = state.query.sort,
            onSortChange = { onIntent(QueryIntent.OnSortChange(sort = it)) }
        )
        NotekeaLazyScrollArea(state = list, modifier = Modifier.weight(weight = 1f)) {
            LazyColumn(state = list, modifier = Modifier.fillMaxSize()) {
                items(items = result.rows, key = { it.path.value }) { row ->
                    QueryTableRow(row = row, columns = result.columns, onIntent = onIntent)
                }
            }
        }
    }
}

/** What the last write told the person, until it is dismissed. */
@Composable
internal fun QueryNoticeLine(
    notice: QueryNotice?,
    onIntent: (QueryIntent) -> Unit
) {
    val message = when (notice) {
        is QueryNotice.Saved -> stringResource(resource = Res.string.query_saved, notice.path.nameWithoutExtension)
        QueryNotice.WriteFailed -> stringResource(resource = Res.string.query_write_failed)
        null -> return
    }
    NotekeaNotice(
        message = message,
        icon = if (notice is QueryNotice.Saved) NotekeaIcons.Checked else NotekeaIcons.CalloutWarning,
        dismissLabel = stringResource(resource = Res.string.query_notice_dismiss),
        onDismiss = { onIntent(QueryIntent.OnNoticeDismiss) },
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS)
    )
}

@Composable
internal fun UnknownProperties(
    unknown: List<String>
) {
    unknown.forEach { property ->
        Text(
            text = stringResource(resource = Res.string.query_unknown_property, property),
            modifier = Modifier.padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingXs
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun QueryNoticeLinePreview() {
    NotekeaPreviewThemes {
        QueryNoticeLine(notice = QueryNotice.WriteFailed, onIntent = {})
    }
}

@Preview
@Composable
private fun UnknownPropertiesPreview() {
    NotekeaPreviewThemes {
        UnknownProperties(unknown = QueryPreviewData.state.query.columns)
    }
}

@Preview
@Composable
private fun QueryContentPreview() {
    NotekeaPreviewThemes {
        QueryContent(state = QueryPreviewData.state, saveTarget = null, onIntent = {})
    }
}
