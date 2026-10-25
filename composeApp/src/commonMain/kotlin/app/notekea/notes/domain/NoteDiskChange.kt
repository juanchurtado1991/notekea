package app.notekea.notes.domain

/** What happened on disk to a note open in the editor, seen by [ObserveNoteOnDiskUseCase]. */
sealed interface NoteDiskChange {
    /** The file now holds [text]; it may be the editor's own save, which the editor recognizes and ignores. */
    data class Changed(val text: String) : NoteDiskChange

    /** The file is gone (deleted, moved or renamed outside the editor). */
    data object Deleted : NoteDiskChange
}
