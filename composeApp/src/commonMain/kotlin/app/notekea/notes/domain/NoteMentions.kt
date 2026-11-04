package app.notekea.notes.domain

/**
 * The notes that name the open one (`ROADMAP.md` 2.5): those that link to it ([linked]), and those that only write its
 * name or an alias as plain text ([unlinked]), one entry per note, by name.
 */
data class NoteMentions(
    val linked: List<NoteMention>,
    val unlinked: List<NoteMention>
) {
    companion object {
        val NONE = NoteMentions(linked = emptyList(), unlinked = emptyList())
    }
}
