package com.testbuddy

import androidx.compose.ui.input.key.Key

/** Modifier keys held together, waiting for the key that makes them a [Shortcut]: the `Mod + Shift` of `Mod + Shift + Key.F`. */
public class Chord internal constructor(private val modifiers: List<ShortcutModifier>) {
    public operator fun plus(key: Key): Shortcut = Shortcut(modifiers = modifiers, key = key)

    public operator fun plus(other: ShortcutModifier): Chord = Chord(modifiers = modifiers + other)
}
