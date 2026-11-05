package app.notekea.query.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaCompactTabs
import app.notekea.core.presentation.components.NotekeaPopover
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryValueKind
import app.notekea.resources.Res
import app.notekea.resources.query_apply
import app.notekea.resources.query_invalid_value
import app.notekea.resources.query_no
import app.notekea.resources.query_value
import app.notekea.resources.query_yes
import org.jetbrains.compose.resources.stringResource

private const val FALSE = "false"
private const val TRUE = "true"

/**
 * A card to choose how a property is tested: the conditions its [kind] allows, and a value of that kind, yes or no for
 * a checkbox. Enter or Apply give [onApply] the filter; a value that does not fit the kind says so. Esc closes it.
 */
@Composable
internal fun QueryFilterEditor(
    filter: QueryFilter,
    kind: QueryValueKind,
    onApply: (QueryFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val first = filter.condition.takeIf { it in kind.conditions } ?: kind.conditions.first()
    var condition by remember { mutableStateOf(value = first) }
    var value by remember { mutableStateOf(value = filter.value) }
    var isChecked by remember { mutableStateOf(value = filter.value != FALSE) }
    var isRejected by remember { mutableStateOf(value = false) }
    val apply = {
        val written = if (kind == QueryValueKind.CHECKBOX) isChecked.toString() else value.trim()
        val applied = filterOf(filter = filter, condition = condition, text = written, kind = kind)
        isRejected = applied == null
        applied?.let(onApply) ?: Unit
    }
    NotekeaPopover(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            Text(text = filter.property, style = MaterialTheme.typography.titleSmall)
            ConditionTabs(kind = kind, condition = condition, onSelect = { condition = it })
            if (condition.needsValue) {
                ValueInput(
                    kind = kind,
                    text = value,
                    onTextChange = {
                        value = it
                        isRejected = false
                    },
                    onCheckedChange = { isChecked = it },
                    onSubmit = apply,
                    isChecked = isChecked,
                    error = stringResource(resource = Res.string.query_invalid_value).takeIf { isRejected }
                )
            }
            NotekeaButton(text = stringResource(resource = Res.string.query_apply), onClick = apply)
        }
    }
}

@Composable
private fun ConditionTabs(
    kind: QueryValueKind,
    condition: QueryCondition,
    onSelect: (QueryCondition) -> Unit
) {
    NotekeaCompactTabs(
        labels = kind.conditions.map { conditionLabel(condition = it) },
        selectedIndex = kind.conditions.indexOf(element = condition).coerceAtLeast(minimumValue = 0),
        onSelect = { onSelect(kind.conditions[it]) }
    )
}

@Composable
private fun ValueInput(
    kind: QueryValueKind,
    text: String,
    onTextChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    isChecked: Boolean,
    error: String?
) {
    if (kind == QueryValueKind.CHECKBOX) {
        NotekeaCompactTabs(
            labels = listOf(
                stringResource(resource = Res.string.query_yes),
                stringResource(resource = Res.string.query_no)
            ),
            selectedIndex = if (isChecked) 0 else 1,
            onSelect = { onCheckedChange(it == 0) }
        )
    } else {
        val focus = remember { FocusRequester() }
        NotekeaTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.focusRequester(focusRequester = focus),
            placeholder = stringResource(resource = Res.string.query_value),
            errorText = error,
            onSubmit = onSubmit
        )
        LaunchedEffect(focus) { focus.requestFocus() }
    }
}

/** [filter] with [condition] and [text], or null when [text] does not fit [kind]. */
private fun filterOf(
    filter: QueryFilter,
    condition: QueryCondition,
    text: String,
    kind: QueryValueKind
): QueryFilter? = when {
    !condition.needsValue -> filter.copy(condition = condition, value = "")
    isValidValue(text = text, kind = kind) -> filter.copy(condition = condition, value = text)
    else -> null
}

private fun isValidValue(
    text: String,
    kind: QueryValueKind
): Boolean = text.isNotEmpty() && when (kind) {
    QueryValueKind.NUMBER -> text.toDoubleOrNull() != null
    QueryValueKind.DATE -> MomentFormat().parseDate(text = text, pattern = MomentFormat.ISO_DATE) != null
    QueryValueKind.CHECKBOX -> text == TRUE || text == FALSE
    QueryValueKind.TEXT -> true
}

@Preview
@Composable
private fun QueryFilterEditorPreview() {
    NotekeaPreviewThemes {
        QueryFilterEditor(
            filter = QueryPreviewData.ratingFilter,
            kind = QueryValueKind.NUMBER,
            onApply = {},
            onDismiss = {}
        )
    }
}
