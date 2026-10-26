package app.notekea.notes.domain

/** A file to attach to a note: content without a file yet (a pasted image), or a file on disk (dropped or picked). */
sealed interface AttachmentSource {
    /** [bytes] to save as a file named [name]. */
    class Content(
        val name: String,
        val bytes: ByteArray
    ) : AttachmentSource

    /** The file at [absolutePath], anywhere on the computer. */
    data class File(val absolutePath: String) : AttachmentSource
}
