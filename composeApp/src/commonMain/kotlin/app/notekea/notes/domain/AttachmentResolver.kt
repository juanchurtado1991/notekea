package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * Finds the file an [Embed] shows among the vault's [files], as Obsidian does: a target with a folder is a path from
 * the vault root (a Markdown link is read from the note's folder first); a bare name is looked for in the note's
 * folder, then in the attachments folder, then anywhere, the shortest path first. Letter case is ignored. Null when no
 * file matches: the embed shows as missing.
 */
class AttachmentResolver(
    files: Collection<RelativePath>,
    private val attachmentFolder: RelativePath
) {
    private val byLowerPath = files.associateBy { it.value.lowercase() }
    private val byLowerName = files.groupBy { it.name.lowercase() }

    fun resolve(
        embed: Embed,
        note: RelativePath
    ): RelativePath? {
        val target = embed.target.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR).trim()
        val noteFolder = note.parent ?: RelativePath.ROOT
        if (embed.kind == EmbedKind.MARKDOWN) {
            resolvedFrom(folder = noteFolder, target = target)?.let { find(path = it) }?.let { return it }
        }
        if (RelativePath.SEPARATOR in target) {
            return resolvedFrom(folder = RelativePath.ROOT, target = target)?.let { find(path = it) }
        }
        val candidates = byLowerName[target.lowercase()].orEmpty()
        return candidates.minWithOrNull(
            comparator = compareBy<RelativePath>(
                { it.parent != noteFolder },
                { it.parent != attachmentFolder },
                { path -> path.value.count { it == RelativePath.SEPARATOR.single() } },
                { it.value.lowercase() }
            )
        )
    }

    private fun find(
        path: RelativePath
    ): RelativePath? = byLowerPath[path.value.lowercase()]
}
