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
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.Alignment
import app.notekea.core.presentation.platform.WindowSizes
import app.notekea.core.presentation.theme.NotekeaBrand
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.UiZoom
import org.koin.core.Koin
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
import app.notekea.resources.crash_close
import app.notekea.resources.crash_copied
import app.notekea.resources.crash_copy
import app.notekea.resources.crash_message
import app.notekea.resources.crash_report_github
import app.notekea.resources.crash_report_not_saved
import app.notekea.resources.crash_report_saved
import app.notekea.resources.crash_title
import app.notekea.core.presentation.IntentTrail
import com.testbuddy.crash.Breadcrumbs
import com.testbuddy.crash.CrashBuddy
import com.testbuddy.crash.CrashConfig
import com.testbuddy.crash.CrashNotice
import com.testbuddy.crash.CrashTexts
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.koin.core.module.Module
import java.nio.file.Path
import app.notekea.vault.presentation.welcome.WelcomeScreen
import io.github.vinceglb.filekit.FileKit
import java.awt.Cursor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin

private const val APP_ID = "Notekea"
private const val CRASH_FOLDER = "crashes"
private const val CRASH_LOG_TAG = "CrashBuddy"
private const val CRASH_MESSAGE = "Unexpected error, kept as a crash report"
private const val ISSUE_FORM = "https://github.com/juanchurtado1991/notekea/issues/new?template=crash_report.yml"
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
 * Starts Notekea. Errors are kept as crash reports from the first moment ([CrashBuddy], `test-buddy-crash`): any
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
    val koin = startKoin { modules(modules) }.koin
    val crashBuddy = crashBuddyOf(koin = koin)
    crashBuddy.installThreadHandler()
    IntentTrail.listener = Breadcrumbs::record
    // What the app logs goes to the console and to the log file Copy diagnostics reads (qa/README.md, piece 6).
    Logger.setLogWriters(platformLogWriter(), koin.get<FileLogWriter>())
    FileKit.init(appId = APP_ID)
    val showGallery = System.getProperty(GALLERY_PROPERTY).toBoolean()
    val isCrashTest = koin.get<SystemEnvironment>().environmentVariable(name = CRASH_TEST_VARIABLE) == CRASH_TEST_ON
    val systemDarkTheme = koin.get<SystemAppearance>().observeDarkTheme()
    application {
        val isSystemDark by systemDarkTheme.collectAsState(initial = null)
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides crashBuddy.windowExceptionHandlers(),
            LocalSystemDarkTheme provides isSystemDark,
            LocalFileDrop provides ::awtFileDrop,
            LocalAppVersion provides koin.get<SystemEnvironment>().appVersion
        ) {
            if (showGallery) GalleryWindow() else NotekeaWindow(isCrashTest = isCrashTest, zoom = zoomOf(koin = koin))
            CrashNotice(crashBuddy = crashBuddy, theme = { NotekeaTheme { it() } })
            extraWindows()
        }
    }
}

/** Notekea's crash reports: in the app data folder's `crashes`, with Notekea's texts and its GitHub form. */
private fun crashBuddyOf(
    koin: Koin
): CrashBuddy {
    val conventions = koin.get<PlatformConventions>()
    val logger = Logger.withTag(tag = CRASH_LOG_TAG)
    return CrashBuddy(
        config = CrashConfig(
            appName = APP_ID,
            version = koin.get<SystemEnvironment>().appVersion,
            reportFolder = Path.of(conventions.appDataDirectory, CRASH_FOLDER),
            issueFormUrl = ISSUE_FORM,
            texts = runBlocking { crashTexts() },
            isCloseLast = conventions.dialogButtonOrder == DialogButtonOrder.CANCEL_THEN_PRIMARY
        ),
        onError = { logger.e(throwable = it) { CRASH_MESSAGE } }
    )
}

private suspend fun crashTexts(): CrashTexts = CrashTexts(
    title = getString(resource = Res.string.crash_title),
    message = getString(resource = Res.string.crash_message),
    saved = getString(resource = Res.string.crash_report_saved),
    notSaved = getString(resource = Res.string.crash_report_not_saved),
    copy = getString(resource = Res.string.crash_copy),
    copied = getString(resource = Res.string.crash_copied),
    report = getString(resource = Res.string.crash_report_github),
    close = getString(resource = Res.string.crash_close)
)

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
