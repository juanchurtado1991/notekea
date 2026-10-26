package app.notekea.notes.domain

/**
 * Resolves links the way Obsidian does, among [notePaths], ignoring letter case. A wiki link with a folder must match
 * a path (or the end of one); a bare name must match one note's name, and among several the one in the linking note's
 * folder wins; any other ambiguity resolves to nothing, so an unclear link is never rewritten. A Markdown link is read
 * from the linking note's folder first and from the vault root otherwise.
 */
class LinkResolver(notePaths: Collection<RelativePath>) {
    private val notes = notePaths.toSet()
    private val byLowerPath = notePaths.associateBy { it.value.lowercase() }
    private val byLowerName = notePaths.groupBy { it.nameWithoutExtension.lowercase() }

    fun resolve(
        link: NoteLink,
        source: RelativePath
    ): ResolvedLink? = when (link.kind) {
        LinkKind.WIKI -> resolveWiki(target = link.target, source = source)?.let {
            ResolvedLink(target = it, isRelativeToSource = false)
        }

        LinkKind.MARKDOWN_ANGLE -> resolveMarkdown(target = link.target, source = source)

        LinkKind.MARKDOWN_ENCODED -> resolveMarkdown(target = percentDecoded(target = link.target), source = source)
    }

    /**
     * The note a clicked wiki link to [target] opens when [resolve] finds no single one because several notes share its
     * name: the one in [source]'s folder, else the one closest to the vault root, else the first by path. Null when no
     * note matches. Only for following a link: an ambiguous link is still never rewritten.
     */
    fun closestWiki(
        target: String,
        source: RelativePath
    ): RelativePath? {
        val name = withoutMarkdownExtension(target = target.trim()).lowercase()
        val candidates = if (RelativePath.SEPARATOR in name) {
            val withExtension = name + RelativePath.MARKDOWN_EXTENSION
            notes.filter {
                val path = it.value.lowercase()
                path == withExtension || path.endsWith(suffix = RelativePath.SEPARATOR + withExtension)
            }
        } else {
            byLowerName[name].orEmpty()
        }
        return candidates.minWithOrNull(
            comparator = compareBy<RelativePath>(
                { it.parent != source.parent },
                { path -> path.value.count { it == RelativePath.SEPARATOR.single() } },
                { it.value.lowercase() }
            )
        )
    }

    /** How a new wiki link to [path] is written: its name when that is unique in the vault, else its path. */
    fun wikiTarget(
        path: RelativePath
    ): String = if (isUniqueName(name = path.nameWithoutExtension)) {
        path.nameWithoutExtension
    } else {
        withoutMarkdownExtension(target = path.value)
    }

    /** Whether a bare `[[name]]` would point to exactly one note. */
    fun isUniqueName(
        name: String
    ): Boolean = byLowerName[name.lowercase()].orEmpty().size == 1

    private fun resolveMarkdown(
        target: String,
        source: RelativePath
    ): ResolvedLink? {
        val fromSource = resolvedFrom(folder = source.parent ?: RelativePath.ROOT, target = target)
        if (fromSource != null &&
            fromSource in notes
        ) {
            return ResolvedLink(target = fromSource, isRelativeToSource = true)
        }
        val fromRoot =
            resolvedFrom(folder = RelativePath.ROOT, target = target.removePrefix(prefix = RelativePath.SEPARATOR))
        return fromRoot?.takeIf { it in notes }?.let { ResolvedLink(target = it, isRelativeToSource = false) }
    }

    private fun resolveWiki(
        target: String,
        source: RelativePath
    ): RelativePath? {
        val name = withoutMarkdownExtension(target = target.trim()).lowercase()
        if (RelativePath.SEPARATOR in name) {
            val withExtension = name + RelativePath.MARKDOWN_EXTENSION
            return byLowerPath[withExtension]
                ?: notes
                    .filter {
                        it.value.lowercase().endsWith(
                            suffix = RelativePath.SEPARATOR + withExtension
                        )
                    }.singleOrNull()
        }
        val candidates = byLowerName[name].orEmpty()
        return candidates.singleOrNull() ?: candidates.singleOrNull { it.parent == source.parent }
    }

    companion object {
        fun withoutMarkdownExtension(
            target: String
        ): String = if (target.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION, ignoreCase = true)) {
            target.dropLast(n = RelativePath.MARKDOWN_EXTENSION.length)
        } else {
            target
        }
    }
}
