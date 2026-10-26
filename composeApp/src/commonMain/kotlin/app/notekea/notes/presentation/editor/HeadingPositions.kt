package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * Where the outline's headings are in the editor's scrolled page, in pixels from its top (`ROADMAP.md` 1.9): in Edit
 * mode from the text's layout ([inText]), in Read mode from the rendered pieces that start with a heading
 * ([inReading]). Places are measured when asked for, with [pageTop], which turns a laid-out element into its distance
 * from the top of the page (null while the page is not placed), so a place is never one remembered too early.
 */
@Stable
internal class HeadingPositions(
    private val pageTop: (LayoutCoordinates) -> Float?
) {
    /** The top of the [Int]th heading in the text, or null when it is not laid out. */
    var inText: (Int) -> Float? by mutableStateOf(value = { null })

    /** The rendered pieces that start with a heading, by the heading's index in the outline. */
    val inReading = mutableStateMapOf<Int, LayoutCoordinates>()

    fun top(
        index: Int,
        isReading: Boolean
    ): Float? = if (isReading) inReading[index]?.let { pageTopOf(coordinates = it) } else inText(index)

    fun pageTopOf(
        coordinates: LayoutCoordinates
    ): Float? = coordinates.takeIf { it.isAttached }?.let(pageTop)
}
