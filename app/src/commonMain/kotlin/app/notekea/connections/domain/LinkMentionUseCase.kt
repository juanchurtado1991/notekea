package app.notekea.connections.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.isTableRow
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.LinkResolver
import app.notekea.core.notes.domain.links.NoteRewrite
import app.notekea.core.notes.domain.links.isWikiLinkable
import app.notekea.core.notes.domain.links.markdownLink

/**
 * Turns an unlinked mention into `[[target]]`, or `[[target|words]]` when the words are written differently (the `|`
 * escaped in a table), the target as short as stays unambiguous ([LinkResolver.wikiTarget]); a Markdown link for a name
 * a wiki link cannot hold. Written only if the words are still [note]'s name or an alias and the file is unchanged.
 */
class LinkMentionUseCase(
    private val noteContentStore: NoteContentStore,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        mention: NoteMention,
        note: NoteEntry,
        notes: Collection<RelativePath>
    ): Boolean {
        val target = note.file.path
        val names = listOf(target.nameWithoutExtension) + note.metadata.aliases

        val original = noteContentStore.read(
            vaultPath = vaultPath,
            path = mention.source
        ) ?: return false

        val document = NoteDocument.fromText(text = original)
        if (mention.range.last >= document.body.length) return false
        val words = document.body.substring(range = mention.range)

        if (names.none {
                it.trim().equals(
                    other = words,
                    ignoreCase = true
                )
            }
        ) {
            return false
        }

        val link = linkTo(
            target = target,
            words = words,
            notes = notes,
            source = mention.source,
            isInTable = isTableRow(
                text = document.body,
                offset = mention.range.first
            )
        )

        val body = document.body.replaceRange(
            range = mention.range,
            replacement = link
        )

        val rewrite = NoteRewrite(
            path = mention.source,
            original = original,
            updated = document.copy(body = body).toText(),
            linkCount = 1
        )

        return applyLinkUpdates(
            vaultPath = vaultPath,
            rewrites = listOf(rewrite)
        ).updated.isNotEmpty()
    }

    /** `[[target|words]]`, or for a name that would end a wiki link early (`C# notes`), `[words](C%23%20notes.md)`. */
    private fun linkTo(
        target: RelativePath,
        words: String,
        notes: Collection<RelativePath>,
        source: RelativePath,
        isInTable: Boolean
    ): String {
        val wikiTarget = LinkResolver(notePaths = notes).wikiTarget(path = target)

        if (!isWikiLinkable(target = wikiTarget)) {
            return markdownLink(
                label = words,
                target = target,
                source = source
            )
        }

        val bar = if (isInTable) {
            MarkdownSyntax.LINK_LABEL_SEPARATOR_ESCAPED
        } else {
            MarkdownSyntax.LINK_LABEL_SEPARATOR
        }

        return if (words == wikiTarget) "[[$wikiTarget]]" else "[[$wikiTarget$bar$words]]"
    }
}
