package app.notekea

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.notekea.core.presentation.gallery.DesignSystemGallery
import app.notekea.di.desktopModules
import org.koin.core.context.startKoin

private const val GALLERY_HEIGHT_DP = 900
private const val GALLERY_PROPERTY = "notekea.gallery"
private const val GALLERY_TITLE = "Notekea — Design system gallery"
private const val GALLERY_WIDTH_DP = 1280
private const val WINDOW_TITLE = "Notekea"

fun main() {
    startKoin { modules(desktopModules) }
    val showGallery = System.getProperty(GALLERY_PROPERTY).toBoolean()
    application {
        if (showGallery) {
            Window(
                onCloseRequest = ::exitApplication,
                title = GALLERY_TITLE,
                state = rememberWindowState(width = GALLERY_WIDTH_DP.dp, height = GALLERY_HEIGHT_DP.dp)
            ) {
                DesignSystemGallery()
            }
        } else {
            Window(
                onCloseRequest = ::exitApplication,
                title = WINDOW_TITLE
            ) {
                App()
            }
        }
    }
}
