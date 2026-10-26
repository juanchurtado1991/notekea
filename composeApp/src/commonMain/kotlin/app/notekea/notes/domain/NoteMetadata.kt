package app.notekea.notes.domain

/**
 * What a note's frontmatter says about it, and the `#tags` written in its text ([textTags], see [TextTagFinder]).
 * [title] is null when the frontmatter has none (or none can be read); a note without frontmatter, or with frontmatter
 * that cannot be parsed, gets [NONE] and still appears in the vault.
 */
data class NoteMetadata(
    val tags: List<String>,
    val aliases: List<String>,
    val title: String?,
    val textTags: List<String> = emptyList()
) {
    companion object {
        val NONE = NoteMetadata(tags = emptyList(), aliases = emptyList(), title = null)
    }
}
