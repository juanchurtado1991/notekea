package app.notekea.documents.presentation

sealed interface DocumentViewerIntent {
    /** Show the passage [range] of the document's text, asked for the [count]th time. */
    data class OnReveal(
        val range: IntRange,
        val count: Int
    ) : DocumentViewerIntent

    /** Page [page] (from 0) came into view: it is drawn if it is not yet. */
    data class OnPageShown(val page: Int) : DocumentViewerIntent

    data object OnOpenInApp : DocumentViewerIntent
}
