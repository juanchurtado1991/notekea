package app.notekea.core.presentation.platform

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.notekea.core.platform.domain.PlatformConventions

/**
 * What a host of Notekea's window holds for it: the real window or a Self-Test case. [conventions] are the system's,
 * and [title] follows the open vault and its active tab.
 */
@Stable
class WindowHost(
    val conventions: PlatformConventions
) {
    val shortcuts = ShortcutDispatcher()
    val closeGuard = WindowCloseGuard()
    val dialogs = WindowDialogs()
    var vaultName by mutableStateOf<String?>(value = null)
    var activeTabName by mutableStateOf<String?>(value = null)

    val title: String get() = WindowTitle.of(vaultName = vaultName, activeTabName = activeTabName)
}
