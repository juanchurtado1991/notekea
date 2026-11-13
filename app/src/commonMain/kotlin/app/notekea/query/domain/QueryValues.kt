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

    fun isEmpty(value: PropertyValue?): Boolean =
        value == null || textsOf(value = value).all { it.isBlank() }

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

        else -> null
    }

    private fun dateOf(text: String): CalendarDate? = momentFormat.parseDate(
        text = text.trim(),
        pattern = MomentFormat.ISO_DATE
    )
}
