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

/**
 * Turns an unlinked mention into `[[target]]`, or `[[target|words]]` when the words are written differently (the `|`
 * escaped in a table), the target as short as stays unambiguous ([LinkResolver.wikiTarget]). The note is written only
 * if the words are still [note]'s name or an alias and the file does not change meanwhile; otherwise false.
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

        val wikiTarget = LinkResolver(notePaths = notes).wikiTarget(path = target)

        val inTable = isTableRow(
            text = document.body,
            offset = mention.range.first
        )

        val bar = if (inTable) {
            MarkdownSyntax.LINK_LABEL_SEPARATOR_ESCAPED
        } else {
            MarkdownSyntax.LINK_LABEL_SEPARATOR
        }

        val link = if (words == wikiTarget) "[[$wikiTarget]]" else "[[$wikiTarget$bar$words]]"

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
}
