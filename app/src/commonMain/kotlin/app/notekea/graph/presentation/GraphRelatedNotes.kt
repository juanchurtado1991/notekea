package app.notekea.graph.presentation

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FindRelatedNotesUseCase
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.graph.domain.NoteGraph

/**
 * The notes close in meaning to one note, for the map and the inspector, which both ask: the last answer is remembered
 * until [forget], as finding them runs the model. Only what is at least [MIN_CLOSENESS] close counts.
 */
internal class GraphRelatedNotes(
    private val vaultPath: String,
    private val findRelatedNotes: FindRelatedNotesUseCase,
    private val noteTextCache: NoteTextCache
) {
    private var remembered: Pair<RelativePath, List<RelativePath>>? = null

    suspend fun of(
        note: RelativePath,
        notes: Map<RelativePath, NoteEntry>
    ): List<RelativePath> {
        remembered?.takeIf { it.first == note }?.let { return it.second }
        val entry = notes[note] ?: return emptyList()
        val text = noteTextCache.textsOf(vaultPath = vaultPath, notes = listOf(entry))[note]?.body
            ?: return emptyList()
        val found = findRelatedNotes(vaultPath, note, text, notes.keys) as? Outcome.Success ?: return emptyList()
        val close = found.value.related.filter { it.score >= MIN_CLOSENESS }.map { it.path }
        remembered = note to close
        return close
    }

    /** Those on [graph] that no link joins to [note] yet. */
    suspend fun notLinked(
        note: RelativePath,
        graph: NoteGraph,
        notes: Map<RelativePath, NoteEntry>
    ): List<RelativePath> {
        val joined = graph.neighbors()[note].orEmpty().toSet()
        val paths = graph.nodes.mapTo(destination = HashSet()) { it.path }
        return of(note = note, notes = notes).filter { it in paths && it !in joined }
    }

    fun forget() {
        remembered = null
    }

    private companion object {
        const val MIN_CLOSENESS = 0.82f
    }
}
