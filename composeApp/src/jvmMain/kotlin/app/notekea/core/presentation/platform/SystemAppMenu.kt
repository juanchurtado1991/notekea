package app.notekea.core.presentation.platform

import java.awt.Desktop

/** macOS's own app menu: its About and Settings open Notekea's [dialogs]. Elsewhere the desktop offers neither. */
class SystemAppMenu(
    private val dialogs: WindowDialogs
) {
    fun install() {
        if (!Desktop.isDesktopSupported()) return
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler { dialogs.shown = WindowDialog.ABOUT }
        }
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler { dialogs.shown = WindowDialog.SETTINGS }
        }
    }
}
