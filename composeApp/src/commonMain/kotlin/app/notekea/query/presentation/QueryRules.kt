package app.notekea.query.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.presentation.components.NotekeaFieldFrame
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaPropertyField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.query.domain.QueryCatalog
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryValueKind
import app.notekea.resources.Res
import app.notekea.resources.query_add_filter
import app.notekea.resources.query_choose
import app.notekea.resources.query_filter_folder
import app.notekea.resources.query_filter_tag
import app.notekea.resources.query_no
import app.notekea.resources.query_remove_filter
import app.notekea.resources.query_value
import app.notekea.resources.query_yes
import org.jetbrains.compose.resources.stringResource

private const val FALSE = "false"
private const val POPUP_WIDTH_DP = 120
private const val TRUE = "true"
private val NEW_RULE = QueryFilter(condition = QueryCondition.IN_FOLDER)
private val TAG_RULE = QueryFilter(condition = QueryCondition.HAS_TAG)

/**
 * The query's filters as rules, one a row, as a Finder smart folder writes them (`ROADMAP.md` 2.10, design A). A rule
 * is edited in place and reaches the query only once it is whole, so the table never runs on half a rule; [isAdding]
 * shows a new one after the last. A value that does not fit its property is outlined in red and not taken.
 */
@Composable
internal fun QueryRules(
    filters: List<QueryFilter>,
    catalog: QueryCatalog,
    isAdding: Boolean,
    onAddingChange: (Boolean) -> Unit,
    onIntent: (QueryIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        filters.forEachIndexed { index, filter ->
            QueryRule(
                catalog = catalog,
                onWhole = { onIntent(QueryIntent.OnFilterChange(index = index, filter = it)) },
                onRemove = { onIntent(QueryIntent.OnFilterRemove(index = index)) },
                filter = filter,
                onAdd = { onAddingChange(true) }.takeIf { index == filters.lastIndex && !isAdding }
            )
        }
        if (isAdding) {
            QueryRule(
                catalog = catalog,
                onWhole = {
                    onAddingChange(false)
                    onIntent(QueryIntent.OnFilterAdd(filter = it))
                },
                onRemove = { onAddingChange(false) },
                filter = null,
                onAdd = null
            )
        }
    }
}

/** A rule: what it looks at, how, and against what, with [onAdd] after the last one. A new rule has no [filter]. */
@Composable
private fun QueryRule(
    catalog: QueryCatalog,
    onWhole: (QueryFilter) -> Unit,
    onRemove: () -> Unit,
    filter: QueryFilter?,
    onAdd: (() -> Unit)?
) {
    var draft by remember(filter) { mutableStateOf(value = filter ?: newRule(catalog = catalog)) }
    val kind = kindOf(filter = draft, catalog = catalog)
    val change = { changed: QueryFilter ->
        draft = changed
        val isReady = isWhole(filter = changed, kind = kindOf(filter = changed, catalog = catalog))
        if (changed != filter && isReady) onWhole(changed)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = NotekeaDimensions.SpacingM,
            vertical = NotekeaDimensions.SpacingXs / 2
        ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QueryPopup(
            text = fieldLabel(filter = draft),
            sections = fieldSections(catalog = catalog, draft = draft, onChange = change),
            modifier = Modifier.width(width = POPUP_WIDTH_DP.dp)
        )
        QueryPopup(
            text = conditionLabel(condition = draft.condition),
            sections = listOf(conditionItems(draft = draft, kind = kind, onChange = change)),
            modifier = Modifier.width(width = POPUP_WIDTH_DP.dp)
        )
        Box(modifier = Modifier.weight(weight = 1f)) {
            RuleValue(draft = draft, kind = kind, catalog = catalog, onChange = change)
        }
        RuleActions(onRemove = onRemove, onAdd = onAdd)
    }
}

/** The − of a rule and, after the last one, the + that adds another. */
@Composable
private fun RuleActions(
    onRemove: () -> Unit,
    onAdd: (() -> Unit)?
) {
    NotekeaIconButton(
        icon = NotekeaIcons.RemoveRow,
        label = stringResource(resource = Res.string.query_remove_filter),
        onClick = onRemove
    )
    if (onAdd == null) {
        Spacer(modifier = Modifier.size(size = NotekeaDimensions.CompactControlSize))
    } else {
        NotekeaIconButton(
            icon = NotekeaIcons.AddRow,
            label = stringResource(resource = Res.string.query_add_filter),
            onClick = onAdd
        )
    }
}

@Composable
private fun fieldLabel(
    filter: QueryFilter
): String = when (filter.condition) {
    QueryCondition.IN_FOLDER -> stringResource(resource = Res.string.query_filter_folder)
    QueryCondition.HAS_TAG -> stringResource(resource = Res.string.query_filter_tag)
    else -> filter.property
}

/** Folder and Tag, when the vault has any, then its properties; another one waits for its value. */
@Composable
private fun fieldSections(
    catalog: QueryCatalog,
    draft: QueryFilter,
    onChange: (QueryFilter) -> Unit
): List<List<NotekeaMenuItem>> {
    val isChosen = { field: QueryFilter ->
        if (field.condition.needsProperty) field.property == draft.property else field.condition == draft.condition
    }
    val choose = { field: QueryFilter -> if (!isChosen(field)) onChange(field) }
    val noteItems = listOfNotNull(
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.query_filter_folder),
            onClick = { choose(NEW_RULE) },
            isChecked = draft.condition == QueryCondition.IN_FOLDER
        ).takeIf { catalog.folders.isNotEmpty() },
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.query_filter_tag),
            onClick = { choose(TAG_RULE) },
            isChecked = draft.condition == QueryCondition.HAS_TAG
        ).takeIf { catalog.tags.isNotEmpty() }
    )
    val keyItems = catalog.keys.map { key ->
        NotekeaMenuItem(
            label = key.name,
            onClick = { choose(QueryFilter(condition = key.kind.conditions.first(), property = key.name)) },
            isChecked = draft.property == key.name
        )
    }
    return listOf(noteItems, keyItems).filter { it.isNotEmpty() }
}

/** What the rule's kind can do; a folder or a tag can only be one. */
@Composable
private fun conditionItems(
    draft: QueryFilter,
    kind: QueryValueKind,
    onChange: (QueryFilter) -> Unit
): List<NotekeaMenuItem> {
    val conditions = if (draft.condition.needsProperty) kind.conditions else listOf(draft.condition)
    return conditions.map { condition ->
        NotekeaMenuItem(
            label = conditionLabel(condition = condition),
            onClick = {
                val value = draft.value.takeIf { condition.needsValue }.orEmpty()
                onChange(draft.copy(condition = condition, value = value))
            },
            isChecked = condition == draft.condition
        )
    }
}

/** The rule's value: chosen from the vault's folders or tags, yes or no for a checkbox, or typed. */
@Composable
private fun RuleValue(
    draft: QueryFilter,
    kind: QueryValueKind,
    catalog: QueryCatalog,
    onChange: (QueryFilter) -> Unit
) {
    val choose = { value: String -> onChange(draft.copy(value = value)) }
    val choice = draft.value.ifEmpty { stringResource(resource = Res.string.query_choose) }
    when {
        !draft.condition.needsValue -> Unit
        draft.condition == QueryCondition.IN_FOLDER -> ChoiceValue(choice, catalog.folders, draft.value, choose)
        draft.condition == QueryCondition.HAS_TAG -> ChoiceValue(choice, catalog.tags, draft.value, choose)
        kind == QueryValueKind.CHECKBOX -> CheckboxValue(value = draft.value, onChoose = choose)
        else -> TypedValue(value = draft.value, kind = kind, onCommit = choose)
    }
}

@Composable
private fun ChoiceValue(
    text: String,
    values: List<String>,
    chosen: String,
    onChoose: (String) -> Unit
) {
    QueryPopup(
        text = text,
        sections = listOf(
            values.map { NotekeaMenuItem(label = it, onClick = { onChoose(it) }, isChecked = it == chosen) }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CheckboxValue(
    value: String,
    onChoose: (String) -> Unit
) {
    val yes = stringResource(resource = Res.string.query_yes)
    val no = stringResource(resource = Res.string.query_no)
    val text = when (value) {
        TRUE -> yes
        FALSE -> no
        else -> stringResource(resource = Res.string.query_choose)
    }
    QueryPopup(
        text = text,
        sections = listOf(
            listOf(
                NotekeaMenuItem(label = yes, onClick = { onChoose(TRUE) }, isChecked = value == TRUE),
                NotekeaMenuItem(label = no, onClick = { onChoose(FALSE) }, isChecked = value == FALSE)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TypedValue(
    value: String,
    kind: QueryValueKind,
    onCommit: (String) -> Unit
) {
    NotekeaFieldFrame {
        NotekeaPropertyField(
            value = value,
            onCommit = { onCommit(it.trim()) },
            placeholder = stringResource(resource = Res.string.query_value),
            isValid = { it.isBlank() || fits(text = it.trim(), kind = kind) }
        )
    }
}

/** Where a rule starts: on the first of Folder, Tag and the properties that the vault has any of. */
private fun newRule(
    catalog: QueryCatalog
): QueryFilter {
    val key = catalog.keys.firstOrNull()
    return when {
        catalog.folders.isNotEmpty() -> NEW_RULE
        catalog.tags.isNotEmpty() -> TAG_RULE
        key != null -> QueryFilter(condition = key.kind.conditions.first(), property = key.name)
        else -> NEW_RULE
    }
}

private fun kindOf(
    filter: QueryFilter,
    catalog: QueryCatalog
): QueryValueKind = catalog.keys.firstOrNull { it.name == filter.property }?.kind ?: QueryValueKind.TEXT

private fun isWhole(
    filter: QueryFilter,
    kind: QueryValueKind
): Boolean = !filter.condition.needsValue || (filter.value.isNotBlank() && fits(text = filter.value, kind = kind))

private fun fits(
    text: String,
    kind: QueryValueKind
): Boolean = when (kind) {
    QueryValueKind.NUMBER -> text.toDoubleOrNull() != null
    QueryValueKind.DATE -> MomentFormat().parseDate(text = text, pattern = MomentFormat.ISO_DATE) != null
    QueryValueKind.CHECKBOX -> text == TRUE || text == FALSE
    QueryValueKind.TEXT -> true
}

@Preview
@Composable
private fun QueryRulesPreview() {
    NotekeaPreviewThemes {
        val state = QueryPreviewData.state
        QueryRules(
            filters = state.query.filters,
            catalog = state.catalog,
            isAdding = false,
            onAddingChange = {},
            onIntent = {}
        )
    }
}
