package app.notekea

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.notekea.core.presentation.gallery.DesignSystemGallery
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.di.desktopModules
import app.notekea.notes.presentation.tree.VaultTreeScreen
import app.notekea.vault.presentation.welcome.WelcomeScreen
import io.github.vinceglb.filekit.FileKit
import org.koin.core.context.startKoin
import java.awt.Cursor

private const val APP_ID = "Notekea"
private const val GALLERY_HEIGHT_DP = 900
private const val GALLERY_PROPERTY = "notekea.gallery"
private const val GALLERY_TITLE = "Notekea — Design system gallery"
private const val GALLERY_WIDTH_DP = 1280
private const val TITLE_SEPARATOR = " — "
private const val WINDOW_HEIGHT_DP = 720
private const val WINDOW_TITLE = "Notekea"
private const val WINDOW_WIDTH_DP = 1080

fun main() {
    startKoin { modules(desktopModules) }
    FileKit.init(appId = APP_ID)
    val showGallery = System.getProperty(GALLERY_PROPERTY).toBoolean()
    application {
        if (showGallery) GalleryWindow() else NotekeaWindow()
    }
}

@Composable
private fun ApplicationScope.GalleryWindow() {
    Window(
        onCloseRequest = ::exitApplication,
        title = GALLERY_TITLE,
        state = rememberWindowState(width = GALLERY_WIDTH_DP.dp, height = GALLERY_HEIGHT_DP.dp)
    ) {
        DesignSystemGallery()
    }
}

/**
 * The app window. Every key event goes through the [ShortcutDispatcher] first, the desktop resize cursor is provided
 * to the design system, and the welcome flow shows the vault workspace once a vault is open.
 */
@Composable
private fun ApplicationScope.NotekeaWindow() {
    var title by remember { mutableStateOf(value = WINDOW_TITLE) }
    val shortcutDispatcher = remember { ShortcutDispatcher() }
    Window(
        onCloseRequest = ::exitApplication,
        title = title,
        state = rememberWindowState(width = WINDOW_WIDTH_DP.dp, height = WINDOW_HEIGHT_DP.dp),
        onPreviewKeyEvent = shortcutDispatcher::dispatch
    ) {
        CompositionLocalProvider(
            LocalShortcutDispatcher provides shortcutDispatcher,
            LocalResizePointerIcon provides PointerIcon(cursor = Cursor(Cursor.E_RESIZE_CURSOR))
        ) {
            App {
                WelcomeScreen(
                    onVaultNameChange = { name -> title = windowTitle(vaultName = name) },
                    openedVaultContent = { vault, onSwitchVault ->
                        VaultTreeScreen(vaultPath = vault.path, vaultName = vault.name, onSwitchVault = onSwitchVault)
                    }
                )
            }
        }
    }
}

private fun windowTitle(
    vaultName: String?
): String = vaultName?.let { it + TITLE_SEPARATOR + WINDOW_TITLE } ?: WINDOW_TITLE
