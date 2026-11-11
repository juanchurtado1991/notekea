package app.notekea.core.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.date_long
import app.notekea.resources.date_short
import app.notekea.resources.date_with_time
import app.notekea.resources.months_short
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/*
 * Dates as people read them, in the interface's language (5.2): "Nov 11, 2026" or "11 nov 2026". File names and
 * Obsidian's formats keep MomentFormat, whose month names are Obsidian's English ones.
 */

private val PREVIEW_DATE = CalendarDate(
    year = 2026,
    month = 11,
    day = 11
)

/** [date] with its year, or without it when [withYear] is false: a task's due date this year. */
@Composable
fun readableDate(
    date: CalendarDate,
    withYear: Boolean = true
): String {
    val month = stringArrayResource(resource = Res.array.months_short)[date.month - 1]

    return if (withYear) {
        stringResource(Res.string.date_long, date.day, month, date.year)
    } else {
        stringResource(Res.string.date_short, date.day, month)
    }
}

/** [moment]'s date and its time on the 24-hour clock: "Nov 11, 2026, 15:58". */
@Composable
fun readableMoment(moment: LocalMoment): String = stringResource(
    Res.string.date_with_time,
    readableDate(date = moment.date),
    "${twoDigits(moment.hour)}:${twoDigits(moment.minute)}"
)

private fun twoDigits(value: Int): String = value.toString().padStart(
    length = 2,
    padChar = '0'
)

@Preview
@Composable
private fun readableDatePreview() {
    NotekeaPreviewThemes { Text(text = readableDate(date = PREVIEW_DATE)) }
}

@Preview
@Composable
private fun readableMomentPreview() {
    NotekeaPreviewThemes {
        Text(
            text = readableMoment(
                moment = LocalMoment(
                    date = PREVIEW_DATE,
                    hour = 15,
                    minute = 58
                )
            )
        )
    }
}
