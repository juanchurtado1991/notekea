package app.notekea.video.player

import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

/**
 * The system's web view (WKWebView) inside Notekea's own window on macOS, over the part of it a tab gives
 * ([place]): the YouTube player in a tab (the developer's request of 2026-11-13), in this process, so no second app
 * shows in the Dock. Every call goes to Cocoa's main thread ([CocoaMainThread]).
 */
internal class MacWebView private constructor(
    private val view: Pointer,
    private val isParentFlipped: Boolean
) {
    /**
     * Puts the web view at [left], [top] of the window's content, [width] by [height], all in points; the content is
     * [contentHeight] high, which AppKit needs to count from the bottom.
     */
    fun place(
        left: Double,
        top: Double,
        width: Double,
        height: Double,
        contentHeight: Double
    ) = CocoaMainThread.run {
        val y = PlayerPlace.yOf(
            top = top,
            height = height,
            contentHeight = contentHeight,
            isFlipped = isParentFlipped
        )

        ObjC.tell(view, "setFrame:", CocoaRect(left, y, width, height))
    }

    /** Hides the web view under a dialog of the window, which it would otherwise cover, or shows it again. */
    fun hide(isHidden: Boolean) = CocoaMainThread.run {
        if (isHidden) yieldKeyboard()
        ObjC.tell(view, "setHidden:", if (isHidden) 1 else 0)
    }

    // The keyboard goes back to the window's own content when the player had it: hidden or gone, it kept the
    // keys, and the shortcuts did nothing until a click (the code review of 2026-11-13).
    private fun yieldKeyboard() {
        val window = ObjC.send(view, "window") ?: return
        val holder = ObjC.send(window, "firstResponder") ?: return

        val isView =
            ObjC.ask(holder, "respondsToSelector:", ObjC.selectorOf(name = "isDescendantOf:"))

        val isPlayers = isView && ObjC.ask(holder, "isDescendantOf:", view)
        if (!isPlayers) return

        ObjC.send(window, "contentView")?.let { ObjC.tell(window, "makeFirstResponder:", it) }
    }

    /** Stops the page and takes the web view out of the window. */
    fun close() = CocoaMainThread.run {
        yieldKeyboard()
        ObjC.tell(view, "stopLoading")
        ObjC.tell(view, "removeFromSuperview")
        ObjC.tell(view, "release")
    }

    companion object {
        private const val WEBKIT = "WebKit"
        private const val NO_USER_ACTION = 0L

        /**
         * A web view showing [html], as if it came from [origin] (what YouTube takes as the app's identity), in the
         * window titled [windowTitle], or else the one that has the keyboard; null with no window or no WebKit.
         */
        fun show(
            html: String,
            origin: String,
            windowTitle: String? = null
        ): MacWebView? {
            var shown: MacWebView? = null

            CocoaMainThread.run {
                shown = made(
                    html = html,
                    origin = origin,
                    windowTitle = windowTitle
                )
            }

            return shown
        }

        private fun made(
            html: String,
            origin: String,
            windowTitle: String?
        ): MacWebView? {
            NativeLibrary.getInstance(WEBKIT)
            val parent = contentView(title = windowTitle) ?: return null
            val configuration = configuration() ?: return null

            val allocated = ObjC.classOf(name = "WKWebView")?.let {
                ObjC.send(it, "alloc")
            } ?: return null

            val create = "initWithFrame:configuration:"
            val view = ObjC.send(allocated, create, CocoaRect(), configuration)
            // The view keeps its own copy: ours is let go either way, or each video played would leave one behind.
            ObjC.tell(configuration, "release")
            view ?: return null

            val url = ObjC
                .classOf(name = "NSURL")
                ?.let { ObjC.send(it, "URLWithString:", ObjC.stringOf(text = origin)) }

            ObjC.send(view, "loadHTMLString:baseURL:", ObjC.stringOf(text = html), url)
            ObjC.tell(parent, "addSubview:", view)

            return MacWebView(
                view = view,
                isParentFlipped = ObjC.ask(parent, "isFlipped")
            )
        }

        // The content of the window the person is in: the one with the keyboard, or else the app's main one.
        private fun contentView(title: String?): Pointer? {
            val app = ObjC
                .classOf(name = "NSApplication")
                ?.let { ObjC.send(it, "sharedApplication") } ?: return null

            // Notekea's own window, by its title: the one with the keyboard may be a panel of the system's or
            // another window of the program (the code review of 2026-11-13).
            val titled = title?.let { wanted ->
                ObjC
                    .send(app, "orderedWindows")
                    ?.let { ObjC.items(array = it) }
                    ?.firstOrNull {
                        ObjC.textOf(string = ObjC.send(it, "title")) == wanted
                    }
            }

            val window = titled
                ?: ObjC.send(app, "keyWindow")
                ?: ObjC.send(app, "mainWindow")
                ?: ObjC.send(app, "orderedWindows")?.let { ObjC.send(it, "firstObject") }
                ?: return null

            return ObjC.send(window, "contentView")
        }

        // A video plays at once, with no click in the page first, and the player's own full screen button works.
        private fun configuration(): Pointer? {
            val configuration = ObjC
                .classOf(name = "WKWebViewConfiguration")
                ?.let { ObjC.send(it, "new") } ?: return null

            ObjC.tell(configuration, "setMediaTypesRequiringUserActionForPlayback:", NO_USER_ACTION)

            // What the player's page stores (cookies, what was watched) lives only while it plays, never on disk.
            ObjC
                .classOf(name = "WKWebsiteDataStore")
                ?.let { ObjC.send(it, "nonPersistentDataStore") }
                ?.let { ObjC.tell(configuration, "setWebsiteDataStore:", it) }

            val preferences = ObjC.send(configuration, "preferences") ?: return configuration
            val fullscreen = "setElementFullscreenEnabled:"

            if (ObjC.ask(preferences, "respondsToSelector:", ObjC.selectorOf(name = fullscreen))) {
                ObjC.tell(preferences, fullscreen, 1)
            }

            return configuration
        }
    }
}
