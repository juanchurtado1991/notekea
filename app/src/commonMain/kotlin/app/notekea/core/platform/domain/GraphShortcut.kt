package app.notekea.core.platform.domain

/** Opens the graph (`ROADMAP.md` 2.7), the same on every system with its primary modifier. */
val PlatformConventions.graphShortcut: Shortcut
    get() = Shortcut(key = ShortcutKey.G, modifiers = setOf(primaryModifier))
