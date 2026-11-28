package app.notekea.chat.data

import app.notekea.chat.domain.plan.TextNotesReader
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException

/** [TextNotesReader] over the model's JSON: a note without a title or a text is left out, a reply not JSON is null. */
class GhostTextNotesReader : TextNotesReader {
    override fun readNotes(json: String): List<Pair<String, String>>? {
        val dto = try {
            Ghost.deserialize<TextNotesDto>(json)
        } catch (_: GhostJsonException) {
            return null
        }

        return dto
            .notes
            .filter {
                it.title.isNotBlank() && it.text.isNotBlank()
            }
            .map {
                it.title.trim() to it.text.trim()
            }
    }
}
