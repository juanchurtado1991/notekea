package app.notekea.tree.data

import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue

/**
 * A property as two text columns of the note cache: its type and its value. A list joins its items with a character no
 * frontmatter item holds. A row that cannot be read as its type keeps its text: the cache is refreshed from the notes.
 */
internal class CachedPropertyCodec {
    private val momentFormat = MomentFormat()

    fun encode(
        value: PropertyValue
    ): Pair<String, String> = when (value) {
        is PropertyValue.Text -> TEXT to value.text
        is PropertyValue.Number -> NUMBER to value.text
        is PropertyValue.Checkbox -> CHECKBOX to value.isChecked.toString()
        is PropertyValue.Date -> DATE to momentFormat.format(LocalMoment(date = value.date), MomentFormat.ISO_DATE)
        is PropertyValue.Items -> ITEMS to value.items.joinToString(separator = ITEM_SEPARATOR)
        is PropertyValue.Other -> OTHER to value.source
    }

    fun decode(
        key: String,
        type: String,
        value: String
    ): NoteProperty = NoteProperty(key = key, value = valueOf(type = type, value = value))

    private fun valueOf(
        type: String,
        value: String
    ): PropertyValue = when (type) {
        NUMBER -> PropertyValue.Number(text = value)

        CHECKBOX -> PropertyValue.Checkbox(isChecked = value.toBoolean())

        DATE -> momentFormat.parseDate(text = value, pattern = MomentFormat.ISO_DATE)
            ?.let { PropertyValue.Date(date = it) } ?: PropertyValue.Text(text = value)

        ITEMS -> PropertyValue.Items(items = if (value.isEmpty()) emptyList() else value.split(ITEM_SEPARATOR))

        OTHER -> PropertyValue.Other(source = value)

        else -> PropertyValue.Text(text = value)
    }

    private companion object {
        const val CHECKBOX = "checkbox"
        const val DATE = "date"
        const val ITEMS = "items"
        const val ITEM_SEPARATOR = "\u001F"
        const val NUMBER = "number"
        const val OTHER = "other"
        const val TEXT = "text"
    }
}
