package app.notekea.core.presentation.platform

import androidx.compose.ui.input.key.Key
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier
import kotlin.test.Test
import kotlin.test.assertEquals

class ShortcutPresentationTest {
    private val everyModifier = Shortcut(key = ShortcutKey.O, modifiers = ShortcutModifier.entries.toSet())

    @Test
    fun `labels follow each platform's notation and modifier order`() {
        assertEquals(
            expected = listOf("⌃⌥⇧⌘O", "Ctrl+Alt+Shift+Win+O", "Ctrl+Alt+Shift+Win+O"),
            actual = listOf(OperatingSystem.MACOS, OperatingSystem.LINUX, OperatingSystem.WINDOWS).map {
                everyModifier.label(operatingSystem = it)
            }
        )
    }

    @Test
    fun `keys are listed one by one for keycaps, in the platform's order`() {
        assertEquals(
            expected = listOf(listOf("⌃", "⌥", "⇧", "⌘", "O"), listOf("Ctrl", "Alt", "Shift", "Win", "O")),
            actual = listOf(OperatingSystem.MACOS, OperatingSystem.WINDOWS).map {
                everyModifier.keys(operatingSystem = it)
            }
        )
    }

    @Test
    fun `toggling a task reads as each platform writes it`() {
        assertEquals(
            expected = listOf("⌘↩", "Ctrl+Enter"),
            actual = listOf(
                Shortcut(key = ShortcutKey.ENTER, modifiers = setOf(ShortcutModifier.COMMAND))
                    .label(operatingSystem = OperatingSystem.MACOS),
                Shortcut(key = ShortcutKey.ENTER, modifiers = setOf(ShortcutModifier.CONTROL))
                    .label(operatingSystem = OperatingSystem.WINDOWS)
            )
        )
    }

    @Test
    fun `search in all notes reads as each platform writes it`() {
        val search = { modifier: ShortcutModifier ->
            Shortcut(key = ShortcutKey.F, modifiers = setOf(modifier, ShortcutModifier.SHIFT))
        }

        assertEquals(
            expected = listOf("⇧⌘F", "Ctrl+Shift+F"),
            actual = listOf(
                search(ShortcutModifier.COMMAND).label(operatingSystem = OperatingSystem.MACOS),
                search(ShortcutModifier.CONTROL).label(operatingSystem = OperatingSystem.LINUX)
            )
        )
    }

    @Test
    fun `named keys use symbols on macOS and words elsewhere`() {
        val shortcuts = listOf(
            Shortcut(key = ShortcutKey.ENTER, modifiers = emptySet()),
            Shortcut(key = ShortcutKey.BACKSPACE, modifiers = setOf(ShortcutModifier.COMMAND)),
            Shortcut(key = ShortcutKey.DELETE, modifiers = emptySet()),
            Shortcut(key = ShortcutKey.BACKSLASH, modifiers = setOf(ShortcutModifier.COMMAND)),
            Shortcut(key = ShortcutKey.F2, modifiers = emptySet())
        )

        assertEquals(
            expected = listOf("↩", "⌘⌫", "⌦", "⌘\\", "F2"),
            actual = shortcuts.map { it.label(operatingSystem = OperatingSystem.MACOS) }
        )
        assertEquals(
            expected = listOf("Enter", "Win+Backspace", "Delete", "Win+\\", "F2"),
            actual = shortcuts.map { it.label(operatingSystem = OperatingSystem.WINDOWS) }
        )
    }

    @Test
    fun `tree and editor shortcut keys match their compose keys`() {
        val pressed =
            listOf(Key.Backslash, Key.Backspace, Key.Delete, Key.Enter, Key.F2, Key.N, Key.S, Key.F, Key.E, Key.P)
        val keys = listOf(
            ShortcutKey.BACKSLASH,
            ShortcutKey.BACKSPACE,
            ShortcutKey.DELETE,
            ShortcutKey.ENTER,
            ShortcutKey.F2,
            ShortcutKey.N,
            ShortcutKey.S,
            ShortcutKey.F,
            ShortcutKey.E,
            ShortcutKey.P
        )

        assertEquals(
            expected = List(size = keys.size) { true },
            actual = keys.zip(pressed).map { (key, composeKey) ->
                Shortcut(key = key, modifiers = emptySet()).matches(key = composeKey, pressedModifiers = emptySet())
            }
        )
    }

    @Test
    fun `a shortcut matches only its key with exactly its modifiers`() {
        val openFolder = Shortcut(key = ShortcutKey.O, modifiers = setOf(ShortcutModifier.CONTROL))
        val control = setOf(ShortcutModifier.CONTROL)

        assertEquals(
            expected = listOf(true, false, false, false),
            actual = listOf(
                openFolder.matches(key = Key.O, pressedModifiers = control),
                openFolder.matches(key = Key.Q, pressedModifiers = control),
                openFolder.matches(key = Key.O, pressedModifiers = emptySet()),
                openFolder.matches(key = Key.O, pressedModifiers = control + ShortcutModifier.SHIFT)
            )
        )
        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                Shortcut(key = ShortcutKey.F4, modifiers = setOf(ShortcutModifier.ALT)).matches(
                    key = Key.F4,
                    pressedModifiers = setOf(ShortcutModifier.ALT)
                ),
                Shortcut(
                    key = ShortcutKey.Q,
                    modifiers = emptySet()
                ).matches(key = Key.Q, pressedModifiers = emptySet())
            )
        )
    }
}
