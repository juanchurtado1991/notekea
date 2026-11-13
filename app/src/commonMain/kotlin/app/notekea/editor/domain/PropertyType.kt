package app.notekea.editor.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/** The types a new property can have, each with the empty value it starts with. */
enum class PropertyType {
    TEXT,
    LIST,
    NUMBER,
    CHECKBOX,
    DATE;

    /** The value a new property of this type starts with; a date starts on [today]. */
    fun emptyValue(today: CalendarDate): PropertyValue = when (this) {
        TEXT -> PropertyValue.Text(text = "")
        LIST -> PropertyValue.Items(items = emptyList())
        NUMBER -> PropertyValue.Number(text = ZERO)
        CHECKBOX -> PropertyValue.Checkbox(isChecked = false)
        DATE -> PropertyValue.Date(date = today)
    }

    private companion object {
        const val ZERO = "0"
    }
}
