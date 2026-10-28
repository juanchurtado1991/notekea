package com.testbuddy

import androidx.compose.ui.input.key.Key
import com.testbuddy.ShortcutModifier.Alt
import com.testbuddy.ShortcutModifier.Ctrl
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import kotlin.test.Test
import kotlin.test.assertEquals

class ShortcutTest {
    @Test
    fun `Mod is Cmd on macOS and Ctrl elsewhere`() {
        val shortcut = Mod + Key.P
        assertEquals(listOf(Key.MetaLeft), shortcut.modifierKeysOn(system = HostSystem.MACOS))
        assertEquals(listOf(Key.CtrlLeft), shortcut.modifierKeysOn(system = HostSystem.WINDOWS))
        assertEquals(listOf(Key.CtrlLeft), shortcut.modifierKeysOn(system = HostSystem.LINUX))
        assertEquals(Key.P, shortcut.key)
    }

    @Test
    fun `modifiers chain in the order they are written`() {
        val shortcut = Mod + Shift + Alt + Key.F
        assertEquals(
            listOf(Key.MetaLeft, Key.ShiftLeft, Key.AltLeft),
            shortcut.modifierKeysOn(system = HostSystem.MACOS)
        )
        assertEquals(listOf(Key.CtrlLeft), (Ctrl + Key.Tab).modifierKeysOn(system = HostSystem.MACOS))
        assertEquals("Mod+Shift+${Key.F}", (Mod + Shift + Key.F).toString())
    }

    @Test
    fun `the system is read from the JVM's name for it`() {
        assertEquals(HostSystem.WINDOWS, HostSystem.of(osName = "Windows 11"))
        assertEquals(HostSystem.MACOS, HostSystem.of(osName = "Mac OS X"))
        assertEquals(HostSystem.LINUX, HostSystem.of(osName = "Linux"))
        assertEquals(HostSystem.LINUX, HostSystem.of(osName = "FreeBSD"))
        assertEquals(HostSystem.of(osName = System.getProperty("os.name")), HostSystem.current())
    }
}
