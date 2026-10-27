package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull

private const val MILLIS_PER_DAY = 86_400_000L
private const val PREVIEW_CALENDAR = "Choose a date"
private const val PREVIEW_WIDTH_DP = 260
private val PREVIEW_DATE = CalendarDate(year = 2026, month = 10, day = 27)

/**
 * A date edited in place: its [text] typed as in a [NotekeaPropertyField] (handed to [onTextCommit], which reads it),
 * and a calendar button, named [calendarLabel], that opens a month calendar below it on [date]; a day picked there
 * goes to [onDatePick] and closes it.
 */
@Composable
fun NotekeaDateField(
    date: CalendarDate,
    text: String,
    onTextCommit: (String) -> Unit,
    onDatePick: (CalendarDate) -> Unit,
    calendarLabel: String,
    modifier: Modifier = Modifier,
    isValid: (String) -> Boolean = { true }
) {
    var isOpen by remember { mutableStateOf(value = false) }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(weight = 1f)) {
            NotekeaPropertyField(value = text, onCommit = onTextCommit, isValid = isValid)
        }
        Box {
            NotekeaIconButton(
                icon = NotekeaIcons.Calendar,
                label = calendarLabel,
                onClick = { isOpen = !isOpen },
                takesFocus = false
            )
            if (isOpen) {
                CalendarPopup(
                    date = date,
                    onPick = {
                        isOpen = false
                        onDatePick(it)
                    },
                    onDismiss = { isOpen = false }
                )
            }
        }
    }
}

/** The month calendar under the button, on [date]; a click on a day picks it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarPopup(
    date: CalendarDate,
    onPick: (CalendarDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = date.epochDay * MILLIS_PER_DAY)
    LaunchedEffect(state) {
        snapshotFlow { state.selectedDateMillis }.drop(count = 1).filterNotNull().collect { millis ->
            onPick(CalendarDate.ofEpochDay(epochDay = millis / MILLIS_PER_DAY))
        }
    }
    Popup(alignment = Alignment.TopEnd, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = MaterialTheme.colorScheme.outline),
            shadowElevation = NotekeaDimensions.SpacingS
        ) {
            DatePicker(
                state = state,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
            )
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
                calendarLabel = PREVIEW_CALENDAR
            )
        }
    }
}
