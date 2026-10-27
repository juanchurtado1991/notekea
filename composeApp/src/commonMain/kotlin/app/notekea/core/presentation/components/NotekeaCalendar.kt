package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.calendar_months
import app.notekea.resources.calendar_next_month
import app.notekea.resources.calendar_previous_month
import app.notekea.resources.calendar_today
import app.notekea.resources.calendar_weekdays
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

private const val DAY_CELL_DP = 32
private const val DAY_INSET_DP = 2
private const val MONTH_YEAR_SEPARATOR = " "
private val PREVIEW_SELECTED = CalendarDate(year = 2026, month = 10, day = 14)
private val PREVIEW_TODAY = CalendarDate(year = 2026, month = 10, day = 27)

/**
 * A compact month calendar for picking a day, weeks starting on Monday: the month and year with arrows that move it,
 * the weekday initials, then the days, [selected] filled with the primary color and [today] ringed. A click on a day
 * goes to [onPick]; "Today", shown when [today] is known, picks it. It opens on [selected]'s month.
 */
@Composable
fun NotekeaCalendar(
    selected: CalendarDate,
    onPick: (CalendarDate) -> Unit,
    today: CalendarDate?,
    modifier: Modifier = Modifier
) {
    var month by remember(selected) { mutableStateOf(value = selected.copy(day = 1)) }
    Column(
        modifier = modifier
            .width(width = (DAY_CELL_DP * CalendarDate.DAYS_IN_WEEK).dp + NotekeaDimensions.SpacingM * 2)
            .padding(all = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        MonthHeader(month = month, onMove = { month = it })
        WeekdayRow()
        weeksOf(month = month).forEach { week ->
            Row {
                week.forEach { date ->
                    DayCell(date = date, isSelected = date == selected, isToday = date == today, onPick = onPick)
                }
            }
        }
        today?.let { TodayFooter(onPick = { onPick(it) }) }
    }
}

/** The month and year, with the arrows that show the previous and the next month. */
@Composable
private fun MonthHeader(
    month: CalendarDate,
    onMove: (CalendarDate) -> Unit
) {
    val name = stringArrayResource(resource = Res.array.calendar_months)[month.month - 1]
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name + MONTH_YEAR_SEPARATOR + month.year,
            modifier = Modifier.weight(weight = 1f).padding(start = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        NotekeaIconButton(
            icon = NotekeaIcons.PreviousMonth,
            label = stringResource(resource = Res.string.calendar_previous_month),
            onClick = { onMove(shifted(month = month, by = -1)) },
            takesFocus = false
        )
        NotekeaIconButton(
            icon = NotekeaIcons.NextMonth,
            label = stringResource(resource = Res.string.calendar_next_month),
            onClick = { onMove(shifted(month = month, by = 1)) },
            takesFocus = false
        )
    }
}

@Composable
private fun WeekdayRow() {
    Row {
        stringArrayResource(resource = Res.array.calendar_weekdays).forEach { initials ->
            Box(modifier = Modifier.size(size = DAY_CELL_DP.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** One day: a circle filled while it is the [isSelected] day and ringed while it is today; null is an empty cell. */
@Composable
private fun DayCell(
    isSelected: Boolean,
    isToday: Boolean,
    onPick: (CalendarDate) -> Unit,
    date: CalendarDate?
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(size = DAY_CELL_DP.dp).padding(all = DAY_INSET_DP.dp)) {
        if (date == null) return@Box
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape = CircleShape)
                .background(color = if (isSelected) colors.primary else Color.Transparent)
                .border(
                    width = NotekeaDimensions.BorderWidth,
                    color = if (isToday && !isSelected) colors.primary else Color.Transparent,
                    shape = CircleShape
                )
                .focusProperties { canFocus = false }
                .clickable(role = Role.Button) { onPick(date) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = when {
                    isSelected -> colors.onPrimary
                    isToday -> colors.primary
                    else -> colors.onSurface
                }
            )
        }
    }
}

/** A hairline, then "Today", which picks today. */
@Composable
private fun TodayFooter(
    onPick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = NotekeaDimensions.SpacingXs)
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outline)
    )
    NotekeaButton(
        text = stringResource(resource = Res.string.calendar_today),
        onClick = onPick,
        style = NotekeaButtonStyle.TEXT
    )
}

/** The days of [month]'s weeks, Monday to Sunday, with null before its first day and after its last. */
private fun weeksOf(
    month: CalendarDate
): List<List<CalendarDate?>> {
    val leading = month.dayOfWeek - 1
    val days = List(size = leading) { null } +
        (1..CalendarDate.lengthOfMonth(year = month.year, month = month.month)).map { month.copy(day = it) }
    return days.chunked(size = CalendarDate.DAYS_IN_WEEK).map { week ->
        week + List(size = CalendarDate.DAYS_IN_WEEK - week.size) { null }
    }
}

/** The first day of the month [by] months after [month]. */
private fun shifted(
    month: CalendarDate,
    by: Int
): CalendarDate {
    val index = month.year * CalendarDate.MONTHS + month.month - 1 + by
    return CalendarDate(
        year = index.floorDiv(other = CalendarDate.MONTHS),
        month = index.mod(other = CalendarDate.MONTHS) + 1,
        day = 1
    )
}

@Preview
@Composable
private fun NotekeaCalendarPreview() {
    NotekeaPreviewThemes {
        NotekeaCalendar(selected = PREVIEW_SELECTED, onPick = {}, today = PREVIEW_TODAY)
    }
}
