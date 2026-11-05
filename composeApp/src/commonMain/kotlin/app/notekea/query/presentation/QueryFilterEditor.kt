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
    var condition by remember { mutableStateOf(value = filter.condition) }
    var value by remember { mutableStateOf(value = filter.value) }
    var isChecked by remember { mutableStateOf(value = filter.value != FALSE) }
    var isRejected by remember { mutableStateOf(value = false) }
    val written = if (kind == QueryValueKind.CHECKBOX) isChecked.toString() else value.trim()
    val apply = {
        val isValid = !condition.needsValue || isValidValue(text = written, kind = kind)
        isRejected = !isValid
        if (isValid) onApply(filter.copy(condition = condition, value = written.takeIf { condition.needsValue }.orEmpty()))
    }
    NotekeaPopover(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            Text(text = filter.property, style = MaterialTheme.typography.titleSmall)
            val labels = kind.conditions.map { conditionLabel(condition = it) }
            NotekeaCompactTabs(
                labels = labels,
                selectedIndex = kind.conditions.indexOf(element = condition).coerceAtLeast(minimumValue = 0),
                onSelect = { condition = kind.conditions[it] }
            )
            if (condition.needsValue) {
                ValueInput(
                    kind = kind,
                    text = value,
                    isChecked = isChecked,
                    error = stringResource(resource = Res.string.query_invalid_value).takeIf { isRejected },
                    onTextChange = {
                        value = it
                        isRejected = false
                    },
                    onCheckedChange = { isChecked = it },
                    onSubmit = apply
                )
            }
            NotekeaButton(text = stringResource(resource = Res.string.query_apply), onClick = apply)
        }
    }
}

@Composable
private fun ValueInput(
    kind: QueryValueKind,
    text: String,
    isChecked: Boolean,
    error: String?,
    onTextChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
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
