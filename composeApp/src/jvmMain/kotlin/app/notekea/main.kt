package app.notekea

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.notekea.di.desktopModules
import org.koin.core.context.startKoin

private const val WINDOW_TITLE = "Notekea"

fun main() {
    startKoin { modules(desktopModules) }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = WINDOW_TITLE
        ) {
            App()
        }
    }
}
