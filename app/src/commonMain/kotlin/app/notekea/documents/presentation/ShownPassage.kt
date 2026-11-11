package app.notekea.documents.presentation

import app.notekea.documents.domain.PageMark

/**
 * The passage asked for ([range] of the document's text), the [page] of a PDF it is on (from 0) and its [marks] there;
 * [count] tells the same passage asked again, to scroll to it again.
 */
data class ShownPassage(
    val range: IntRange,
    val marks: List<PageMark>,
    val count: Int,
    val page: Int?
)
