package com.testbuddy

import androidx.compose.ui.input.key.Key

/** A key pressed while [modifiers] are held, as [CaseScope.press] sends it on each system. */
public class Shortcut internal constructor(
    private val modifiers: List<ShortcutModifier>,
    internal val key: Key
) {
    internal fun modifierKeysOn(system: HostSystem): List<Key> = modifiers.map { it.keyOn(system = system) }

    override fun toString(): String = (modifiers.map { it.toString() } + key.toString()).joinToString(separator = "+")
}
