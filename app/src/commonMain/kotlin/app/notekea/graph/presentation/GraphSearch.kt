package app.notekea.graph.presentation

import app.notekea.core.notes.domain.FuzzyMatcher

/** Find in the graph: the notes whose names match the query, and stepping from one to the next, centered on it. */
internal class GraphSearch(
    private val fuzzyMatcher: FuzzyMatcher,
    private val host: GraphHost
) {
    fun close() {
        host.reduce {
            it.copy(
                isSearching = false,
                query = "",
                matches = emptyList(),
                matchIndex = -1
            )
        }
    }

    fun changeQuery(query: String) {
        host.reduce {
            withMatches(
                state = it.copy(
                    query = query,
                    matchIndex = -1
                )
            )
        }
    }

    fun withMatches(state: GraphState): GraphState {
        val paths = host
            .graph
            ?.withoutFolders(folders = state.excludedFolders)
            ?.nodes
            ?.map { it.path }
            .orEmpty()

        val matches = matchesOf(
            query = state.query,
            paths = paths,
            matcher = fuzzyMatcher
        )

        val matchIndex = state.matchIndex.coerceAtMost(maximumValue = matches.lastIndex)

        return state.copy(
            matches = matches,
            matchIndex = matchIndex
        )
    }

    fun step(isForward: Boolean) {
        val current = host.state.value
        val count = current.matches.size
        if (count == 0) return

        val index = when {
            current.matchIndex >= 0 -> (current.matchIndex + if (isForward) 1 else count - 1) %
                count

            isForward -> 0

            else -> count - 1
        }

        val path = current.matches[index]

        host.change {
            it.copy(
                scope = GraphScope.THIS_NOTE,
                center = path,
                selected = path,
                matchIndex = index,
                reveals = it.reveals + 1,
                linked = null,
                linkFailed = false
            )
        }
    }
}
