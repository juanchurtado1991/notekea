package app.notekea

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.notekea.core.presentation.platform.FollowWindowBounds
import app.notekea.core.presentation.platform.WindowOpening
import app.notekea.core.presentation.platform.WindowSizes
import app.notekea.core.presentation.platform.WindowTitle
import app.notekea.core.presentation.theme.NotekeaBrand
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.UiZoom
import org.koin.core.Koin
import app.notekea.core.logging.data.FileLogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemAppearance
import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.core.presentation.gallery.DesignSystemGallery
import app.notekea.core.presentation.menu.AppMenuEntry
import app.notekea.core.presentation.menu.AppMenus
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.core.presentation.platform.awtFileDrop
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.core.presentation.platform.SystemAppMenu
import app.notekea.core.presentation.platform.SystemQuit
import app.notekea.core.presentation.platform.WindowCloseGuard
import app.notekea.core.presentation.platform.WindowCloseShortcuts
import app.notekea.core.presentation.platform.WindowDialogs
import app.notekea.core.presentation.platform.WindowDialogsHost
import app.notekea.core.presentation.platform.WindowQuit
import app.notekea.core.presentation.platform.keyShortcut
import app.notekea.core.presentation.theme.LocalAppearance
import app.notekea.core.presentation.theme.LocalSystemDarkTheme
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.di.desktopModules
import app.notekea.notes.presentation.tree.VaultScope
import app.notekea.notes.presentation.tree.VaultTreeScreen
import app.notekea.core.presentation.IntentTrail
import com.testbuddy.crash.Breadcrumbs
import com.testbuddy.crash.CrashNotice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.core.module.Module
import app.notekea.vault.presentation.welcome.WelcomeScreen
import io.github.vinceglb.filekit.FileKit
import java.awt.Cursor
import kotlinx.coroutines.delay
import org.koin.core.context.startKoin

private const val APP_ID = "Notekea"
private const val CRASH_TEST_DELAY_MILLIS = 3_000L
private const val CRASH_TEST_MESSAGE = "Crash test: NOTEKEA_CRASH_TEST is set"
private const val CRASH_TEST_ON = "1"
private const val CRASH_TEST_VARIABLE = "NOTEKEA_CRASH_TEST"
private const val GALLERY_HEIGHT_DP = 900
private const val GALLERY_PROPERTY = "notekea.gallery"
private const val GALLERY_TITLE = "Notekea — Design system gallery"
private const val GALLERY_WIDTH_DP = 1280
private const val SCREEN_MENU_BAR_PROPERTY = "apple.laf.useScreenMenuBar"

/**
 * Starts Notekea. Errors are kept as crash reports from the first moment (`CrashBuddy`, `test-buddy-crash`): any
 * thread, and the window, whose failure shows a notice instead of Compose's raw message; each report lists the last
 * steps ([IntentTrail]) and the machine. With `NOTEKEA_CRASH_TEST=1` the window fails on purpose a few seconds after
 * it opens, so the smoke test can check the crash report without a real bug.
 */
fun main() = runNotekea()

/**
 * Runs Notekea with the Koin [modules] given (the QA build adds its own) and, next to its window, the [extraWindows]
 * (the QA build's Test Buddy manager).
 */
@OptIn(ExperimentalComposeUiApi::class)
fun runNotekea(
    modules: List<Module> = desktopModules,
    extraWindows: @Composable ApplicationScope.() -> Unit = {}
) {
    System.setProperty(SCREEN_MENU_BAR_PROPERTY, true.toString())
    val koin = startKoin { modules(modules) }.koin
    val crashBuddy = crashBuddyOf(koin = koin, appName = APP_ID)
    crashBuddy.installThreadHandler()
    IntentTrail.listener = Breadcrumbs::record
    // What the app logs goes to the console and to the log file Copy diagnostics reads (qa/README.md, piece 6).
    Logger.setLogWriters(platformLogWriter(), koin.get<FileLogWriter>())
    FileKit.init(appId = APP_ID)
    val showGallery = System.getProperty(GALLERY_PROPERTY).toBoolean()
    val isCrashTest = koin.get<SystemEnvironment>().environmentVariable(name = CRASH_TEST_VARIABLE) == CRASH_TEST_ON
    val systemDarkTheme = koin.get<SystemAppearance>().observeDarkTheme()
    val appearances = koin.get<AppearanceStore>().observe()
    // Read before the first window, so it never opens in the default theme first.
    val keptAppearance = runBlocking { appearances.first() }
    val windowOpening = WindowSizes.system.opening(kept = runBlocking { koin.get<WindowBoundsStore>().bounds() })
    application {
        val isSystemDark by systemDarkTheme.collectAsState(initial = null)
        val appearance by appearances.collectAsState(initial = keptAppearance)
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides crashBuddy.windowExceptionHandlers(),
            LocalSystemDarkTheme provides isSystemDark,
            LocalAppearance provides appearance,
            LocalFileDrop provides ::awtFileDrop,
            LocalAppVersion provides koin.get<SystemEnvironment>().appVersion
        ) {
            if (showGallery) GalleryWindow() else NotekeaWindow(isCrashTest, windowOpening, zoomOf(koin = koin))
            CrashNotice(crashBuddy = crashBuddy, theme = { NotekeaTheme { it() } })
            extraWindows()
        }
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
 * to the design system, and the welcome flow shows the vault workspace once a vault is open. However Notekea ends
 * ([WindowQuit]), the [WindowCloseGuard] finishes first (the editor saving the note being typed).
 */
@Composable
private fun ApplicationScope.NotekeaWindow(
    isCrashTest: Boolean,
    opening: WindowOpening,
    zoom: WindowZoom
) {
    var vaultName by remember { mutableStateOf<String?>(value = null) }
    var activeTabName by remember { mutableStateOf<String?>(value = null) }
    val shortcutDispatcher = remember { ShortcutDispatcher() }
    val closeGuard = remember { WindowCloseGuard() }
    val dialogs = remember { WindowDialogs() }
    val quit = rememberSystemIntegration(closeGuard = closeGuard, dialogs = dialogs)
    val windowState = rememberWindowState(opening.placement, position = opening.position, size = opening.size)
    val onClose = { quit.request(then = ::exitApplication) }
    Window(
        onCloseRequest = onClose,
        title = WindowTitle.of(vaultName = vaultName, activeTabName = activeTabName),
        icon = rememberVectorPainter(image = NotekeaBrand.Logo),
        state = windowState,
        onPreviewKeyEvent = shortcutDispatcher::dispatch
    ) {
        NotekeaMenuBar(conventions = zoom.platformConventions, shortcuts = shortcutDispatcher, dialogs = dialogs)
        FollowWindowBounds(state = windowState)
        CompositionLocalProvider(
            LocalShortcutDispatcher provides shortcutDispatcher,
            LocalWindowCloseGuard provides closeGuard,
            LocalWindowDialogs provides dialogs,
            LocalResizePointerIcon provides PointerIcon(cursor = Cursor(Cursor.E_RESIZE_CURSOR))
        ) {
            if (isCrashTest) CrashTest()
            WindowCloseShortcuts(platformConventions = zoom.platformConventions, onClose = onClose)
            ZoomedContent(
                zoom = zoom,
                dialogs = dialogs,
                onVaultNameChange = { vaultName = it },
                onActiveTabChange = { activeTabName = it }
            )
        }
    }
}

/** The [WindowQuit] of the window, with the system's quit and its app menu leading to it and to the [dialogs]. */
@Composable
private fun rememberSystemIntegration(
    closeGuard: WindowCloseGuard,
    dialogs: WindowDialogs
): WindowQuit {
    val scope = rememberCoroutineScope()
    val quit = remember { WindowQuit(closeGuard = closeGuard, scope = scope) }
    LaunchedEffect(quit) {
        SystemQuit(quit = quit).install()
        SystemAppMenu(dialogs = dialogs).install()
    }
    return quit
}

@Composable
private fun ZoomedContent(
    zoom: WindowZoom,
    dialogs: WindowDialogs,
    onVaultNameChange: (String?) -> Unit,
    onActiveTabChange: (String?) -> Unit
) {
    UiZoom(
        platformConventions = zoom.platformConventions,
        loadScale = { zoom.getUiScale() },
        saveScale = { zoom.saveUiScale(scale = it) }
    ) {
        WindowContent(
            platformConventions = zoom.platformConventions,
            dialogs = dialogs,
            onVaultNameChange = onVaultNameChange,
            onActiveTabChange = onActiveTabChange
        )
    }
}

/** The menu bar ([AppMenus]): a command runs what its shortcut runs, and is disabled while no screen offers it. */
@Composable
private fun FrameWindowScope.NotekeaMenuBar(
    conventions: PlatformConventions,
    shortcuts: ShortcutDispatcher,
    dialogs: WindowDialogs
) {
    MenuBar {
        AppMenus.of(conventions = conventions).forEach { menu ->
            Menu(text = stringResource(resource = menu.title)) {
                menu.entries.forEach { entry ->
                    when (entry) {
                        is AppMenuEntry.Command -> Item(
                            text = stringResource(resource = entry.label),
                            enabled = shortcuts.isAvailable(shortcut = entry.shortcut),
                            shortcut = entry.shortcut.keyShortcut(),
                            onClick = { shortcuts.trigger(shortcut = entry.shortcut) }
                        )

                        is AppMenuEntry.OpensDialog -> Item(
                            text = stringResource(resource = entry.label),
                            shortcut = null,
                            onClick = { dialogs.shown = entry.dialog }
                        )

                        AppMenuEntry.Separator -> Separator()
                    }
                }
            }
        }
    }
}

/** What the window's zoom needs from Koin: the platform's shortcuts and where the zoom is kept. */
private fun zoomOf(
    koin: Koin
) = WindowZoom(platformConventions = koin.get(), getUiScale = koin.get(), saveUiScale = koin.get())

/**
 * The app: the welcome screen, then the open vault's workspace, with the window's [dialogs] over them;
 * [onVaultNameChange] reports the open vault's name and [onActiveTabChange] what its active tab shows.
 */
@Composable
private fun WindowContent(
    platformConventions: PlatformConventions,
    dialogs: WindowDialogs,
    onVaultNameChange: (String?) -> Unit,
    onActiveTabChange: (String?) -> Unit
) {
    App {
        WelcomeScreen(
            onVaultNameChange = onVaultNameChange,
            openedVaultContent = { vault, onSwitchVault ->
                VaultScope(vaultPath = vault.path) { close ->
                    VaultTreeScreen(
                        vaultPath = vault.path,
                        vaultName = vault.name,
                        onSwitchVault = { close(onSwitchVault) },
                        onActiveTabChange = onActiveTabChange
                    )
                }
            }
        )
        WindowDialogsHost(platformConventions = platformConventions, dialogs = dialogs)
    }
}

/** Fails the window on purpose after a few seconds, so the smoke test sees a crash report (`NOTEKEA_CRASH_TEST=1`). */
@Composable
private fun CrashTest() {
    var isTime by remember { mutableStateOf(value = false) }
    LaunchedEffect(Unit) {
        delay(timeMillis = CRASH_TEST_DELAY_MILLIS)
        isTime = true
    }
    check(value = !isTime) { CRASH_TEST_MESSAGE }
}
