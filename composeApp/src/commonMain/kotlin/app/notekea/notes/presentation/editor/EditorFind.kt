package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.TextRange

/** Find in note (`ROADMAP.md` 1.5): the matches of the query in the text, the current one selected in it. */
internal class EditorFind(
    private val host: EditorHost
) {
    private val noteSearch = NoteSearch()

    fun search(
        query: String
    ) {
        updateMatches(query = query, body = host.buffer.text.toString(), keepCurrent = false)
        selectCurrentMatch()
    }

    /** Finds [query] again; after an edit the current match stays the same one when it still exists. */
    fun updateMatches(
        query: String,
        body: String,
        keepCurrent: Boolean
    ) {
        val matches = noteSearch.matches(text = body, query = query)
        host.reduce { state ->
            val previous = state.find?.current?.takeIf { keepCurrent }
            val current = if (matches.isEmpty()) {
                null
            } else {
                (previous ?: 0).coerceAtMost(
                    maximumValue = matches.lastIndex
                )
            }
            state.copy(find = FindState(query = query, matches = matches, current = current))
        }
    }

    fun moveToMatch(
        step: Int
    ) {
        host.reduce { state ->
            val find = state.find ?: return@reduce state
            val count = find.matches.size
            if (count == 0) return@reduce state
            val next = ((find.current ?: 0) + step + count) % count
            state.copy(find = find.copy(current = next))
        }
        selectCurrentMatch()
    }

    fun close() {
        selectCurrentMatch()
        host.reduce { it.copy(find = null) }
    }

    /** Selects the current match in the text, so closing the bar leaves the cursor on it. */
    private fun selectCurrentMatch() {
        val find = host.state.value.find ?: return
        val match = find.current?.let { find.matches.getOrNull(index = it) } ?: return
        host.buffer.edit { selection = TextRange(start = match.first, end = match.last + 1) }
    }
}
