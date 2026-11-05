package app.notekea.query.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaChip
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaContextMenuState
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.query.domain.QueryCatalog
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryKey
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.QueryValueKind
import app.notekea.query.domain.VaultQuery
import app.notekea.resources.Res
import app.notekea.resources.query_add_filter
import app.notekea.resources.query_columns
import app.notekea.resources.query_filter_folder
import app.notekea.resources.query_filter_tag
import app.notekea.resources.query_remove_filter
import app.notekea.resources.query_save_in
import app.notekea.resources.query_sort
import app.notekea.resources.query_sort_ascending
import app.notekea.resources.query_sort_descending
import app.notekea.resources.query_sort_name
import org.jetbrains.compose.resources.stringResource

/**
 * The query above its table (`ROADMAP.md` 2.10, design A): a chip per filter, which edits it or removes it, Add filter,
 * and at the end Sort, Columns, how many notes it found and, with a note at hand, saving it in [saveTarget].
 */
@Composable
internal fun QueryBar(
    state: QueryState,
    saveTarget: RelativePath?,
    onIntent: (QueryIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowRow(
            modifier = Modifier.weight(weight = 1f),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            state.query.filters.forEachIndexed { index, filter ->
                FilterChip(index = index, filter = filter, catalog = state.catalog, onIntent = onIntent)
            }
            AddFilter(catalog = state.catalog, onIntent = onIntent)
        }
        SortMenu(query = state.query, catalog = state.catalog, onIntent = onIntent)
        ColumnsMenu(query = state.query, catalog = state.catalog, onIntent = onIntent)
        Text(
            text = noteCountLabel(count = state.result.rows.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        saveTarget?.let { note ->
            NotekeaButton(
                text = stringResource(resource = Res.string.query_save_in, note.nameWithoutExtension),
                onClick = { onIntent(QueryIntent.OnSave(path = note)) },
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

@Composable
private fun FilterChip(
    index: Int,
    filter: QueryFilter,
    catalog: QueryCatalog,
    onIntent: (QueryIntent) -> Unit
) {
    var isEditing by remember { mutableStateOf(value = false) }
    Box {
        NotekeaChip(
            label = filterLabel(filter = filter),
            onRemove = { onIntent(QueryIntent.OnFilterRemove(index = index)) },
            removeLabel = stringResource(resource = Res.string.query_remove_filter),
            onClick = { isEditing = true }.takeIf { filter.condition.needsProperty }
        )
        if (isEditing) {
            QueryFilterEditor(
                filter = filter,
                kind = kindOf(property = filter.property, catalog = catalog),
                onApply = {
                    isEditing = false
                    onIntent(QueryIntent.OnFilterChange(index = index, filter = it))
                },
                onDismiss = { isEditing = false }
            )
        }
    }
}

private fun kindOf(
    property: String,
    catalog: QueryCatalog
): QueryValueKind = catalog.keys.firstOrNull { it.name == property }?.kind ?: QueryValueKind.TEXT

/** Add filter's menu: a folder or a tag add their filter at once, and a property asks how to test it first. */
@Composable
private fun AddFilter(
    catalog: QueryCatalog,
    onIntent: (QueryIntent) -> Unit
) {
    var adding by remember { mutableStateOf<QueryKey?>(value = null) }
    val add = { condition: QueryCondition, value: String ->
        onIntent(QueryIntent.OnFilterAdd(filter = QueryFilter(condition = condition, value = value)))
    }
    val noteItems = listOfNotNull(
        menuOf(
            label = stringResource(resource = Res.string.query_filter_folder),
            values = catalog.folders,
            onChoose = { add(QueryCondition.IN_FOLDER, it) }
        ),
        menuOf(
            label = stringResource(resource = Res.string.query_filter_tag),
            values = catalog.tags,
            onChoose = { add(QueryCondition.HAS_TAG, it) }
        )
    )
    val keyItems = catalog.keys.map { key -> NotekeaMenuItem(label = key.name, onClick = { adding = key }) }
    Box {
        MenuButton(
            text = stringResource(resource = Res.string.query_add_filter),
            sections = listOf(noteItems, keyItems).filter { it.isNotEmpty() }
        )
        adding?.let { key ->
            QueryFilterEditor(
                filter = QueryFilter(condition = key.kind.conditions.first(), property = key.name),
                kind = key.kind,
                onApply = {
                    adding = null
                    onIntent(QueryIntent.OnFilterAdd(filter = it))
                },
                onDismiss = { adding = null }
            )
        }
    }
}

/** An item that opens [values] beside the menu, or null when there are none. */
private fun menuOf(
    label: String,
    values: List<String>,
    onChoose: (String) -> Unit
): NotekeaMenuItem? = values.takeIf { it.isNotEmpty() }?.let { found ->
    NotekeaMenuItem(
        label = label,
        onClick = {},
        items = listOf(found.map { value -> NotekeaMenuItem(label = value, onClick = { onChoose(value) }) })
    )
}

@Composable
private fun SortMenu(
    query: VaultQuery,
    catalog: QueryCatalog,
    onIntent: (QueryIntent) -> Unit
) {
    val sort = query.sort
    val choose = { chosen: QuerySort? -> onIntent(QueryIntent.OnSortChange(sort = chosen)) }
    val byName = NotekeaMenuItem(
        label = stringResource(resource = Res.string.query_sort_name),
        onClick = { choose(null) },
        isChecked = sort == null
    )
    val keys = catalog.keys.map { key ->
        NotekeaMenuItem(
            label = key.name,
            onClick = { choose(QuerySort(property = key.name, isDescending = sort?.isDescending == true)) },
            isChecked = sort?.property == key.name
        )
    }
    val order = sort?.let { current ->
        listOf(
            NotekeaMenuItem(
                label = stringResource(resource = Res.string.query_sort_ascending),
                onClick = { choose(current.copy(isDescending = false)) },
                isChecked = !current.isDescending
            ),
            NotekeaMenuItem(
                label = stringResource(resource = Res.string.query_sort_descending),
                onClick = { choose(current.copy(isDescending = true)) },
                isChecked = current.isDescending
            )
        )
    }
    MenuButton(
        text = stringResource(resource = Res.string.query_sort),
        sections = listOfNotNull(listOf(byName) + keys, order)
    )
}

@Composable
private fun ColumnsMenu(
    query: VaultQuery,
    catalog: QueryCatalog,
    onIntent: (QueryIntent) -> Unit
) {
    val shown = query.shownColumns
    MenuButton(
        text = stringResource(resource = Res.string.query_columns),
        sections = listOf(
            catalog.keys.map { key ->
                NotekeaMenuItem(
                    label = key.name,
                    onClick = { onIntent(QueryIntent.OnColumnToggle(column = key.name)) },
                    isChecked = key.name in shown,
                    keepsMenuOpen = true
                )
            }
        )
    )
}

/** A quiet button that opens a menu of [sections] below itself. */
@Composable
private fun MenuButton(
    text: String,
    sections: List<List<NotekeaMenuItem>>
) {
    val menu = remember { NotekeaContextMenuState() }
    val below = with(LocalDensity.current) { Offset(x = 0f, y = NotekeaDimensions.CompactControlSize.toPx()) }
    NotekeaContextMenuArea(sections = sections, state = menu) {
        NotekeaButton(text = text, onClick = { menu.open(at = below) }, style = NotekeaButtonStyle.TEXT)
    }
}

@Preview
@Composable
private fun QueryBarPreview() {
    NotekeaPreviewThemes {
        QueryBar(
            state = QueryPreviewData.state,
            saveTarget = RelativePath(value = "Books/Reading list.md"),
            onIntent = {}
        )
    }
}
