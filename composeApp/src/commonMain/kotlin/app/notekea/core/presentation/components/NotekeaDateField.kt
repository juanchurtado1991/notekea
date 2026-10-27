package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val FIELD_WIDTH_DP = 132
private const val PREVIEW_CALENDAR = "Choose a date"
private const val PREVIEW_DISPLAY = "Oct 27, 2026"
private const val PREVIEW_WIDTH_DP = 260
private val PREVIEW_DATE = CalendarDate(year = 2026, month = 10, day = 27)

/**
 * A date edited in place: its [text] typed as in a [NotekeaPropertyField] (handed to [onTextCommit], which reads it)
 * and shown as [displayText] until the field is focused, then a calendar button, named [calendarLabel], that opens a
 * [NotekeaCalendar] under the date on [date], marking [today]; a day picked there goes to [onDatePick] and closes it.
 */
@Composable
fun NotekeaDateField(
    date: CalendarDate,
    text: String,
    onTextCommit: (String) -> Unit,
    onDatePick: (CalendarDate) -> Unit,
    calendarLabel: String,
    modifier: Modifier = Modifier,
    displayText: String? = null,
    today: CalendarDate? = null,
    isValid: (String) -> Boolean = { true }
) {
    var isOpen by remember { mutableStateOf(value = false) }
    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(width = FIELD_WIDTH_DP.dp)) {
                NotekeaPropertyField(
                    value = text,
                    onCommit = onTextCommit,
                    displayValue = displayText,
                    isValid = isValid
                )
            }
            NotekeaIconButton(
                icon = NotekeaIcons.Calendar,
                label = calendarLabel,
                onClick = { isOpen = !isOpen },
                takesFocus = false
            )
        }
        if (isOpen) {
            CalendarPopup(
                date = date,
                today = today,
                onPick = {
                    isOpen = false
                    onDatePick(it)
                },
                onDismiss = { isOpen = false }
            )
        }
    }
}

/** The calendar in a card under the date, on [date]; a click on a day picks it, a click outside or Esc closes it. */
@Composable
private fun CalendarPopup(
    date: CalendarDate,
    onPick: (CalendarDate) -> Unit,
    onDismiss: () -> Unit,
    today: CalendarDate?
) {
    val gap = with(LocalDensity.current) { NotekeaDimensions.SpacingXs.roundToPx() }
    Popup(
        popupPositionProvider = remember(gap) { DropdownPositionProvider(gap = gap) },
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = MaterialTheme.colorScheme.outline),
            shadowElevation = NotekeaDimensions.SpacingS
        ) {
            NotekeaCalendar(selected = date, onPick = onPick, today = today)
        }
    }
}

@Preview
@Composable
private fun NotekeaDateFieldPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaDateField(
                date = PREVIEW_DATE,
                text = PREVIEW_DATE.toString(),
                onTextCommit = {},
                onDatePick = {},
                calendarLabel = PREVIEW_CALENDAR,
                displayText = PREVIEW_DISPLAY
            )
        }
    }
}
