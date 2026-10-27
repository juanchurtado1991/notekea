package app.notekea.notes.domain

import app.notekea.core.domain.time.CalendarDate

/** The value of one frontmatter property, by the type its text shows (`ROADMAP.md` 1.14). */
sealed interface PropertyValue {
    data class Text(val text: String) : PropertyValue

    /** A number, kept as written (`3`, `-1.5`). */
    data class Number(val text: String) : PropertyValue

    data class Checkbox(val isChecked: Boolean) : PropertyValue

    data class Date(val date: CalendarDate) : PropertyValue

    /** A list: block (`- a`) or inline (`[a, b]`); tags, aliases and CSS classes are always lists. */
    data class Items(val items: List<String>) : PropertyValue

    /** Anything the form does not edit (nested values, multi-line text): shown as written, edited in the source. */
    data class Other(val source: String) : PropertyValue
}
