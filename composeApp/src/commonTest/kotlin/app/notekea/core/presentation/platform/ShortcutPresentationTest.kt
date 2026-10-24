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
