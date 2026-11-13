package app.notekea.graph.domain

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.LinkFinder
import app.notekea.core.notes.domain.links.LinkResolver
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * The vault's notes as a graph: each link that a click would follow to another note is one edge,
 * however many times two notes link each other. Texts come from [NoteTextCache], so only changed notes are read again.
 */
class BuildNoteGraphUseCase(
    private val noteTextCache: NoteTextCache,
    private val linkFinder: LinkFinder,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): NoteGraph {
        val texts = noteTextCache.textsOf(
            vaultPath = vaultPath,
            notes = notes
        )

        return withContext(context = dispatchers.default) {
            val paths = notes.map { it.file.path }
            val resolver = LinkResolver(notePaths = paths)
            val edges = LinkedHashSet<GraphEdge>()

            texts.forEach { (source, text) ->
                ensureActive()

                linkFinder.find(markdown = text.body).forEach { link ->
                    val target = resolver.closest(
                        link = link,
                        source = source
                    )

                    if (target != null && target != source) {
                        edges += GraphEdge.between(
                            one = source,
                            other = target
                        )
                    }
                }
            }

            val counts = countsOf(edges = edges)

            NoteGraph(
                nodes = paths.map {
                    GraphNode(
                        path = it,
                        links = counts[it] ?: 0
                    )
                },
                edges = edges.toList()
            )
        }
    }

    private fun countsOf(edges: Collection<GraphEdge>): Map<RelativePath, Int> {
        val counts = HashMap<RelativePath, Int>()

        edges.forEach { edge ->
            counts[edge.first] = (counts[edge.first] ?: 0) + 1
            counts[edge.second] = (counts[edge.second] ?: 0) + 1
        }

        return counts
    }
}
