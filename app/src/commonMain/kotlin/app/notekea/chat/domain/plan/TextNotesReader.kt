package app.notekea.chat.domain.plan

import app.notekea.chat.domain.prompt.TextNotesPrompt

/** Reads the notes the model writes from a pasted text (3.9, [TextNotesPrompt]). */
interface TextNotesReader {
    /** The notes as titles and texts, or null when [json] is not such a list. */
    fun readNotes(json: String): List<Pair<String, String>>?
}
