package app.notekea.chat.domain.tool

import app.notekea.core.notes.domain.RelativePath

/**
 * What a turn read: the notes that held text (what a note it writes is written from), those that held none, and the
 * unread notes about the topic of one read that write the task's words ([related], `docs/agent-figures.md`).
 */
class TurnReads {
    private val held = mutableListOf<RelativePath>()
    private val empty = mutableListOf<String>()
    private val topic = mutableListOf<RelativePath>()

    /** The notes read so far that held text. */
    val read: List<RelativePath> get() = held.distinct()

    /** The notes read that held no text, when none read held any: what a note would be written blind from. */
    val blind: List<String> get() = empty
        .distinct()
        .takeIf { held.isEmpty() }
        .orEmpty()

    /** The notes about the topic of the ones read, not read yet. */
    val related: List<RelativePath> get() = topic.distinct().filterNot {
        it in held
    }

    /** Whether [note] was one of the topic's: its own topic is no task's (one step from what the task read). */
    fun isRelated(note: RelativePath): Boolean = note in topic

    fun relate(notes: List<RelativePath>) {
        topic += notes
    }

    fun add(note: RelativePath) {
        held += note
    }

    fun addEmpty(name: String) {
        empty += name
    }
}
