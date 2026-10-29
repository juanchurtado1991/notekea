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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.Alignment
import app.notekea.core.presentation.platform.WindowSizes
import app.notekea.core.presentation.theme.NotekeaBrand
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.UiZoom
import org.koin.core.Koin
import app.notekea.core.crash.domain.RecordedCrash
import app.notekea.core.crash.presentation.CrashNoticeContent
import app.notekea.core.crash.presentation.CrashReportActions
import app.notekea.core.crash.presentation.CrashReporter
import app.notekea.core.logging.data.FileLogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemAppearance
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.core.presentation.gallery.DesignSystemGallery
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.awtFileDrop
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.core.presentation.platform.WindowCloseGuard
import app.notekea.core.presentation.theme.LocalSystemDarkTheme
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.di.desktopModules
import app.notekea.notes.presentation.tree.VaultScope
import app.notekea.notes.presentation.tree.VaultTreeScreen
import app.notekea.resources.Res
import app.notekea.resources.crash_title
import app.notekea.vault.presentation.welcome.WelcomeScreen
import io.github.vinceglb.filekit.FileKit
import java.awt.Cursor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin

private const val APP_ID = "Notekea"
private const val CRASH_DIALOG_HEIGHT_DP = 360
private const val CRASH_DIALOG_WIDTH_DP = 600
private const val CRASH_TEST_DELAY_MILLIS = 3_000L
private const val CRASH_TEST_MESSAGE = "Crash test: NOTEKEA_CRASH_TEST is set"
private const val CRASH_TEST_ON = "1"
private const val CRASH_TEST_VARIABLE = "NOTEKEA_CRASH_TEST"
private const val GALLERY_HEIGHT_DP = 900
private const val GALLERY_PROPERTY = "notekea.gallery"
private const val GALLERY_TITLE = "Notekea — Design system gallery"
private const val GALLERY_WIDTH_DP = 1280
private const val TITLE_SEPARATOR = " — "
private const val WINDOW_TITLE = "Notekea"

/**
 * Starts Notekea. Errors are kept as crash reports from the first moment ([CrashReporter]): any thread, and the window,
 * whose failure shows a notice instead of Compose's raw message. With `NOTEKEA_CRASH_TEST=1` the window fails on
 * purpose a few seconds after it opens, so the smoke test can check the crash report without a real bug.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val koin = startKoin { modules(desktopModules) }.koin
    val crashReporter = koin.get<CrashReporter>()
    crashReporter.installThreadHandler()
    // What the app logs goes to the console and to the log file Copy diagnostics reads (qa/README.md, piece 6).
    Logger.setLogWriters(platformLogWriter(), koin.get<FileLogWriter>())
    FileKit.init(appId = APP_ID)
    val showGallery = System.getProperty(GALLERY_PROPERTY).toBoolean()
    val isCrashTest = koin.get<SystemEnvironment>().environmentVariable(name = CRASH_TEST_VARIABLE) == CRASH_TEST_ON
    val crashActions = koin.get<CrashReportActions>()
    val buttonOrder = koin.get<PlatformConventions>().dialogButtonOrder
    val systemDarkTheme = koin.get<SystemAppearance>().observeDarkTheme()
    application {
        val isSystemDark by systemDarkTheme.collectAsState(initial = null)
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides crashReporter.windowExceptionHandlers(),
            LocalSystemDarkTheme provides isSystemDark,
            LocalFileDrop provides ::awtFileDrop,
            LocalAppVersion provides koin.get<SystemEnvironment>().appVersion
        ) {
            if (showGallery) GalleryWindow() else NotekeaWindow(isCrashTest = isCrashTest, zoom = zoomOf(koin = koin))
            val crash by crashReporter.crash.collectAsState()
            crash?.let {
                CrashDialog(
                    crash = it,
                    buttonOrder = buttonOrder,
                    actions = crashActions,
                    onClose = crashReporter::close
                )
            }
        }
    }
}

/**
 * The crash notice in its own dialog window, a separate scene that still works when the main window is broken, with
 * the app's theme. Closing it, by its button or the window's close box, closes the failed window the usual way.
 */
@Composable
private fun CrashDialog(
    crash: RecordedCrash,
    buttonOrder: DialogButtonOrder,
    actions: CrashReportActions,
    onClose: () -> Unit
) {
    DialogWindow(
        onCloseRequest = onClose,
        state = rememberDialogState(
            size = DpSize(width = CRASH_DIALOG_WIDTH_DP.dp, height = CRASH_DIALOG_HEIGHT_DP.dp)
        ),
        title = stringResource(resource = Res.string.crash_title),
        resizable = false
    ) {
        NotekeaTheme {
            CrashNoticeContent(
                crash = crash,
                buttonOrder = buttonOrder,
                onCopy = { actions.copy(text = crash.text) },
                onReport = { actions.report(text = crash.text) },
                onClose = onClose
            )
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
 * to the design system, and the welcome flow shows the vault workspace once a vault is open. Closing the window first
 * lets the [WindowCloseGuard] finish (the editor saving the note being typed), then quits.
 */
@Composable
private fun ApplicationScope.NotekeaWindow(
    isCrashTest: Boolean,
    zoom: WindowZoom
) {
    var vaultName by remember { mutableStateOf<String?>(value = null) }
    val shortcutDispatcher = remember { ShortcutDispatcher() }
    val closeGuard = remember { WindowCloseGuard() }
    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(value = false) }
    val windowState = rememberWindowState(
        size = WindowSizes.system.initial,
        position = WindowPosition.Aligned(alignment = Alignment.Center)
    )
    Window(
        onCloseRequest = {
            if (!isClosing) {
                isClosing = true
                scope.launch {
                    closeGuard.beforeClose()
                    exitApplication()
                }
            }
        },
        title = windowTitle(vaultName = vaultName),
        icon = rememberVectorPainter(image = NotekeaBrand.Logo),
        state = windowState,
        onPreviewKeyEvent = shortcutDispatcher::dispatch
    ) {
        CompositionLocalProvider(
            LocalShortcutDispatcher provides shortcutDispatcher,
            LocalWindowCloseGuard provides closeGuard,
            LocalResizePointerIcon provides PointerIcon(cursor = Cursor(Cursor.E_RESIZE_CURSOR))
        ) {
            if (isCrashTest) CrashTest()
            UiZoom(
                platformConventions = zoom.platformConventions,
                loadScale = { zoom.getUiScale() },
                saveScale = { zoom.saveUiScale(scale = it) }
            ) { WindowContent(onVaultNameChange = { vaultName = it }) }
        }
    }
}

/** What the window's zoom needs from Koin: the platform's shortcuts and where the zoom is kept. */
private fun zoomOf(
    koin: Koin
) = WindowZoom(platformConventions = koin.get(), getUiScale = koin.get(), saveUiScale = koin.get())

/** The app: the welcome screen, then the open vault's workspace; [onVaultNameChange] reports the open vault's name. */
@Composable
private fun WindowContent(
    onVaultNameChange: (String?) -> Unit
) {
    App {
        WelcomeScreen(
            onVaultNameChange = onVaultNameChange,
            openedVaultContent = { vault, onSwitchVault ->
                VaultScope(vaultPath = vault.path) { close ->
                    VaultTreeScreen(
                        vaultPath = vault.path,
                        vaultName = vault.name,
                        onSwitchVault = { close(onSwitchVault) }
                    )
                }
            }
        )
    }
}

private fun windowTitle(
    vaultName: String?
): String = vaultName?.let { it + TITLE_SEPARATOR + WINDOW_TITLE } ?: WINDOW_TITLE

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
