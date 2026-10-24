package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** A vault on paper (notes bring their folders along): [files] and [folders] are what a scan finds, [failure] makes every scan fail. */
class FakeNoteScanner : NoteScanner {
    val files = mutableMapOf<RelativePath, NoteFile>()
    val folders = mutableSetOf<RelativePath>()
    var failure: NotesError? = null
    var scanCount = 0

    /** Adds the note and, as a real folder would hold it, its parent folders. */
    fun addNote(
        file: NoteFile
    ) {
        files[file.path] = file
        folders += generateSequence(seed = file.path.parent) { it.parent }.filterNot { it.isRoot }
    }

    override suspend fun scan(
        vaultPath: String
    ): Outcome<VaultListing, NotesError> {
        scanCount++
        val error = failure
            ?: return Outcome.Success(
                value = VaultListing(folders = folders.toSet(), files = files.values.toList())
            )
        return Outcome.Failure(error = error)
    }

    override suspend fun stat(
        vaultPath: String,
        path: RelativePath
    ): NoteFile? = files[path]
}
