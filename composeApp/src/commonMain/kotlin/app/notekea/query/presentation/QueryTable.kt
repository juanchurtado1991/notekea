package app.notekea.query.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.FrontmatterReader
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.presentation.PropertyValueEditor
import app.notekea.core.presentation.components.NotekeaPropertyField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.query.domain.QueryRow
import app.notekea.query.domain.QuerySort
import app.notekea.resources.Res
import app.notekea.resources.query_note_column
import app.notekea.resources.query_sort_by
import org.jetbrains.compose.resources.stringResource

private const val ASCENDING_MARK = " ↑"
private const val DESCENDING_MARK = " ↓"
private const val CELL_INSET_DP = 4
private const val ITEM_FIELD_WIDTH_DP = 48
private const val NAME_WEIGHT = 1.5f

/**
 * The table's header: with [onSortChange], a click sorts by a column, again to reverse it, and once more to leave it.
 * A block's order is written in its YAML, so its header only shows it.
 */
@Composable
internal fun QueryHeader(
    columns: List<String>,
    sort: QuerySort?,
    onSortChange: ((QuerySort?) -> Unit)?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = NotekeaDimensions.SpacingM),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            HeaderText(text = stringResource(resource = Res.string.query_note_column), weight = NAME_WEIGHT)
            columns.forEach { column ->
                val mark = when {
                    sort?.property != column -> ""
                    sort.isDescending -> DESCENDING_MARK
                    else -> ASCENDING_MARK
                }
                HeaderText(
                    text = column + mark,
                    weight = 1f,
                    label = stringResource(resource = Res.string.query_sort_by, column),
                    onClick = onSortChange?.let { change -> { change(nextSort(column = column, sort = sort)) } },
                    startPadding = CELL_INSET_DP.dp
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private fun nextSort(
    column: String,
    sort: QuerySort?
): QuerySort? = when {
    sort?.property != column -> QuerySort(property = column)
    sort.isDescending -> null
    else -> sort.copy(isDescending = true)
}

@Composable
private fun RowScope.HeaderText(
    text: String,
    weight: Float,
    label: String? = null,
    onClick: (() -> Unit)? = null,
    startPadding: Dp = 0.dp
) {
    val action = onClick?.let {
        Modifier.pointerHoverIcon(icon = PointerIcon.Hand).clickable(
            onClickLabel = label,
            role = Role.Button,
            onClick = it
        )
    } ?: Modifier
    Text(
        text = text,
        modifier = Modifier.weight(weight = weight).then(other = action)
            .padding(start = startPadding, top = NotekeaDimensions.SpacingXs, bottom = NotekeaDimensions.SpacingXs),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** A note of the result: its name opens it, and each cell edits its property as the properties view does. */
@Composable
internal fun QueryTableRow(
    row: QueryRow,
    columns: List<String>,
    onIntent: (QueryIntent) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingXs
            ),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = row.path.nameWithoutExtension,
                modifier = Modifier.weight(weight = NAME_WEIGHT)
                    .pointerHoverIcon(icon = PointerIcon.Hand)
                    .clickable(role = Role.Button) { onIntent(QueryIntent.OnNoteOpen(path = row.path)) },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            columns.forEachIndexed { index, column ->
                Box(modifier = Modifier.weight(weight = 1f)) {
                    QueryCell(
                        property = row.cells.getOrNull(index = index),
                        column = column,
                        onChange = { onIntent(QueryIntent.OnCellChange(path = row.path, property = it)) }
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** The property, or an empty field whose text, typed as a note's property would be, adds it to the note. */
@Composable
private fun QueryCell(
    property: NoteProperty?,
    column: String,
    onChange: (NoteProperty) -> Unit
) {
    if (property == null) {
        val reader = remember { FrontmatterReader() }
        NotekeaPropertyField(
            value = "",
            onCommit = { text ->
                if (text.isNotBlank()) onChange(NoteProperty(key = column, value = reader.scalarValue(text = text.trim())))
            }
        )
    } else {
        PropertyValueEditor(
            property = property,
            today = null,
            change = { value: PropertyValue -> onChange(property.copy(value = value)) },
            itemFieldWidth = ITEM_FIELD_WIDTH_DP.dp
        )
    }
}

@Preview
@Composable
private fun QueryHeaderPreview() {
    NotekeaPreviewThemes {
        val state = QueryPreviewData.state
        QueryHeader(columns = state.result.columns, sort = state.query.sort, onSortChange = {})
    }
}

@Preview
@Composable
private fun QueryTableRowPreview() {
    NotekeaPreviewThemes {
        val result = QueryPreviewData.state.result
        result.rows.forEach { QueryTableRow(row = it, columns = result.columns, onIntent = {}) }
    }
}
