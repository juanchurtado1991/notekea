package app.notekea.query.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaChip
import app.notekea.core.presentation.designsystem.components.NotekeaMarkdown
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.query.domain.QueryParse
import app.notekea.query.domain.QueryResult
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.VaultQuery
import app.notekea.resources.Res
import app.notekea.resources.query_invalid_title
import app.notekea.resources.query_more_rows
import org.jetbrains.compose.resources.stringResource

private const val MAX_ROWS = 100
private const val PREVIEW_BLOCK = "columns: [author, rating]"
private const val PREVIEW_SOURCE = "```query\ncolumns: [author, rating]\n```"

/**
 * A ```` ```query ```` block as Preview shows it (`ROADMAP.md` 2.10): the live table of what its [text] finds ([run]),
 * again whenever [notesVersion] changes, under the query's chips, which are edited as the block's YAML. A block that
 * is not a query says which line cannot be read, above its [source]. A note stays light: past [MAX_ROWS], the table
 * says how many it leaves out.
 */
@Composable
fun QueryBlock(
    text: String,
    source: String,
    notesVersion: Int,
    run: suspend (VaultQuery) -> QueryResult,
    onIntent: (QueryIntent) -> Unit,
    notice: QueryNotice?,
    modifier: Modifier = Modifier
) {
    val parse = remember(text) { QueryYaml().read(text = text) }
    Column(modifier = modifier.fillMaxWidth()) {
        when (parse) {
            is QueryParse.Invalid -> InvalidBlock(invalid = parse, source = source)
            is QueryParse.Valid -> ValidBlock(query = parse.query, notesVersion, run, onIntent, notice)
        }
    }
}

@Composable
private fun InvalidBlock(
    invalid: QueryParse.Invalid,
    source: String
) {
    Text(
        text = stringResource(resource = Res.string.query_invalid_title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.error
    )
    Text(text = invalidLabel(invalid = invalid), style = MaterialTheme.typography.bodySmall)
    NotekeaMarkdown(content = source)
}

@Composable
private fun ValidBlock(
    query: VaultQuery,
    notesVersion: Int,
    run: suspend (VaultQuery) -> QueryResult,
    onIntent: (QueryIntent) -> Unit,
    notice: QueryNotice?
) {
    // The last result stays until the next one, so the note does not jump when the vault changes.
    val result by produceState<QueryResult?>(initialValue = null, query, notesVersion) {
        value = run(query)
    }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().border(
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outlineVariant),
            shape = MaterialTheme.shapes.medium
        )
    ) {
        Chips(query = query, count = result?.rows?.size)
        QueryNoticeLine(notice = notice, onIntent = onIntent)
        result?.let { found -> FoundRows(query = query, result = found, onIntent = onIntent) }
    }
}

@Composable
private fun Chips(
    query: VaultQuery,
    count: Int?
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        query.filters.forEach { NotekeaChip(label = filterLabel(filter = it)) }
        count?.let {
            Text(
                text = noteCountLabel(count = it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FoundRows(
    query: VaultQuery,
    result: QueryResult,
    onIntent: (QueryIntent) -> Unit
) {
    UnknownProperties(unknown = result.unknown)
    if (result.rows.isEmpty()) return
    QueryHeader(columns = result.columns, sort = query.sort, onSortChange = null)
    result.rows.take(n = MAX_ROWS).forEachIndexed { index, row ->
        QueryTableRow(row = row, index = index, columns = result.columns, onIntent = onIntent)
    }
    if (result.rows.size > MAX_ROWS) {
        Text(
            text = stringResource(resource = Res.string.query_more_rows, MAX_ROWS, result.rows.size),
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun QueryBlockPreview() {
    NotekeaPreviewThemes {
        QueryBlock(
            text = PREVIEW_BLOCK,
            source = PREVIEW_SOURCE,
            notesVersion = 0,
            run = { QueryPreviewData.state.result },
            onIntent = {},
            notice = null
        )
    }
}
