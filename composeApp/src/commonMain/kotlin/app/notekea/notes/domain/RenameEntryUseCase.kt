package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase

/**
 * Renames a note or folder in place and returns its new path. Notes are named without their extension, as the tree
 * shows them; a typed `.md` is not doubled. The name is trimmed and must be valid on every system and not start with
 * a dot, which would hide it. Keeping the current name changes nothing.
 */
class RenameEntryUseCase(
    private val noteFileOperations: NoteFileOperations,
    private val validateFileName: ValidateFileNameUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        newName: String
    ): Outcome<RelativePath, NotesError> {
        val trimmed = newName.trim()
        val baseName = if (path.isMarkdown) {
            trimmed.removeSuffixIgnoringCase(
                suffix = RelativePath.MARKDOWN_EXTENSION
            )
        } else {
            trimmed
        }
        val fileName = if (path.isMarkdown) baseName.trimEnd() + RelativePath.MARKDOWN_EXTENSION else baseName
        val target = (path.parent ?: RelativePath.ROOT).child(name = fileName)
        return when {
            !validateFileName(name = baseName.trimEnd()) -> Outcome.Failure(error = NotesError.InvalidName)
            fileName.startsWith(prefix = HIDDEN_PREFIX) -> Outcome.Failure(error = NotesError.HiddenName)
            target == path -> Outcome.Success(value = path)
            else -> moveTo(vaultPath = vaultPath, path = path, target = target)
        }
    }

    private suspend fun moveTo(
        vaultPath: String,
        path: RelativePath,
        target: RelativePath
    ): Outcome<RelativePath, NotesError> = when (
        val moved = noteFileOperations.move(
            vaultPath = vaultPath,
            from = path,
            to = target
        )
    ) {
        is Outcome.Failure -> moved
        is Outcome.Success -> Outcome.Success(value = target)
    }

    private fun String.removeSuffixIgnoringCase(
        suffix: String
    ): String = if (endsWith(suffix = suffix, ignoreCase = true)) dropLast(n = suffix.length) else this

    companion object {
        private const val HIDDEN_PREFIX = "."
    }
}
