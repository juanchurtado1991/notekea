package app.notekea.core.notes.domain.links

import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.RelativePath

/**
 * Prepares, without writing anything, the notes whose links must change after `from` was renamed to [to] within its
 * folder; `notesBefore` are the vault's notes before the rename, so old links resolve as they did. Notes whose text
 * does not hold the old name are not parsed. A rewritten link keeps its style (bare, a path, `.md`, relative, encoded).
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
            after = LinkResolver(
                notePaths = notesBefore.map {
                    it.rebased(
                        from = from,
                        to = to
                    )
                }
            )
        )

        return notesBefore.mapNotNull { oldSource ->
            val source = oldSource.rebased(
                from = from,
                to = to
            )

            val text = noteContentStore.read(
                vaultPath = vaultPath,
                path = source
            )

            val mayLink = text != null && mayName(text = text, name = from.nameWithoutExtension)

            if (mayLink) {
                rewrite(
                    text = text,
                    oldSource = oldSource,
                    source = source,
                    rename = rename
                )
            } else {
                null
            }
        }
    }

    /** Whether [text] holds [name] at all: a Markdown link may only hold it encoded (`My%20Note.md`). */
    private fun mayName(
        text: String,
        name: String
    ): Boolean = text.contains(other = name, ignoreCase = true) ||
        percentDecoded(target = text).contains(other = name, ignoreCase = true)

    private fun rewrite(
        text: String,
        oldSource: RelativePath,
        source: RelativePath,
        rename: Rename
    ): NoteRewrite? {
        val edits = linkFinder.find(markdown = text).mapNotNull { link ->
            val resolved = rename
                .before
                .resolve(
                    link = link,
                    source = oldSource
                )
                ?.takeIf { it.target.isWithin(folder = rename.from) }

            val newTarget = resolved?.let {
                newLinkTarget(
                    link = link,
                    resolved = it,
                    source = source,
                    rename = rename
                )
            }

            newTarget?.takeIf {
                it != link.target
            }?.let {
                link to it
            }
        }

        if (edits.isEmpty()) return null

        val updated = edits
            .sortedByDescending { it.first.targetStart }
            .fold(initial = text) { current, (link, target) ->
                current.replaceRange(
                    startIndex = link.targetStart,
                    endIndex = link.targetEnd,
                    replacement = target
                )
            }

        return NoteRewrite(
            path = source,
            original = text,
            updated = updated,
            linkCount = edits.size
        )
    }

    private fun newLinkTarget(
        link: NoteLink,
        resolved: ResolvedLink,
        source: RelativePath,
        rename: Rename
    ): String {
        val target = resolved.target.rebased(
            from = rename.from,
            to = rename.to
        )

        return when (link.kind) {
            LinkKind.WIKI -> {
                newWikiTarget(
                    written = link.target,
                    target = target,
                    after = rename.after
                )
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

        val isBare = RelativePath.SEPARATOR !in trimmed &&
            after.isUniqueName(name = target.nameWithoutExtension)

        val text = if (isBare) {
            target.nameWithoutExtension
        } else {
            LinkResolver.withoutMarkdownExtension(target = target.value)
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
