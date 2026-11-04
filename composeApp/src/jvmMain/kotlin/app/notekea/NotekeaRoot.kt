package app.notekea

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.presentation.platform.AppEnvironment
import app.notekea.core.presentation.platform.AppVersionDefaults
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.core.presentation.platform.UiZoom
import app.notekea.core.presentation.platform.WindowCloseShortcuts
import app.notekea.core.presentation.platform.WindowDialogsHost
import app.notekea.core.presentation.platform.WindowHost
import app.notekea.core.presentation.platform.awtFileDrop
import app.notekea.core.presentation.theme.LocalAppearance
import app.notekea.core.presentation.theme.LocalSystemDarkTheme
import app.notekea.core.presentation.theme.LocalSystemReduceMotion
import app.notekea.tree.presentation.VaultScope
import app.notekea.shell.presentation.VaultTreeScreen
import app.notekea.vault.presentation.welcome.WelcomeContent
import app.notekea.vault.presentation.welcome.WelcomePreviewData
import app.notekea.vault.presentation.welcome.WelcomeScreen
import java.awt.Cursor

private const val PREVIEW_TEXT = "Every window of the app"

/**
 * What every window of the app reads, around them: the system's dark theme and reduced motion, the kept appearance
 * ([initialAppearance] until the store answers), the desktop's file drop and resize cursor, and the app's version. The
 * app and the Self-Test's cases both start here, so a case runs the same app.
 */
@Composable
fun NotekeaAppLocals(
    environment: AppEnvironment,
    initialAppearance: Appearance,
    content: @Composable () -> Unit
) {
    val darkTheme = remember(environment) { environment.systemAppearance.observeDarkTheme() }
    val appearances = remember(environment) { environment.appearanceStore.observe() }
    val isSystemDark by darkTheme.collectAsState(initial = null)
    val appearance by appearances.collectAsState(initial = initialAppearance)
    val isMotionReduced by produceState(initialValue = false, environment) {
        value = environment.systemMotion.isReduced()
    }
    AppLocals(
        isMotionReduced = isMotionReduced,
        appearance = appearance,
        appVersion = environment.systemEnvironment.appVersion,
        content = content,
        isSystemDark = isSystemDark
    )
}

@Composable
private fun AppLocals(
    isMotionReduced: Boolean,
    appearance: Appearance,
    appVersion: String,
    content: @Composable () -> Unit,
    isSystemDark: Boolean?
) {
    CompositionLocalProvider(
        LocalSystemDarkTheme provides isSystemDark,
        LocalSystemReduceMotion provides isMotionReduced,
        LocalAppearance provides appearance,
        LocalFileDrop provides ::awtFileDrop,
        LocalAppVersion provides appVersion,
        LocalResizePointerIcon provides PointerIcon(cursor = Cursor(Cursor.E_RESIZE_CURSOR)),
        content = content
    )
}

/**
 * The whole window's content, in the real window and in a Self-Test case, which differ only in their [host] and in what
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
        WindowCloseShortcuts(platformConventions = host.conventions, onClose = onClose)
        UiZoom(platformConventions = host.conventions) { WindowContent(host = host) }
    }
}

@Composable
private fun WindowContent(
    host: WindowHost
) {
    App {
        WelcomeScreen(
            onVaultNameChange = { host.vaultName = it },
            openedVaultContent = { vault, onSwitchVault ->
                VaultScope(vaultPath = vault.path) { close ->
                    VaultTreeScreen(
                        vaultPath = vault.path,
                        vaultName = vault.name,
                        onSwitchVault = { close(onSwitchVault) },
                        onActiveTabChange = { host.activeTabName = it }
                    )
                }
            }
        )
        WindowDialogsHost(platformConventions = host.conventions, dialogs = host.dialogs)
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
        WelcomeContent(state = WelcomePreviewData.state(), onIntent = {}, openedVaultContent = { _, _ -> })
    }
}
