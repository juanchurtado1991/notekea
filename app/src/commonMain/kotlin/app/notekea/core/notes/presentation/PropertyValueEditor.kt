package app.notekea.core.notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyKeys
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.presentation.designsystem.components.NotekeaChip
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.fields.NotekeaDateField
import app.notekea.core.presentation.designsystem.components.fields.NotekeaPropertyField
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.readableDate
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
 * A property's value edited in place, by its type: text and numbers as fields, a checkbox, a date typed or picked on a
 * calendar that marks [today], a list as chips with a field of [itemFieldWidth] that adds one, and a value the form
 * does not edit as it is written. Each change goes to [change] at once.
 */
@Composable
fun PropertyValueEditor(
    property: NoteProperty,
    change: (PropertyValue) -> Unit,
    today: CalendarDate?,
    itemFieldWidth: Dp = ITEM_FIELD_WIDTH_DP.dp
) {
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
            label = property.key,
            modifier = Modifier.padding(start = NotekeaDimensions.SpacingXs)
        )

        is PropertyValue.Date -> DateEditor(
            value = value,
            today = today,
            change = change
        )

        is PropertyValue.Items -> ItemsEditor(property, value.items, itemFieldWidth, change)

        is PropertyValue.Other -> OtherValue(source = value.source)
    }
}

@Composable
private fun DateEditor(
    value: PropertyValue.Date,
    today: CalendarDate?,
    change: (PropertyValue) -> Unit
) {
    val format = remember { MomentFormat() }

    NotekeaDateField(
        date = value.date,
        text = format.format(
            moment = LocalMoment(date = value.date),
            pattern = MomentFormat.ISO_DATE
        ),
        onTextCommit = { text ->
            format.parseDate(
                text = text,
                pattern = MomentFormat.ISO_DATE
            )?.let { change(PropertyValue.Date(date = it)) }
        },
        onDatePick = { change(PropertyValue.Date(date = it)) },
        calendarLabel = stringResource(resource = Res.string.properties_calendar),
        displayText = readableDate(date = value.date),
        today = today,
        isValid = {
            format.parseDate(
                text = it,
                pattern = MomentFormat.ISO_DATE
            ) != null
        }
    )
}

/** A list as chips, each with its ✕, and a field that adds what is typed (tags without their `#`). */
@Composable
private fun ItemsEditor(
    property: NoteProperty,
    items: List<String>,
    fieldWidth: Dp,
    change: (PropertyValue) -> Unit
) {
    val isTags = PropertyKeys.isTags(key = property.key)
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
                removeLabel = removeLabel,
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        fun addItem(text: String) {
            val item = (if (isTags) text.removePrefix(prefix = TAG_MARK) else text).trim()

            if (item.isNotEmpty() && item !in items) {
                change(PropertyValue.Items(items = items + item))
            }
        }

        // A new field after each change, so what was typed does not stay once it is added.
        key(items) {
            Box(modifier = Modifier.width(width = fieldWidth)) {
                NotekeaPropertyField(
                    value = "",
                    onCommit = { addItem(it) },
                    placeholder = stringResource(resource = Res.string.properties_add_item)
                )
            }
        }
    }
}

/** A nested or multi-line value, as written, with a note that it is edited in the source. */
@Composable
private fun OtherValue(source: String) {
    Text(
        text = source
            .substringAfter(delimiter = SOURCE_KEY_SEPARATOR)
            .trim()
            .ifEmpty { stringResource(resource = Res.string.properties_other) },
        modifier = Modifier.padding(start = NotekeaDimensions.SpacingXs),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = OTHER_MAX_LINES,
        overflow = TextOverflow.Ellipsis
    )
}

private const val SOURCE_KEY_SEPARATOR = ":"
private const val PREVIEW_KEY = "tags"
private val PREVIEW_ITEMS = listOf("recipe", "breakfast")

@Preview
@Composable
private fun PropertyValueEditorPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            PropertyValueEditor(
                property = NoteProperty(
                    key = PREVIEW_KEY,
                    value = PropertyValue.Items(items = PREVIEW_ITEMS)
                ),
                change = {},
                today = null
            )
        }
    }
}
