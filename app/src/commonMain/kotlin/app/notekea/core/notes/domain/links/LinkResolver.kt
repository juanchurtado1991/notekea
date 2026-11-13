package app.notekea.core.notes.domain.links

import app.notekea.core.notes.domain.RelativePath

/**
 * Resolves links the way Obsidian does, among [notePaths], ignoring letter case. A wiki link with a folder must match a
 * path or the end of one ([wikiPath]); a bare name must match one note's name, the linking note's folder winning, and
 * any other ambiguity resolves to nothing. A Markdown link is read from the linking note's folder, then the vault root.
 */
class LinkResolver(notePaths: Collection<RelativePath>) {
    private val notes = notePaths.toSet()
    private val byLowerPath = notePaths.associateBy { it.value.lowercase() }
    private val byLowerName = notePaths.groupBy { it.nameWithoutExtension.lowercase() }

    fun resolve(
        link: NoteLink,
        source: RelativePath
    ): ResolvedLink? = when (link.kind) {
        LinkKind.WIKI -> resolveWiki(
            target = link.target,
            source = source
        )?.let {
            ResolvedLink(
                target = it,
                isRelativeToSource = false
            )
        }

        LinkKind.MARKDOWN_ANGLE -> resolveMarkdown(
            target = link.target,
            source = source
        )

        LinkKind.MARKDOWN_ENCODED -> resolveMarkdown(
            target = percentDecoded(target = link.target),
            source = source
        )
    }

    /**
     * The note [link] leads to when read, not rewritten: the one [resolve] finds, or for a wiki link to a name several
     * notes share, the closest ([closestWiki]). Following it, backlinks and "Already linked" all agree on this note.
     */
    fun closest(
        link: NoteLink,
        source: RelativePath
    ): RelativePath? = resolve(
        link = link,
        source = source
    )?.target
        ?: closestWiki(
            target = link.target,
            source = source
        ).takeIf {
            link.kind == LinkKind.WIKI
        }

    /**
     * The note a wiki link to [target] leads to when [resolve] finds no single one because several notes share its
     * name: the one in [source]'s folder, else the one closest to the vault root, else the first by path. Null when no
     * note matches. Only for reading a link: an ambiguous link is still never rewritten.
     */
    fun closestWiki(
        target: String,
        source: RelativePath
    ): RelativePath? {
        val name = wikiPath(
            target = target,
            source = source
        )?.value?.lowercase() ?: return null

        val candidates = if (RelativePath.SEPARATOR in name) {
            val withExtension = name + RelativePath.MARKDOWN_EXTENSION

            notes.filter {
                val path = it.value.lowercase()

                path == withExtension ||
                    path.endsWith(suffix = RelativePath.SEPARATOR + withExtension)
            }
        } else {
            byLowerName[name].orEmpty()
        }

        return candidates.minWithOrNull(
            comparator = compareBy<RelativePath>(
                {
                    it.parent != source.parent
                },
                { path ->
                    path.value.count {
                        it == RelativePath.SEPARATOR.single()
                    }
                },
                { it.value.lowercase() }
            )
        )
    }

    /** How a new wiki link to [path] is written: its name when that is unique in the vault, else its path. */
    fun wikiTarget(path: RelativePath): String =
        if (isUniqueName(name = path.nameWithoutExtension)) {
            path.nameWithoutExtension
        } else {
            withoutMarkdownExtension(target = path.value)
        }

    /** Whether a bare `[[name]]` would point to exactly one note. */
    fun isUniqueName(name: String): Boolean = byLowerName[name.lowercase()].orEmpty().size == 1

    private fun resolveMarkdown(
        target: String,
        source: RelativePath
    ): ResolvedLink? {
        val fromSource = noteAt(
            folder = source.parent ?: RelativePath.ROOT,
            target = target
        )

        if (fromSource != null) {
            return ResolvedLink(
                target = fromSource,
                isRelativeToSource = true
            )
        }

        val fromRoot = noteAt(
            folder = RelativePath.ROOT,
            target = target.removePrefix(prefix = RelativePath.SEPARATOR)
        )

        return (
            fromRoot ?: resolveWiki(
                target = target,
                source = source
            )
            )?.let {
            ResolvedLink(
                target = it,
                isRelativeToSource = false
            )
        }
    }

    /** The note [target] names read from [folder]: exactly as written, else ignoring letter case. */
    private fun noteAt(
        folder: RelativePath,
        target: String
    ): RelativePath? {
        val path = resolvedFrom(
            folder = folder,
            target = target
        ) ?: return null

        return path.takeIf {
            it in notes
        } ?: byLowerPath[path.value.lowercase()]
    }

    private fun resolveWiki(
        target: String,
        source: RelativePath
    ): RelativePath? {
        val name = wikiPath(
            target = target,
            source = source
        )?.value?.lowercase() ?: return null

        if (RelativePath.SEPARATOR in name) {
            val withExtension = name + RelativePath.MARKDOWN_EXTENSION

            return byLowerPath[withExtension]
                ?: notes
                    .filter {
                        it
                            .value
                            .lowercase()
                            .endsWith(suffix = RelativePath.SEPARATOR + withExtension)
                    }.singleOrNull()
        }

        val candidates = byLowerName[name].orEmpty()

        return candidates.singleOrNull() ?: candidates.singleOrNull {
            it.parent == source.parent
        }
    }

    companion object {
        fun withoutMarkdownExtension(target: String): String {
            val isMarkdown = target.endsWith(
                suffix = RelativePath.MARKDOWN_EXTENSION,
                ignoreCase = true
            )

            if (!isMarkdown) return target

            return target.dropLast(n = RelativePath.MARKDOWN_EXTENSION.length)
        }
    }
}
