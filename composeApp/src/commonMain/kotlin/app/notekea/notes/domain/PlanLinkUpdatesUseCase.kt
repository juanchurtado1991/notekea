package app.notekea.notes.domain

/**
 * Prepares, without writing anything, the notes whose links must change after [from] was renamed to [to] (a note or
 * a folder, in the same parent folder). [notesBefore] are the vault's notes before the rename, so old links resolve as
 * they did; the files are read at their current paths. A rename keeps the parent folder, so every affected link names
 * the old entry: notes whose text does not contain that name are not parsed at all, which keeps large vaults fast.
 * A rewritten link keeps its style: a bare `[[name]]` stays bare unless the new name is shared by another note, a path
 * stays a path, `.md` stays when it was written, and a Markdown link stays relative or vault-rooted and encoded alike.
 */
class PlanLinkUpdatesUseCase(
    private val noteContentStore: NoteContentStore,
    private val linkFinder: LinkFinder
) {
    suspend operator fun invoke(
        vaultPath: String,
        notesBefore: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ): List<NoteRewrite> {
        val rename = Rename(
            from = from,
            to = to,
            before = LinkResolver(notePaths = notesBefore),
            after = LinkResolver(notePaths = notesBefore.map { it.rebased(from = from, to = to) })
        )
        return notesBefore.mapNotNull { oldSource ->
            val source = oldSource.rebased(from = from, to = to)
            val text = noteContentStore.read(vaultPath = vaultPath, path = source)
            val mayLink = text != null && text.contains(other = from.nameWithoutExtension, ignoreCase = true)
            if (mayLink) rewrite(text = text, oldSource = oldSource, source = source, rename = rename) else null
        }
    }

    private fun rewrite(
        text: String,
        oldSource: RelativePath,
        source: RelativePath,
        rename: Rename
    ): NoteRewrite? {
        val edits = linkFinder.find(markdown = text).mapNotNull { link ->
            val resolved = rename.before.resolve(link = link, source = oldSource)?.takeIf {
                it.target.isWithin(folder = rename.from)
            }
            val newTarget = resolved?.let {
                newLinkTarget(
                    link = link,
                    resolved = it,
                    source = source,
                    rename = rename
                )
            }
            newTarget?.takeIf { it != link.target }?.let { link to it }
        }
        if (edits.isEmpty()) return null
        val updated = edits.sortedByDescending { it.first.targetStart }.fold(
            initial = text
        ) { current, (link, target) ->
            current.replaceRange(startIndex = link.targetStart, endIndex = link.targetEnd, replacement = target)
        }
        return NoteRewrite(path = source, original = text, updated = updated, linkCount = edits.size)
    }

    private fun newLinkTarget(
        link: NoteLink,
        resolved: ResolvedLink,
        source: RelativePath,
        rename: Rename
    ): String {
        val target = resolved.target.rebased(from = rename.from, to = rename.to)
        return when (link.kind) {
            LinkKind.WIKI -> {
                newWikiTarget(written = link.target, target = target, after = rename.after)
            }

            LinkKind.MARKDOWN_ANGLE, LinkKind.MARKDOWN_ENCODED -> {
                val path = if (resolved.isRelativeToSource) {
                    relativeTo(
                        folder = source.parent ?: RelativePath.ROOT,
                        target = target
                    )
                } else {
                    target.value
                }
                if (link.kind == LinkKind.MARKDOWN_ENCODED) spacesEncoded(target = path) else path
            }
        }
    }

    private fun newWikiTarget(
        written: String,
        target: RelativePath,
        after: LinkResolver
    ): String {
        val trimmed = written.trim()
        val hasExtension = LinkResolver.withoutMarkdownExtension(target = trimmed) != trimmed
        val isBare = RelativePath.SEPARATOR !in trimmed && after.isUniqueName(name = target.nameWithoutExtension)
        val text = if (isBare) {
            target.nameWithoutExtension
        } else {
            LinkResolver.withoutMarkdownExtension(
                target = target.value
            )
        }
        return if (hasExtension) text + RelativePath.MARKDOWN_EXTENSION else text
    }

    /** The rename being applied, with link resolution before and after it. */
    private class Rename(
        val from: RelativePath,
        val to: RelativePath,
        val before: LinkResolver,
        val after: LinkResolver
    )
}
