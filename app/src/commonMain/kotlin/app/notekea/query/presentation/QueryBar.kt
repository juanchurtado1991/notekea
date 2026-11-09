package app.notekea.query.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.NotekeaToolButton
import app.notekea.core.presentation.designsystem.components.NotekeaToolMenu
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.query.domain.QueryCatalog
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.VaultQuery
import app.notekea.resources.Res
import app.notekea.resources.query_add_filter
import app.notekea.resources.query_columns
import app.notekea.resources.query_matching
import app.notekea.resources.query_save_in
import app.notekea.resources.query_sort
import app.notekea.resources.query_sort_ascending
import app.notekea.resources.query_sort_descending
import app.notekea.resources.query_sort_name
import org.jetbrains.compose.resources.stringResource

/**
 * The query above its table (`ROADMAP.md` 2.10, design A): a toolbar with Sort, Columns and, with a note at hand,
 * saving it in [saveTarget], over its filters as [QueryRules]. With no filter, Add filter starts the first.
 */
@Composable
internal fun QueryBar(
    state: QueryState,
    saveTarget: RelativePath?,
    onIntent: (QueryIntent) -> Unit
) {
    var isAdding by remember { mutableStateOf(value = false) }
    val hasRules = state.query.filters.isNotEmpty() || isAdding
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = NotekeaDimensions.SpacingXs)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingXs
            ),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RulesTitle(hasRules = hasRules, onAdd = { isAdding = true })
            Spacer(modifier = Modifier.weight(weight = 1f))
            SortMenu(query = state.query, catalog = state.catalog, onIntent = onIntent)
            ColumnsMenu(query = state.query, catalog = state.catalog, onIntent = onIntent)
            saveTarget?.let { SaveButton(note = it, onIntent = onIntent) }
        }
        QueryRules(
            filters = state.query.filters,
            catalog = state.catalog,
            isAdding = isAdding,
            onAddingChange = { isAdding = it },
            onIntent = onIntent
        )
    }
}

/** What the rules below do or, with none, Add filter. */
@Composable
private fun RulesTitle(
    hasRules: Boolean,
    onAdd: () -> Unit
) {
    if (hasRules) {
        Text(
            text = stringResource(resource = Res.string.query_matching),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        NotekeaToolButton(
            icon = NotekeaIcons.AddRow,
            text = stringResource(resource = Res.string.query_add_filter),
            onClick = onAdd,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SaveButton(
    note: RelativePath,
    onIntent: (QueryIntent) -> Unit
) {
    NotekeaToolButton(
        icon = NotekeaIcons.QuerySave,
        text = stringResource(resource = Res.string.query_save_in, note.nameWithoutExtension),
        onClick = { onIntent(QueryIntent.OnSave(path = note)) },
        tint = MaterialTheme.colorScheme.primary
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
    NotekeaToolMenu(
        icon = NotekeaIcons.QuerySort,
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
    NotekeaToolMenu(
        icon = NotekeaIcons.QueryColumns,
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
