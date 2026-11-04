package app.notekea.core.presentation.platform

import androidx.compose.ui.input.key.KeyShortcut
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutModifier

/** The shortcut as a menu item of the desktop's menu bar shows it. */
fun Shortcut.keyShortcut(): KeyShortcut = KeyShortcut(
    key = key.composeKey(),
    ctrl = ShortcutModifier.CONTROL in modifiers,
    meta = ShortcutModifier.COMMAND in modifiers,
    alt = ShortcutModifier.ALT in modifiers,
    shift = ShortcutModifier.SHIFT in modifiers
)
