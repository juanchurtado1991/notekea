package app.notekea.notes.domain

/**
 * A note found by the quick switcher. [text] is what matched (the name, the alias, or the path without `.md`; the name
 * for a recent note) and [ranges] its matched characters, for highlighting.
 */
data class NoteSearchResult(
    val note: NoteEntry,
    val field: NoteMatchField,
    val text: String,
    val ranges: List<IntRange>
)
