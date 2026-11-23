package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/** A note's properties as its frontmatter writes them, one `key: value` each, for the model to read. */
internal object NoteProperties {
    fun of(note: NoteEntry): List<String> = note
        .metadata
        .properties
        .map { property ->
            // An unread value's source already starts with its key: the model was shown "status: status: draft".
            val value = property.value
            if (value is PropertyValue.Other) value.source else "${property.key}: ${writtenOf(value = value)}"
        }

    private fun writtenOf(value: PropertyValue): String = when (value) {
        is PropertyValue.Text -> value.text
        is PropertyValue.Number -> value.text
        is PropertyValue.Checkbox -> value.isChecked.toString()
        is PropertyValue.Date -> value.date.iso
        is PropertyValue.Items -> value.items.joinToString(separator = ", ")
        is PropertyValue.Other -> value.source
    }
}
