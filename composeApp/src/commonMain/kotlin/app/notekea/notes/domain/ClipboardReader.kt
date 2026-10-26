package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** What the system clipboard holds, read when the user pastes into a note. */
interface ClipboardReader {
    /**
     * A copied image (a screenshot, an image copied from a browser) as PNG bytes, or null when there is none; a failure
     * when the clipboard offers an image it does not give ([NotesError.ClipboardImageUnreadable]), or one that needs a
     * missing system tool to read ([NotesError.ClipboardToolMissing]).
     */
    fun image(): Outcome<ByteArray?, NotesError>

    /** Copied text, or null when there is none. */
    fun text(): String?
}
