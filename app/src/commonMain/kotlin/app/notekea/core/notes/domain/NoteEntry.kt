package app.notekea.core.notes.domain

/** A note of the vault: its file and what its frontmatter says. */
data class NoteEntry(
    val file: NoteFile,
    val metadata: NoteMetadata
) {
    /** The folders the note is in, the nearest first, without the vault's root. */
    val folders: List<String>
        get() = generateSequence(file.path.parent) { it.parent }
            .filterNot { it.isRoot }
            .map { it.value }
            .toList()

    /** Its tags, in its frontmatter and its text. */
    val allTags: List<String>
        get() = metadata.tags + metadata.textTags

    /** Whether it has [tag] or a tag nested under it, whatever their case, with or without the `#`. */
    fun hasTag(tag: String): Boolean {
        val wanted = tag.trim().removePrefix(prefix = TAG_MARK)

        return allTags.any {
            it.equals(other = wanted, ignoreCase = true) || it.startsWith(wanted + NESTED, true)
        }
    }

    private companion object {
        const val NESTED = "/"
        const val TAG_MARK = "#"
    }
}
