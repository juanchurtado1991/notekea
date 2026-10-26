package app.notekea.notes.domain

/** The system's dialog to choose any file to attach to a note. */
interface AttachmentPicker {
    /** The chosen file's path on the computer, or null when the user cancels. */
    suspend fun pickFile(): String?
}
