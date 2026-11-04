package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * Turns an unlinked mention into a link (`ROADMAP.md` 2.5), only because the person clicked Link: the words at the
 * mention's range in its note become `[[target]]`, or `[[target|words]]` when they are written differently (an alias,
 * another letter case), so the note reads the same; in a table that `|` is escaped, or it would end the cell. The
 * target is as short as stays unambiguous
 * ([LinkResolver.wikiTarget]). The note is written only if the words there are still [note]'s name or an alias and
 * the file does not change meanwhile; otherwise nothing changes and false is returned.
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
        val original = noteContentStore.read(vaultPath = vaultPath, path = mention.source) ?: return false
        val document = NoteDocument.fromText(text = original)
        if (mention.range.last >= document.body.length) return false
        val words = document.body.substring(range = mention.range)
        if (names.none { it.trim().equals(other = words, ignoreCase = true) }) return false
        val wikiTarget = LinkResolver(notePaths = notes).wikiTarget(path = target)
        val inTable = isTableRow(text = document.body, offset = mention.range.first)
        val bar = if (inTable) MarkdownSyntax.LINK_LABEL_SEPARATOR_ESCAPED else MarkdownSyntax.LINK_LABEL_SEPARATOR
        val link = if (words == wikiTarget) "[[$wikiTarget]]" else "[[$wikiTarget$bar$words]]"
        val body = document.body.replaceRange(range = mention.range, replacement = link)
        val rewrite = NoteRewrite(
            path = mention.source,
            original = original,
            updated = document.copy(body = body).toText(),
            linkCount = 1
        )
        return applyLinkUpdates(vaultPath = vaultPath, rewrites = listOf(rewrite)).updated.isNotEmpty()
    }
}
