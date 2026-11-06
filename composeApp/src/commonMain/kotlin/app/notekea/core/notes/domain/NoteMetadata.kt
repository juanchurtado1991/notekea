package app.notekea.core.notes.domain

/**
 * What a note's frontmatter says about it, its typed [properties] among it, and what its text holds: the `#tags`
 * ([textTags], see [TextTagFinder]) and the [tasks].
 * [title] is null when the frontmatter has none (or none can be read); a note without frontmatter, or with frontmatter
 * that cannot be parsed, gets [NONE] and still appears in the vault.
 */
data class NoteMetadata(
    val tags: List<String>,
    val aliases: List<String>,
    val title: String?,
    val textTags: List<String> = emptyList(),
    val properties: List<NoteProperty> = emptyList(),
    val tasks: List<NoteTask> = emptyList()
) {
    companion object {
        val NONE = NoteMetadata(tags = emptyList(), aliases = emptyList(), title = null)
    }
}
