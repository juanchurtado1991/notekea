package app.notekea.notes.domain

/** What the system clipboard holds, read when the user pastes into a note. */
interface ClipboardReader {
    /** A copied image (a screenshot, an image copied from a browser) as PNG bytes, or null when there is none. */
    fun image(): ByteArray?

    /** Copied text, or null when there is none. */
    fun text(): String?
}
