package app.notekea.video.player

import cn.enaium.webview.WindowSizeHint
import cn.enaium.webview.createWebview

/**
 * The YouTube player window: Notekea started again as a process of its own ([YouTubePlayerProcess]) that shows only
 * the system's web view (WebView2, WKWebView, WebKitGTK) on YouTube's player without tracking cookies, on its main
 * thread as macOS asks; its memory goes back to the system when it closes.
 */
object YouTubePlayerWindow {
    const val ADDRESS_VARIABLE = "NOTEKEA_YOUTUBE_PLAYER"
    const val TITLE_VARIABLE = "NOTEKEA_YOUTUBE_TITLE"
    private const val WIDTH = 960
    private const val HEIGHT = 540

    /** Shows the player when this process was started for it, until it closes; false when it was not. */
    fun runIfAsked(): Boolean {
        val address = System.getenv(ADDRESS_VARIABLE) ?: return false
        val title = System.getenv(TITLE_VARIABLE).orEmpty()

        createWebview(debug = false).use { window ->
            window.setTitle(title)
            window.setSize(WIDTH, HEIGHT, WindowSizeHint.NONE)
            window.navigate(address)
            window.run()
        }

        return true
    }
}
