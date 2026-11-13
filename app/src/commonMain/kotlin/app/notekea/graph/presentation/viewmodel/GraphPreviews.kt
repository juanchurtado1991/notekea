package app.notekea.graph.presentation.viewmodel

import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.graph.domain.GraphNode
import app.notekea.graph.presentation.model.GraphLine
import app.notekea.graph.presentation.model.GraphPreview

/** What the inspector shows of a note: a few lines of plain text from its body, and what the map joins it to. */
internal class GraphPreviews(
    private val vaultPath: String,
    private val noteTextCache: NoteTextCache,
    private val markdownLineStripper: MarkdownLineStripper
) {
    suspend fun of(
        entry: NoteEntry,
        lines: List<GraphLine>,
        suggested: List<RelativePath>
    ): GraphPreview {
        val path = entry.file.path

        val body = noteTextCache
            .textsOf(
                vaultPath = vaultPath,
                notes = listOf(entry)
            )[path]
            ?.body
            .orEmpty()

        return GraphPreview(
            path = path,
            name = path.nameWithoutExtension,
            folder = GraphNode(
                path = path,
                links = 0
            ).folder,
            snippet = snippetOf(body = body),
            connected = connectedTo(
                path = path,
                lines = lines
            ),
            suggested = suggested
        )
    }

    private fun snippetOf(body: String): String = body
        .lineSequence()
        .filter {
            it.isNotBlank() && !it.startsWith(prefix = HEADING_MARK)
        }
        .take(n = SNIPPET_LINES)
        .joinToString(separator = " ") {
            markdownLineStripper.strip(
                line = it,
                ranges = emptyList()
            ).text
        }
        .take(n = MAX_SNIPPET)

    private companion object {
        const val HEADING_MARK = "#"
        const val MAX_SNIPPET = 220
        const val SNIPPET_LINES = 4
    }
}
