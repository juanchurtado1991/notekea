package app.notekea.video.player

import java.util.concurrent.Executors

/**
 * One YouTube video playing in the window on macOS: its web view ([MacWebView]) made, placed, hidden and closed in
 * the order asked, on a thread of its own, so the interface never waits for Cocoa's main thread and a tab closed
 * before its player was ready still closes it.
 */
internal class MacYouTubeSession(
    private val id: String,
    private val windowTitle: () -> String? = { null }
) {
    private val worker = Executors.newSingleThreadExecutor { work ->
        Thread(work, THREAD_NAME).apply { isDaemon = true }
    }

    private var view: MacWebView? = null

    fun open() = worker.execute {
        val page = YouTubePage.of(id = id) ?: return@execute

        // The window may not be there yet when the tab is restored at startup: it is tried again for a moment
        // rather than leaving an empty tab (the code review of 2026-11-13).
        repeat(times = TRIES) {
            if (view != null) return@repeat

            view = MacWebView.show(
                html = page,
                origin = YouTubePage.ORIGIN,
                windowTitle = windowTitle()
            )

            if (view == null) Thread.sleep(TRY_MILLIS)
        }
    }

    /** Puts the player over [place] of the window's content, [contentHeight] high, in points. */
    fun place(
        place: PlayerPlace,
        contentHeight: Double
    ) = worker.execute {
        view?.place(
            left = place.left,
            top = place.top,
            width = place.width,
            height = place.height,
            contentHeight = contentHeight
        )
    }

    fun hide(isHidden: Boolean) = worker.execute { view?.hide(isHidden = isHidden) }

    fun close() {
        worker.execute {
            view?.close()
            view = null
        }

        worker.shutdown()
    }

    private companion object {
        const val THREAD_NAME = "notekea-youtube"
        const val TRIES = 15
        const val TRY_MILLIS = 200L
    }
}
