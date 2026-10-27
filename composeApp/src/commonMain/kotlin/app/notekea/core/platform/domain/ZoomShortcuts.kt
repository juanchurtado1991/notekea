package app.notekea.core.platform.domain

/*
 * The app's zoom shortcuts (`ROADMAP.md` 1.14), the same on every system with its primary modifier, as browsers and
 * editors have them: `Mod+=` larger, `Mod+-` smaller, `Mod+0` back to the system's size.
 */

val PlatformConventions.zoomInShortcut: Shortcut
    get() = Shortcut(key = ShortcutKey.EQUALS, modifiers = setOf(primaryModifier))

val PlatformConventions.zoomOutShortcut: Shortcut
    get() = Shortcut(key = ShortcutKey.MINUS, modifiers = setOf(primaryModifier))

val PlatformConventions.zoomResetShortcut: Shortcut
    get() = Shortcut(key = ShortcutKey.DIGIT_0, modifiers = setOf(primaryModifier))
