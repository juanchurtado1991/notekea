package app.notekea.graph.presentation.viewmodel

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.NoteRewrite
import app.notekea.graph.domain.LinkNotesUseCase
import app.notekea.graph.presentation.model.GraphEffect
import app.notekea.graph.presentation.model.GraphState
import kotlinx.coroutines.Job

/** Linking the previewed note to another from the graph, and undoing the last link, one at a time. */
internal class GraphLinking(
    private val vaultPath: String,
    private val linkNotes: LinkNotesUseCase,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val host: GraphHost
) {
    private var linkJob: Job? = null
    private var lastLink: NoteRewrite? = null

    fun link(target: RelativePath) {
        val source = host.state.value.preview?.path ?: return
        if (linkJob?.isActive == true) return

        linkJob = host.launch {
            lastLink = linkNotes(vaultPath, source, target, host.notes.keys)
            val isLinked = lastLink != null

            if (isLinked) {
                host.graph = host.graph?.withLink(
                    one = source,
                    other = target
                )

                host.emit(effect = GraphEffect.NoteRewritten(path = source))
            }

            host.change {
                it.copy(
                    linked = target.takeIf { isLinked },
                    linkFailed = !isLinked
                )
            }
        }
    }

    fun undo() {
        val rewrite = lastLink ?: return
        val target = host.state.value.linked ?: return
        if (linkJob?.isActive == true) return

        linkJob = host.launch {
            val isUndone = applyLinkUpdates(
                vaultPath,
                listOf(rewrite.reversed())
            ).updated.isNotEmpty()

            if (isUndone) {
                host.graph = host.graph?.withoutLink(
                    one = rewrite.path,
                    other = target
                )

                host.emit(effect = GraphEffect.NoteRewritten(path = rewrite.path))
            }

            lastLink = null

            host.change {
                it.copy(
                    linked = null,
                    linkFailed = !isUndone
                )
            }
        }
    }
}
