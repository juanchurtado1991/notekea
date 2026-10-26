package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import app.notekea.notes.domain.OutlineHeading
import kotlinx.coroutines.channels.Channel

/**
 * What the editor and the outline beside it share while scrolling (`ROADMAP.md` 1.9): the [current] heading, the one
 * whose section is at the top of the editor's view, which the outline highlights; and the headings to scroll to,
 * which the outline asks for ([reveal]) and the editor shows. Both are about where the view is, so they live in the
 * screen, not in the ViewModel.
 */
@Stable
class OutlineTracker {
    /** The index of the heading the view is in, or [NONE] above the first heading. */
    var current by mutableIntStateOf(value = NONE)

    private val requests = Channel<OutlineHeading>(capacity = Channel.CONFLATED)

    /** Asks the editor to scroll so [heading] sits at the top of its view. */
    fun reveal(
        heading: OutlineHeading
    ) {
        requests.trySend(element = heading)
    }

    /** Runs [scroll] for every heading asked for, until the editor leaves the screen. */
    suspend fun onReveal(
        scroll: suspend (OutlineHeading) -> Unit
    ) {
        for (heading in requests) scroll(heading)
    }

    companion object {
        const val NONE = -1
    }
}
