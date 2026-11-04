package app.notekea.core.notes.domain

/**
 * What a note's file keeps besides its text and a save must not reset: a save replaces the file with a new one, and
 * Okio has no way to carry these over.
 */
interface NoteFileAttributes {
    /**
     * Gives the file at [targetPath] the permissions and the creation time of the one at [sourcePath], as far as the
     * system allows. Nothing happens when there is no such file (a new note), and it never fails a save.
     */
    fun copy(
        sourcePath: String,
        targetPath: String
    )
}
