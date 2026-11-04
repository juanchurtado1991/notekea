package app.notekea.core.presentation.platform

import java.awt.Desktop
import java.awt.desktop.QuitResponse

/**
 * The ways the system ends Notekea without closing its window, through [quit], so what is being typed is saved first:
 * macOS's Quit (Cmd+Q, the Dock, logging out), which otherwise calls `System.exit`, and a process told to stop
 * (SIGTERM, a Windows or Linux session ending), which only runs the JVM's shutdown hooks.
 */
class SystemQuit(
    private val quit: WindowQuit
) {
    /** Takes over the system's quit where the desktop offers it, and adds the shutdown hook. */
    fun install() {
        if (isQuitHandlerSupported()) {
            Desktop.getDesktop().setQuitHandler { _, response -> onQuit(response = response) }
        }
        Runtime.getRuntime().addShutdownHook(Thread(quit::finishBeforeShutdown))
    }

    /** The system asked to quit: it goes ahead once the guard is done. */
    fun onQuit(
        response: QuitResponse
    ) {
        quit.request(then = response::performQuit)
    }

    private fun isQuitHandlerSupported(): Boolean =
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)
}
