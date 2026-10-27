package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.presentation.components.NotekeaCheckbox
import app.notekea.core.presentation.components.NotekeaChip
import app.notekea.core.presentation.components.NotekeaDateField
import app.notekea.core.presentation.components.NotekeaPropertyField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.NoteProperty
import app.notekea.notes.domain.PropertyValue
import app.notekea.resources.Res
import app.notekea.resources.properties_add_item
import app.notekea.resources.properties_calendar
import app.notekea.resources.properties_empty
import app.notekea.resources.properties_other
import app.notekea.resources.properties_remove_item
import org.jetbrains.compose.resources.stringResource

private const val ITEM_FIELD_WIDTH_DP = 120
private const val OTHER_MAX_LINES = 2
private const val PREVIEW_WIDTH_DP = 420
private const val TAG_MARK = "#"
private val NUMBER = Regex(pattern = "-?\\d+(\\.\\d+)?")

/**
 * A property's value edited in place, by its type (`ROADMAP.md` 1.14): text and numbers as fields, a checkbox, a date
 * typed or picked on a calendar, a list as chips with a field that adds one, and a value the form does not edit as it
 * is written, with a note to edit it in the source. Each change goes to the note at once.
 */
@Composable
internal fun PropertyValueEditor(
    property: NoteProperty,
    onIntent: (EditorIntent) -> Unit
) {
    val change = { value: PropertyValue -> onIntent(PropertyIntent.OnValueChange(key = property.key, value = value)) }
    when (val value = property.value) {
        is PropertyValue.Text -> NotekeaPropertyField(
            value = value.text,
            onCommit = { change(PropertyValue.Text(text = it)) },
            placeholder = stringResource(resource = Res.string.properties_empty)
        )

        is PropertyValue.Number -> NotekeaPropertyField(
            value = value.text,
            onCommit = { change(PropertyValue.Number(text = it)) },
            isValid = { NUMBER.matches(input = it) }
        )

        is PropertyValue.Checkbox -> NotekeaCheckbox(
            checked = value.isChecked,
            onClick = { change(PropertyValue.Checkbox(isChecked = !value.isChecked)) },
            label = property.key
        )

        is PropertyValue.Date -> DateEditor(value = value, change = change)

        is PropertyValue.Items -> ItemsEditor(property = property, items = value.items, change = change)

        is PropertyValue.Other -> OtherValue(source = value.source)
    }
}

@Composable
private fun DateEditor(
    value: PropertyValue.Date,
    change: (PropertyValue) -> Unit
) {
    val format = remember { MomentFormat() }
    NotekeaDateField(
        date = value.date,
        text = format.format(moment = LocalMoment(date = value.date), pattern = MomentFormat.ISO_DATE),
        onTextCommit = { text ->
            format.parseDate(text = text, pattern = MomentFormat.ISO_DATE)?.let {
                change(PropertyValue.Date(date = it))
            }
        },
        onDatePick = { change(PropertyValue.Date(date = it)) },
        calendarLabel = stringResource(resource = Res.string.properties_calendar),
        isValid = { format.parseDate(text = it, pattern = MomentFormat.ISO_DATE) != null }
    )
}

/** A list as chips, each with its ✕, and a field that adds what is typed (tags without their `#`). */
@Composable
private fun ItemsEditor(
    property: NoteProperty,
    items: List<String>,
    change: (PropertyValue) -> Unit
) {
    val isTags = property.key.lowercase() == TAGS_KEY
    val removeLabel = stringResource(resource = Res.string.properties_remove_item)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            NotekeaChip(
                label = if (isTags) TAG_MARK + item else item,
                onRemove = { change(PropertyValue.Items(items = items - item)) },
                removeLabel = removeLabel
            )
        }
        // A new field after each change, so what was typed does not stay once it is added.
        key(items) {
            Box(modifier = Modifier.width(width = ITEM_FIELD_WIDTH_DP.dp)) {
                NotekeaPropertyField(
                    value = "",
                    onCommit = { text ->
                        val item = (if (isTags) text.removePrefix(prefix = TAG_MARK) else text).trim()
                        if (item.isNotEmpty() && item !in items) change(PropertyValue.Items(items = items + item))
                    },
                    placeholder = stringResource(resource = Res.string.properties_add_item)
                )
            }
        }
    }
}

/** A nested or multi-line value, as written, with a note that it is edited in the source. */
@Composable
private fun OtherValue(
    source: String
) {
    Text(
        text = source.substringAfter(delimiter = SOURCE_KEY_SEPARATOR).trim()
            .ifEmpty { stringResource(resource = Res.string.properties_other) },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = OTHER_MAX_LINES,
        overflow = TextOverflow.Ellipsis
    )
}

private const val SOURCE_KEY_SEPARATOR = ":"
private const val TAGS_KEY = "tags"

@Preview
@Composable
private fun PropertyValueEditorPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            PropertyValueEditor(property = EditorPreviewData.typedProperties.properties.first(), onIntent = {})
        }
    }
}
