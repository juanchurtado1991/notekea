package app.notekea.chat.domain.tool

import app.notekea.core.notes.domain.RelativePath

/**
 * What a turn read: the notes that held text (what a note it writes is written from), those that held none.
 */
class TurnReads {
    private val held = mutableListOf<RelativePath>()
    private val empty = mutableListOf<String>()

    /** The notes read so far that held text. */
    val read: List<RelativePath> get() = held.distinct()

    /** The notes read that held no text, when none read held any: what a note would be written blind from. */
    val blind: List<String> get() = empty
        .distinct()
        .takeIf { held.isEmpty() }
        .orEmpty()

    fun add(note: RelativePath) {
        held += note
    }

    fun addEmpty(name: String) {
        empty += name
    }
}
