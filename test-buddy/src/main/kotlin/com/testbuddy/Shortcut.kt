package com.testbuddy

import androidx.compose.ui.input.key.Key

/** A key pressed while [modifiers] are held, as [CaseScope.press] sends it on each system. */
public class Shortcut internal constructor(
    private val modifiers: List<ShortcutModifier>,
    internal val key: Key
) {
    internal fun modifierKeysOn(system: HostSystem): List<Key> = modifiers.map { it.keyOn(system = system) }

    override fun toString(): String = (modifiers.map { it.toString() } + key.toString()).joinToString(separator = "+")

    /** As a person reads it on [system]: `Ctrl+Shift+F`, `Cmd+P` on macOS. */
    internal fun describe(system: HostSystem): String {
        val names = modifiers.map { modifier ->
            when (modifier) {
                ShortcutModifier.Mod -> if (system == HostSystem.MACOS) "Cmd" else "Ctrl"
                else -> modifier.toString()
            }
        }
        return (names + keyName(key = key)).joinToString(separator = "+")
    }
}

/** A key's name as printed on it: `P`, `Enter`, not Compose's `Key: P`. */
internal fun keyName(key: Key): String = key.toString().removePrefix("Key: ")
