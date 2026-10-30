package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * How Notekea writes an embed of a file in a note, as Obsidian does by default: `![[name.png]]` when no other file of
 * the vault ([files]) has that name, otherwise `![[folder/name.png]]`; a `width` is kept as `|300`.
 */
class EmbedText(files: Collection<RelativePath>) {
    private val names = files.groupingBy { it.name.lowercase() }.eachCount()

    fun of(
        path: RelativePath,
        width: Int? = null
    ): String {
        val isUnique = (names[path.name.lowercase()] ?: 0) <= 1
        val target = if (isUnique) path.name else path.value
        val size = width?.let { MarkdownSyntax.LINK_LABEL_SEPARATOR + it }.orEmpty()
        return EMBED_MARK + MarkdownSyntax.WIKI_LINK_OPEN + target + size + MarkdownSyntax.WIKI_LINK_CLOSE
    }

    companion object {
        private const val EMBED_MARK = "!"
    }
}
