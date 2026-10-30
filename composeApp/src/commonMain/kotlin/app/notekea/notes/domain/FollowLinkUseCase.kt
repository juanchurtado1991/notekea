package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Where a link clicked in Read mode leads (`ROADMAP.md` 1.5), resolved among `notes` the way Obsidian and the link
 * updates do ([LinkResolver]) from the note `source` that holds it. A `[[wiki link]]` to a name several notes share
 * opens the closest one ([LinkResolver.closestWiki]) instead of creating another; one to a note that does not exist
 * creates it at the vault root, as Obsidian does by default. A Markdown link to a missing file is reported instead,
 * since its target is a path the user typed. `target` is the link's target only: no alias, heading or block.
 */
class FollowLinkUseCase(private val createNote: CreateNoteUseCase) {
    suspend operator fun invoke(
        vaultPath: String,
        notes: Set<RelativePath>,
        source: RelativePath,
        target: String,
        kind: LinkKind
    ): Outcome<FollowedLink, NotesError> {
        val link = NoteLink(targetStart = 0, targetEnd = target.length, target = target, kind = kind)
        val resolver = LinkResolver(notePaths = notes)
        val resolved = resolver.resolve(link = link, source = source)?.target
            ?: resolver.closestWiki(target = target, source = source).takeIf { kind == LinkKind.WIKI }
        return when {
            resolved != null -> Outcome.Success(value = FollowedLink(path = resolved, isNew = false))
            kind != LinkKind.WIKI -> Outcome.Failure(error = NotesError.EntryMissing(path = target))
            else -> create(vaultPath = vaultPath, target = target)
        }
    }

    private suspend fun create(
        vaultPath: String,
        target: String
    ): Outcome<FollowedLink, NotesError> {
        val name = LinkResolver.withoutMarkdownExtension(
            target = target.trim()
        ).substringAfterLast(delimiter = RelativePath.SEPARATOR)
        return when (val created = createNote(vaultPath = vaultPath, folder = RelativePath.ROOT, baseName = name)) {
            is Outcome.Failure -> created
            is Outcome.Success -> Outcome.Success(value = FollowedLink(path = created.value, isNew = true))
        }
    }
}
