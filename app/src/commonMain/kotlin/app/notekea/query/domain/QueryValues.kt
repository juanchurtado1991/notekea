package app.notekea.query.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/** A property's value as queries read it: as words to match, and in order against another value of its type. */
internal class QueryValues {
    private val momentFormat = MomentFormat()

    /** The value's words: one for a scalar, one per item for a list. */
    fun textsOf(value: PropertyValue): List<String> = when (value) {
        is PropertyValue.Text -> listOf(value.text)

        is PropertyValue.Number -> listOf(value.text)

        is PropertyValue.Checkbox -> listOf(value.isChecked.toString())

        is PropertyValue.Date -> listOf(
            momentFormat.format(LocalMoment(date = value.date), MomentFormat.ISO_DATE)
        )

        is PropertyValue.Items -> value.items

        is PropertyValue.Other -> listOf(value.source)
    }

    // YAML writes "nothing" as `null` or `~` too, and the frontmatter reads those as text: they would pass "is"
    // and sort among the values.
    fun isEmpty(value: PropertyValue?): Boolean =
        value == null || textsOf(value = value).all { isNothing(text = it) }

    /** Whether [value] reads as a day: a date, or text that starts with one (`2026-01-05T10:00`). */
    fun isDate(value: PropertyValue): Boolean = when (value) {
        is PropertyValue.Date -> true
        is PropertyValue.Text -> dateOf(text = value.text) != null
        else -> false
    }

    /** How [value] compares with [other], written by a person; null when they are not numbers or dates. */
    fun compare(
        value: PropertyValue,
        other: String
    ): Int? = when (value) {
        is PropertyValue.Number -> other.toDoubleOrNull()?.let {
            value
                .text
                .toDoubleOrNull()
                ?.compareTo(other = it)
        }

        is PropertyValue.Date -> dateOf(text = other)?.let { value.date.compareTo(other = it) }

        // A date with a time is read as text, so its day is what "before", "after" and "is" compare.
        is PropertyValue.Text -> dateOf(text = value.text)?.let { day ->
            dateOf(text = other)?.let { day.compareTo(other = it) }
        }

        else -> null
    }

    private fun isNothing(text: String): Boolean {
        val trimmed = text.trim()

        return trimmed.isEmpty() || trimmed.lowercase() in NOTHING
    }

    /** The day [text] names, alone or followed by its time (`T10:00`, ` 10:00`); null when it names none. */
    private fun dateOf(text: String): CalendarDate? {
        val trimmed = text.trim()
        val day = if (TIMED.matches(input = trimmed)) trimmed.take(n = ISO_DATE_LENGTH) else trimmed

        return momentFormat.parseDate(
            text = day,
            pattern = MomentFormat.ISO_DATE
        )
    }

    private companion object {
        const val ISO_DATE_LENGTH = 10
        val TIMED = Regex(pattern = "\\d{4}-\\d{2}-\\d{2}[Tt ]\\d{2}:\\d{2}.*")
        val NOTHING = setOf("null", "~")
    }
}
