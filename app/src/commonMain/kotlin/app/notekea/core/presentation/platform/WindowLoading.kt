package app.notekea.core.presentation.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * What the window has finished loading: the vault whose first scan ended ([loadedVault], its path), told by the vault
 * and read by what must not show over a window still loading (the first-run welcome; the developer's smoke of
 * 2026-11-12).
 */
class WindowLoading {
    var loadedVault: String? by mutableStateOf(value = null)
}
