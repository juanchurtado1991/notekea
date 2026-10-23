package app.notekea

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

private const val WINDOW_TITLE = "Notekea"

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = WINDOW_TITLE
    ) {
        App()
    }
}
