package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** The full text of notes, for the few changes Notekea makes to them on the user's request (updating links). */
interface NoteContentStore {
    /** The note's text exactly as stored (line endings and byte order mark included), or null if it cannot be read. */
    suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String?

    /** Replaces the note's text in one step, so a crash leaves either the old or the new text, never a mix. */
    suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError>
}
