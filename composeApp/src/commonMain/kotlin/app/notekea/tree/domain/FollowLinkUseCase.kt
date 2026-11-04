package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.notes.domain.LinkKind
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NoteLink
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.wikiPath

/**
 * Where a link clicked in Read mode leads (`ROADMAP.md` 1.5), resolved among `notes` the way Obsidian and the link
 * updates do ([LinkResolver]) from the note `source` that holds it. A `[[wiki link]]` to a name several notes share
 * opens the closest one ([LinkResolver.closest]) instead of creating another; one to a note that does not exist
 * creates it where the link says ([wikiPath]), its folders too: a bare name at the vault root, as Obsidian does by
 * default. A link that leaves the vault, or names what cannot be a file, creates nothing. A Markdown link to a missing
 * file is reported instead, since its target is a path the user typed. `target` is the link's target only: no alias,
 * heading or block.
 */
class FollowLinkUseCase(
    private val createNote: CreateNoteUseCase,
    private val noteFileOperations: NoteFileOperations,
    private val validateFileName: ValidateFileNameUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        notes: Set<RelativePath>,
        source: RelativePath,
        target: String,
        kind: LinkKind
    ): Outcome<FollowedLink, NotesError> {
        val link = NoteLink(targetStart = 0, targetEnd = target.length, target = target, kind = kind)
        val resolved = LinkResolver(notePaths = notes).closest(link = link, source = source)
        return when {
            resolved != null -> Outcome.Success(value = FollowedLink(path = resolved, isNew = false))
            kind != LinkKind.WIKI -> Outcome.Failure(error = NotesError.EntryMissing(path = target))
            else -> create(vaultPath = vaultPath, target = target, source = source)
        }
    }

    private suspend fun create(
        vaultPath: String,
        target: String,
        source: RelativePath
    ): Outcome<FollowedLink, NotesError> {
        val path = wikiPath(target = target, source = source) ?: return Outcome.Failure(error = NotesError.InvalidName)
        val segments = path.value.split(RelativePath.SEPARATOR)
        val folder = path.parent ?: RelativePath.ROOT
        return when {
            segments.any { !validateFileName(name = it) } -> Outcome.Failure(error = NotesError.InvalidName)

            segments.any { it.startsWith(prefix = HIDDEN_PREFIX) } -> Outcome.Failure(error = NotesError.HiddenName)

            else -> when (val folders = createFolders(vaultPath = vaultPath, folder = folder)) {
                is Outcome.Failure -> folders
                is Outcome.Success -> createAt(vaultPath = vaultPath, folder = folder, name = path.name)
            }
        }
    }

    /** Creates [folder] and the folders above it that do not exist. */
    private suspend fun createFolders(
        vaultPath: String,
        folder: RelativePath
    ): Outcome<Unit, NotesError> {
        if (folder.isRoot || noteFileOperations.exists(vaultPath = vaultPath, path = folder)) {
            return Outcome.Success(value = Unit)
        }
        val above = createFolders(vaultPath = vaultPath, folder = folder.parent ?: RelativePath.ROOT)
        return if (above is Outcome.Failure) above else noteFileOperations.createFolder(vaultPath, folder)
    }

    private suspend fun createAt(
        vaultPath: String,
        folder: RelativePath,
        name: String
    ): Outcome<FollowedLink, NotesError> = when (
        val created = createNote(vaultPath = vaultPath, folder = folder, baseName = name)
    ) {
        is Outcome.Failure -> created
        is Outcome.Success -> Outcome.Success(value = FollowedLink(path = created.value, isNew = true))
    }

    private companion object {
        const val HIDDEN_PREFIX = "."
    }
}
