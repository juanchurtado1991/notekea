package com.testbuddy

import androidx.compose.ui.input.key.Key

/**
 * A modifier key of a [Shortcut], written as a person reads it: `Mod + Key.P`, `Mod + Shift + Key.F`. [Mod] is the
 * system's main one, Cmd on macOS and Ctrl elsewhere; [Ctrl] is Ctrl everywhere (Ctrl+Tab on macOS too).
 */
public sealed class ShortcutModifier {
    public operator fun plus(key: Key): Shortcut = Shortcut(modifiers = listOf(this), key = key)

    public operator fun plus(other: ShortcutModifier): Chord = Chord(modifiers = listOf(this, other))

    internal abstract fun keyOn(system: HostSystem): Key

    public data object Mod : ShortcutModifier() {
        override fun keyOn(system: HostSystem): Key = if (system == HostSystem.MACOS) Key.MetaLeft else Key.CtrlLeft
    }

    public data object Shift : ShortcutModifier() {
        override fun keyOn(system: HostSystem): Key = Key.ShiftLeft
    }

    public data object Alt : ShortcutModifier() {
        override fun keyOn(system: HostSystem): Key = Key.AltLeft
    }

    public data object Ctrl : ShortcutModifier() {
        override fun keyOn(system: HostSystem): Key = Key.CtrlLeft
    }
}
