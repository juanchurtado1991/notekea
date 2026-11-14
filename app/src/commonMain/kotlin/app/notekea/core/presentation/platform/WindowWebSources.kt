package app.notekea.core.presentation.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The open vault's web sources, for Settings → General, which is drawn over the window and not inside the vault: how
 * many days ago they were last updated ([daysSinceUpdate]: 0 is today, null never), whether an update is under way,
 * and [updateNow], null while no vault is open. The vault writes it (the developer's request of 2026-11-14).
 */
class WindowWebSources {
    var updateNow: (() -> Unit)? by mutableStateOf(value = null)
    var daysSinceUpdate: Int? by mutableStateOf(value = null)
    var isUpdating: Boolean by mutableStateOf(value = false)

    /** The last update asked for read none of its pages: Settings says so, since it runs without its dialog. */
    var hasFailed: Boolean by mutableStateOf(value = false)
}
