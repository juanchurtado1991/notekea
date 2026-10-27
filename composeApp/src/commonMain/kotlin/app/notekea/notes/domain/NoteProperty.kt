package app.notekea.notes.domain

/** One frontmatter property: its [key] as written and its [value]. */
data class NoteProperty(
    val key: String,
    val value: PropertyValue
)
