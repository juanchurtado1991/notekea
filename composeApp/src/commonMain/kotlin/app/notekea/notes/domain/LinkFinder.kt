package app.notekea.notes.domain

/** Finds the links to notes in Markdown text, in order, ignoring anything inside code. */
interface LinkFinder {
    fun find(
        markdown: String
    ): List<NoteLink>
}
