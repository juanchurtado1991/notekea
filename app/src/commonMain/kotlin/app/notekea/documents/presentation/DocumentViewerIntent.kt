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

    /**
     * Its tab left the screen (another tab, or closed): the drawn pages and the open PDF are let go, as the tab's
     * ViewModel stays while the vault is open; they are drawn again when it comes back (memory leak hunt, 2026-11-11).
     */
    data object OnHidden : DocumentViewerIntent
}
