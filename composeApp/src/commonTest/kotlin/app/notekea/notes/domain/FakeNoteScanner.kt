package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * A vault on paper (notes and files bring their folders along): [files], [attachments] and [folders] are what a scan
 * finds, [failure] makes every scan fail.
 */
class FakeNoteScanner : NoteScanner {
    val files = mutableMapOf<RelativePath, NoteFile>()
    val folders = mutableSetOf<RelativePath>()
    val attachments = mutableMapOf<RelativePath, NoteFile>()
    var failure: NotesError? = null
    var scanCount = 0

    /** Adds the note and, as a real folder would hold it, its parent folders. */
    fun addNote(
        file: NoteFile
    ) {
        files[file.path] = file
        folders += generateSequence(seed = file.path.parent) { it.parent }.filterNot { it.isRoot }
    }

    /** Adds a file that is not a note (an image, a PDF…) and its parent folders. */
    fun addAttachment(
        file: NoteFile
    ) {
        attachments[file.path] = file
        folders += generateSequence(seed = file.path.parent) { it.parent }.filterNot { it.isRoot }
    }

    override suspend fun scan(
        vaultPath: String
    ): Outcome<VaultListing, NotesError> {
        scanCount++
        val error = failure
            ?: return Outcome.Success(
                value = VaultListing(
                    folders = folders.toSet(),
                    files = files.values.toList(),
                    attachments = attachments.values.toList()
                )
            )
        return Outcome.Failure(error = error)
    }

    override suspend fun stat(
        vaultPath: String,
        path: RelativePath
    ): NoteFile? = files[path]

    override suspend fun statAttachment(
        vaultPath: String,
        path: RelativePath
    ): NoteFile? = attachments[path]
}
