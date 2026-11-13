package app.notekea.core.notes.domain.links

/** Finds the links to notes in Markdown text, in order, ignoring anything inside code. */
interface LinkFinder {
    fun find(markdown: String): List<NoteLink>
}
