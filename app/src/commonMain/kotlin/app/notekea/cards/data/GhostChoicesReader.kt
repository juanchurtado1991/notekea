package app.notekea.cards.data

import app.notekea.cards.domain.ChoicesReader
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException

/** [ChoicesReader] over Ghost: a reply that is not the choices' JSON is none. */
class GhostChoicesReader : ChoicesReader {
    override fun read(json: String): List<String>? = try {
        Ghost.deserialize<ChoicesDto>(json).wrong
    } catch (_: GhostJsonException) {
        null
    }
}
