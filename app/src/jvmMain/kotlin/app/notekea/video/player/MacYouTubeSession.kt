package app.notekea.video.player

import java.util.concurrent.Executors

/**
 * One YouTube video playing in the window on macOS: its web view ([MacWebView]) made, placed, hidden and closed in
 * the order asked, on a thread of its own, so the interface never waits for Cocoa's main thread and a tab closed
 * before its player was ready still closes it.
 */
internal class MacYouTubeSession(private val id: String) {
    private val worker = Executors.newSingleThreadExecutor { work ->
        Thread(work, THREAD_NAME).apply { isDaemon = true }
    }

    private var view: MacWebView? = null

    fun open() = worker.execute {
        view = YouTubePage.of(id = id)?.let {
            MacWebView.show(
                html = it,
                origin = YouTubePage.ORIGIN
            )
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
    }
}
