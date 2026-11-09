package app.notekea.query.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import app.notekea.core.presentation.designsystem.components.NotekeaPropertyField
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.query.domain.QueryRow
import app.notekea.query.domain.QuerySort
import app.notekea.resources.Res
import app.notekea.resources.query_note_column
import app.notekea.resources.query_sort_by
import org.jetbrains.compose.resources.stringResource

private const val STRIPE_ALPHA = 0.45f
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
                HeaderText(
                    text = column,
                    weight = 1f,
                    label = stringResource(resource = Res.string.query_sort_by, column),
                    onClick = onSortChange?.let { change -> { change(nextSort(column = column, sort = sort)) } },
                    startPadding = CELL_INSET_DP.dp,
                    order = sort?.takeIf { it.property == column }
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
    startPadding: Dp = 0.dp,
    order: QuerySort? = null
) {
    val action = onClick?.let {
        Modifier.pointerHoverIcon(icon = PointerIcon.Hand).clickable(
            onClickLabel = label,
            role = Role.Button,
            onClick = it
        )
    } ?: Modifier
    val color = if (order == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.weight(weight = weight).then(other = action)
            .padding(start = startPadding, top = NotekeaDimensions.SpacingXs, bottom = NotekeaDimensions.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(weight = 1f, fill = false),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        order?.let {
            val icons = NotekeaIcons
            Icon(
                imageVector = if (it.isDescending) icons.QuerySortDescending else icons.QuerySortAscending,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
                tint = color
            )
        }
    }
}

/** A note of the result, on a stripe every other row: its name opens it, and each cell edits its property. */
@Composable
internal fun QueryTableRow(
    row: QueryRow,
    index: Int,
    columns: List<String>,
    onIntent: (QueryIntent) -> Unit
) {
    val stripe = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = STRIPE_ALPHA)
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = if (index % 2 == 1) stripe else Color.Transparent)
            .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NoteName(row = row, onIntent = onIntent, modifier = Modifier.weight(weight = NAME_WEIGHT))
        columns.forEachIndexed { column, name ->
            Box(modifier = Modifier.weight(weight = 1f)) {
                QueryCell(
                    column = name,
                    onChange = { onIntent(QueryIntent.OnCellChange(path = row.path, property = it)) },
                    property = row.cells.getOrNull(index = column)
                )
            }
        }
    }
}

@Composable
private fun NoteName(
    row: QueryRow,
    onIntent: (QueryIntent) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable(role = Role.Button) { onIntent(QueryIntent.OnNoteOpen(path = row.path)) },
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = NotekeaIcons.Note,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = row.path.nameWithoutExtension,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The property, or an empty field whose text, typed as a note's property would be, adds it to the note. */
@Composable
private fun QueryCell(
    column: String,
    onChange: (NoteProperty) -> Unit,
    property: NoteProperty?
) {
    if (property == null) {
        val reader = remember { FrontmatterReader() }
        NotekeaPropertyField(
            value = "",
            onCommit = { text ->
                if (text.isNotBlank()) {
                    onChange(
                        NoteProperty(key = column, value = reader.scalarValue(text = text.trim()))
                    )
                }
            }
        )
    } else {
        PropertyValueEditor(
            property = property,
            change = { value: PropertyValue -> onChange(property.copy(value = value)) },
            today = null,
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
        result.rows.forEachIndexed { index, row ->
            QueryTableRow(row = row, index = index, columns = result.columns, onIntent = {})
        }
    }
}
