package app.notekea.notes.domain

/**
 * What an indexing run does (`ROADMAP.md` 2.3): the notes to [read] (new, changed on disk, or indexed with another
 * version), the indexed notes to [remove] (gone from the vault), and how many are [unchanged].
 */
data class IndexPlan(
    val read: List<NoteFile>,
    val remove: List<RelativePath>,
    val unchanged: Int
) {
    val isEmpty: Boolean get() = read.isEmpty() && remove.isEmpty()
}
