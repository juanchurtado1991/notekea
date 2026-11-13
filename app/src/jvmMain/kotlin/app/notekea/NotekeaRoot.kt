package app.notekea

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.ai.presentation.ModelManagerScreen
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.presentation.designsystem.theme.LocalAppearance
import app.notekea.core.presentation.designsystem.theme.LocalSystemAccent
import app.notekea.core.presentation.designsystem.theme.LocalSystemDarkTheme
import app.notekea.core.presentation.designsystem.theme.LocalSystemReduceMotion
import app.notekea.core.presentation.platform.AppEnvironment
import app.notekea.core.presentation.platform.AppLocale
import app.notekea.core.presentation.platform.AppVersionDefaults
import app.notekea.core.presentation.platform.DesktopVideoPlayer
import app.notekea.core.presentation.platform.LocalAppPreferences
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.LocalVideoPlayer
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.core.presentation.platform.LocalYouTubePlayer
import app.notekea.core.presentation.platform.UiZoom
import app.notekea.core.presentation.platform.WindowCloseShortcuts
import app.notekea.core.presentation.platform.WindowHost
import app.notekea.core.presentation.platform.awtFileDrop
import app.notekea.shell.presentation.VaultWindow
import app.notekea.shell.presentation.onboarding.OnboardingHost
import app.notekea.shell.presentation.workspace.WindowDialogsHost
import app.notekea.sync.presentation.SyncSettingsScreen
import app.notekea.tree.presentation.view.VaultScope
import app.notekea.vault.presentation.welcome.WelcomeContent
import app.notekea.vault.presentation.welcome.WelcomePreviewData
import app.notekea.vault.presentation.welcome.WelcomeScreen
import app.notekea.video.player.YouTubePlayerProcess
import java.awt.Cursor

private const val PREVIEW_TEXT = "Every window of the app"

/*
 * Static, so a new language recomposes every scope under it, windows and dialogs included, keeping what they remember:
 * Compose's resources read the locale only when they recompose.
 */
private val LocalLanguage = staticCompositionLocalOf { AppLanguage.SYSTEM }

/**
 * What every window of the app reads, around them: the system's dark theme and reduced motion, the kept appearance
 * ([initialAppearance] until the store answers), the desktop's file drop and resize cursor, and the app's version. The
 * app and the Auto-Test's cases both start here, so a case runs the same app.
 */
@Composable
fun NotekeaAppLocals(
    environment: AppEnvironment,
    initialAppearance: Appearance,
    content: @Composable () -> Unit
) {
    val darkTheme = remember(environment) { environment.systemAppearance.observeDarkTheme() }

    val appearances = remember(environment) { environment.appearanceStore.observe() }
    val preferenceChanges = remember(environment) { environment.preferencesStore.observe() }
    val preferences by preferenceChanges.collectAsState(initial = AppPreferences())

    val isSystemDark by darkTheme.collectAsState(initial = null)
    val appearance by appearances.collectAsState(initial = initialAppearance)

    val isMotionReduced by produceState(initialValue = false, environment) {
        value = environment.systemMotion.isReduced()
    }

    val systemAccent by produceState<Color?>(initialValue = null, environment) {
        value = environment
            .systemAccent
            .read()
            ?.let { Color(color = it) }
    }

    remember(preferences.language) { AppLocale.apply(language = preferences.language) }

    CompositionLocalProvider(
        LocalAppPreferences provides preferences,
        LocalLanguage provides preferences.language,
        LocalSystemAccent provides systemAccent
    ) {
        AppLocals(
            isMotionReduced = isMotionReduced,
            appearance = appearance,
            appVersion = environment.systemEnvironment.appVersion,
            content = content,
            isSystemDark = isSystemDark
        )
    }
}

@Composable
private fun AppLocals(
    isMotionReduced: Boolean,
    appearance: Appearance,
    appVersion: String,
    content: @Composable () -> Unit,
    isSystemDark: Boolean?
) {
    val youTube = remember { YouTubePlayerProcess() }

    DisposableEffect(youTube) {
        onDispose { youTube.close() }
    }

    CompositionLocalProvider(
        LocalYouTubePlayer provides youTube::play,
        LocalSystemDarkTheme provides isSystemDark,
        LocalSystemReduceMotion provides isMotionReduced,
        LocalAppearance provides appearance,
        LocalFileDrop provides ::awtFileDrop,
        LocalVideoPlayer provides { location, onClose, onCannotPlay, modifier ->
            DesktopVideoPlayer(
                location = location,
                onClose = onClose,
                onCannotPlay = onCannotPlay,
                modifier = modifier
            )
        },
        LocalAppVersion provides appVersion,
        LocalResizePointerIcon provides PointerIcon(cursor = Cursor(Cursor.E_RESIZE_CURSOR)),
        content = content
    )
}

/**
 * The whole window's content, in the real window and in a Auto-Test case, which differ only in their [host] and in what
 * closing the window does ([onClose]): the shortcuts and the close guard, the zoom, the welcome screen, then the open
 * vault's workspace, and the window's dialogs over them.
 */
@Composable
fun NotekeaRoot(
    host: WindowHost,
    onClose: () -> Unit
) {
    CompositionLocalProvider(
        LocalShortcutDispatcher provides host.shortcuts,
        LocalWindowCloseGuard provides host.closeGuard,
        LocalWindowDialogs provides host.dialogs
    ) {
        WindowCloseShortcuts(
            platformConventions = host.conventions,
            onClose = onClose
        )

        UiZoom(platformConventions = host.conventions) { WindowContent(host = host) }
    }
}

@Composable
private fun WindowContent(host: WindowHost) {
    // Settings → Sync is about the open vault, and Settings opens over the welcome screen too.
    var openVault by remember { mutableStateOf<String?>(value = null) }

    App {
        WelcomeScreen(
            onVaultNameChange = { host.vaultName = it },
            openedVaultContent = { vault, onSwitchVault ->
                DisposableEffect(vault.path) {
                    openVault = vault.path

                    onDispose {
                        if (openVault == vault.path) openVault = null
                    }
                }

                VaultScope(vaultPath = vault.path) { close ->
                    VaultWindow(
                        vaultPath = vault.path,
                        vaultName = vault.name,
                        onSwitchVault = { close(onSwitchVault) },
                        onActiveTabChange = { host.activeTabName = it }
                    )
                }
            }
        )

        WindowDialogsHost(
            platformConventions = host.conventions,
            dialogs = host.dialogs,
            vaultPath = openVault,
            aiModelSection = { modifier, spotlight ->
                ModelManagerScreen(
                    modifier = modifier,
                    spotlight = spotlight
                )
            },
            syncSection = { modifier, spotlight ->
                SyncSettingsScreen(
                    vaultPath = openVault,
                    modifier = modifier,
                    spotlight = spotlight
                )
            }
        )

        // The first-run welcome over everything, until it is finished or skipped (5.1).
        OnboardingHost()
    }
}

@Preview
@Composable
private fun NotekeaAppLocalsPreview() {
    AppLocals(
        isMotionReduced = false,
        appearance = Appearance(),
        appVersion = AppVersionDefaults.PREVIEW,
        content = { Text(text = PREVIEW_TEXT) },
        isSystemDark = false
    )
}

@Preview
@Composable
private fun NotekeaRootPreview() {
    App {
        WelcomeContent(
            state = WelcomePreviewData.state(),
            onIntent = {},
            openedVaultContent = { _, _ -> }
        )
    }
}
