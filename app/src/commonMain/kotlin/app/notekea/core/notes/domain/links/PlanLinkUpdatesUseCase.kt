package app.notekea.core.notes.domain.links

import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.RelativePath

/**
 * Prepares, without writing anything, the notes whose links must change after `from` was renamed or moved to [to],
 * from the vault's notes and attachments before it, so old links resolve as they did; a moved note's links included.
 * A rewritten link keeps its style (bare, a path, `.md`, relative, encoded) where it still resolves.
 */
class PlanLinkUpdatesUseCase(
    private val noteContentStore: NoteContentStore,
    private val linkFinder: LinkFinder
) {
    suspend operator fun invoke(
        vaultPath: String,
        notesBefore: Set<RelativePath>,
        attachmentsBefore: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ): List<NoteRewrite> {
        // Every attachment of the vault is known, not only the renamed one: a folder of pictures follows its rename,
        // and a bare `![[pic.png]]` that two pictures share leads to neither, so it is never rewritten to one.
        val rename = Rename(
            from = from,
            to = to,
            before = LinkResolver(
                notePaths = notesBefore,
                attachmentPaths = attachmentsBefore
            ),
            after = LinkResolver(
                notePaths = rebased(
                    paths = notesBefore,
                    from = from,
                    to = to
                ),
                attachmentPaths = rebased(
                    paths = attachmentsBefore,
                    from = from,
                    to = to
                )
            ),
            hasAttachments = attachmentsBefore.isNotEmpty()
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

            // A note moved to another folder is read whatever it names: its own relative links may break.
            val isMoved = source.parent != oldSource.parent

            val mayLink = text != null &&
                (isMoved || mayName(text = text, name = from.nameWithoutExtension))

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

    private fun rebased(
        paths: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ): List<RelativePath> = paths.map {
        it.rebased(
            from = from,
            to = to
        )
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
        // In a vault with no attachments, a link to anything but a note leads nowhere, so only note links are read.
        val links = if (rename.hasAttachments) {
            linkFinder.findWithAttachments(markdown = text)
        } else {
            linkFinder.find(markdown = text)
        }

        val edits = links.mapNotNull { link ->
            editOf(
                text = text,
                link = link,
                oldSource = oldSource,
                source = source,
                rename = rename
            )
        }

        if (edits.isEmpty()) return null

        val updated = edits
            .sortedByDescending { it.range.first }
            .fold(initial = text) { current, edit ->
                current.replaceRange(
                    range = edit.range,
                    replacement = edit.replacement
                )
            }

        return NoteRewrite(
            path = source,
            original = text,
            updated = updated,
            linkCount = edits.size
        )
    }

    /**
     * How [link] changes, or null when it stays: a link to what was renamed always follows it; any other link only
     * when its note moved to another folder and the link, read from there, no longer leads where it did.
     */
    private fun editOf(
        text: String,
        link: NoteLink,
        oldSource: RelativePath,
        source: RelativePath,
        rename: Rename
    ): Edit? {
        val resolved = rename
            .before
            .resolve(
                link = link,
                source = oldSource
            ) ?: return null

        if (!resolved.target.isWithin(folder = rename.from)) {
            if (source.parent == oldSource.parent) return null

            val stillLeads = rename
                .after
                .resolve(
                    link = link,
                    source = source
                )
                ?.target == resolved.target

            if (stillLeads) return null
        }

        val target = resolved.target.rebased(
            from = rename.from,
            to = rename.to
        )

        val written = newLinkTarget(
            link = link,
            resolved = resolved,
            target = target,
            source = source,
            after = rename.after
        )

        // A wiki link cannot name a note whose name holds `#`, `^`, `|` or brackets: it becomes a Markdown link.
        val isUnwritableWiki = link.kind == LinkKind.WIKI && !isWikiLinkable(target = written)

        return when {
            written == link.target -> null

            isUnwritableWiki -> Edit(
                range = linkSpan(
                    body = text,
                    link = link
                ),
                replacement = wikiAsMarkdown(
                    body = text,
                    link = link,
                    target = target,
                    source = source
                )
            )

            else -> Edit(
                range = link.targetStart until link.targetEnd,
                replacement = written
            )
        }
    }

    private fun newLinkTarget(
        link: NoteLink,
        resolved: ResolvedLink,
        target: RelativePath,
        source: RelativePath,
        after: LinkResolver
    ): String = when (link.kind) {
        LinkKind.WIKI -> {
            newWikiTarget(
                written = link.target,
                target = target,
                source = source,
                after = after
            )
        }

        LinkKind.MARKDOWN_ANGLE, LinkKind.MARKDOWN_ENCODED -> {
            newMarkdownTarget(
                link = link,
                resolved = resolved,
                target = target,
                source = source
            )
        }
    }

    private fun newMarkdownTarget(
        link: NoteLink,
        resolved: ResolvedLink,
        target: RelativePath,
        source: RelativePath
    ): String {
        val folder = source.parent ?: RelativePath.ROOT
        val isFromRoot = link.target.startsWith(prefix = RelativePath.SEPARATOR)

        val path = when {
            resolved.isRelativeToSource -> relativeTo(
                folder = folder,
                target = target
            )

            isFromRoot -> RelativePath.SEPARATOR + target.value

            else -> target.value
        }

        // `[text](Note)` stays without `.md`, as it was written.
        val hasExtension = link.target.endsWith(
            suffix = RelativePath.MARKDOWN_EXTENSION,
            ignoreCase = true
        )

        val kept = if (hasExtension) path else LinkResolver.withoutMarkdownExtension(target = path)

        return if (link.kind == LinkKind.MARKDOWN_ENCODED) spacesEncoded(target = kept) else kept
    }

    private fun newWikiTarget(
        written: String,
        target: RelativePath,
        source: RelativePath,
        after: LinkResolver
    ): String {
        val trimmed = written.trim()
        val hasExtension = LinkResolver.withoutMarkdownExtension(target = trimmed) != trimmed
        val first = trimmed.substringBefore(delimiter = RelativePath.SEPARATOR)
        val isRelative = first == CURRENT || first == PARENT

        val isBare = RelativePath.SEPARATOR !in trimmed &&
            after.isUniqueName(name = target.nameWithoutExtension)

        val text = when {
            isBare -> target.nameWithoutExtension

            isRelative -> relativeWikiTarget(
                folder = source.parent ?: RelativePath.ROOT,
                target = target
            )

            else -> LinkResolver.withoutMarkdownExtension(target = target.value)
        }

        return if (hasExtension) text + RelativePath.MARKDOWN_EXTENSION else text
    }

    /** `./C` or `../B/C`: a wiki link written relative stays relative, from the note's new folder. */
    private fun relativeWikiTarget(
        folder: RelativePath,
        target: RelativePath
    ): String {
        val path = LinkResolver.withoutMarkdownExtension(
            target = relativeTo(
                folder = folder,
                target = target
            )
        )

        val isClimbing = path.startsWith(prefix = PARENT)

        return if (isClimbing) path else CURRENT + RelativePath.SEPARATOR + path
    }

    /** The rename being applied, with link resolution before and after it. */
    private class Rename(
        val from: RelativePath,
        val to: RelativePath,
        val before: LinkResolver,
        val after: LinkResolver,
        val hasAttachments: Boolean
    )

    /** The text that replaces [range] of a note. */
    private class Edit(
        val range: IntRange,
        val replacement: String
    )

    private companion object {
        const val CURRENT = "."
        const val PARENT = ".."
    }
}
