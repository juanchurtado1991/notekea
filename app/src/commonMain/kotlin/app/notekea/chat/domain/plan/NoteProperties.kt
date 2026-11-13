package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/** A note's properties as its frontmatter writes them, one `key: value` each, for the model to read. */
internal object NoteProperties {
    fun of(note: NoteEntry): List<String> = note
        .metadata
        .properties
        .map { "${it.key}: ${writtenOf(value = it.value)}" }

    private fun writtenOf(value: PropertyValue): String = when (value) {
        is PropertyValue.Text -> value.text
        is PropertyValue.Number -> value.text
        is PropertyValue.Checkbox -> value.isChecked.toString()
        is PropertyValue.Date -> with(value.date) { "$year-${month.padded()}-${day.padded()}" }
        is PropertyValue.Items -> value.items.joinToString(separator = ", ")
        is PropertyValue.Other -> value.source
    }

    private fun Int.padded(): String = toString().padStart(
        length = 2,
        padChar = '0'
    )
}
