package app.notekea.core.platform.domain

/**
 * Maps the raw [SystemEnvironment] to the [PlatformConventions] defined in `PLATFORM_GUIDELINES.md`:
 * primary modifier (Cmd on macOS, Ctrl elsewhere), open-folder shortcut (primary modifier + O), quit shortcut
 * (Cmd+Q, Alt+F4 on Windows, Ctrl+Q on Linux), the vault tree shortcuts (new note with primary modifier + N, rename
 * with Enter on macOS and F2 elsewhere, delete with Cmd+Backspace on macOS and Delete elsewhere, toggle the sidebar
 * with primary modifier + \), save with primary modifier + S, find in note with primary modifier + F, Edit / Read
 * with primary modifier + E, the quick switcher with primary modifier + P and search in all notes with primary
 * modifier + Shift + F, toggling the task on the cursor line with primary modifier + Enter, bold, italic, a note link and a web link
 * with primary modifier + B, I, K and Shift + K, the side panel and the outline with primary modifier + Shift + \ and
 * Shift + O, today's daily note with primary modifier + D and the days around it with primary modifier + Alt + ← and →,
 * dialog button order, the user's home folder and the standard per-user application data directory, honoring
 * `APPDATA` and `XDG_DATA_HOME` when they are set.
 */
class ResolvePlatformConventionsUseCase(private val systemEnvironment: SystemEnvironment) {
    private val macosNamePattern = Regex(pattern = MACOS_NAME_MARKERS)

    operator fun invoke(): PlatformConventions {
        val operatingSystem = detectOperatingSystem(name = systemEnvironment.operatingSystemName)
        val primaryModifier = primaryModifierFor(operatingSystem = operatingSystem)
        val primary = { key: ShortcutKey, extra: ShortcutModifier? ->
            Shortcut(key = key, modifiers = setOfNotNull(primaryModifier, extra))
        }
        return PlatformConventions(
            operatingSystem = operatingSystem,
            primaryModifier = primaryModifier,
            openFolderShortcut = primary(ShortcutKey.O, null),
            quitShortcut = quitShortcutFor(operatingSystem = operatingSystem),
            newNoteShortcut = primary(ShortcutKey.N, null),
            renameShortcut = renameShortcutFor(operatingSystem = operatingSystem),
            deleteShortcut = deleteShortcutFor(operatingSystem = operatingSystem),
            toggleSidebarShortcut = primary(ShortcutKey.BACKSLASH, null),
            saveShortcut = primary(ShortcutKey.S, null),
            findShortcut = primary(ShortcutKey.F, null),
            toggleReadModeShortcut = primary(ShortcutKey.E, null),
            quickSwitcherShortcut = primary(ShortcutKey.P, null),
            searchNotesShortcut = primary(ShortcutKey.F, ShortcutModifier.SHIFT),
            toggleTaskShortcut = primary(ShortcutKey.ENTER, null),
            boldShortcut = primary(ShortcutKey.B, null),
            italicShortcut = primary(ShortcutKey.I, null),
            noteLinkShortcut = primary(ShortcutKey.K, null),
            webLinkShortcut = primary(ShortcutKey.K, ShortcutModifier.SHIFT),
            pasteShortcut = primary(ShortcutKey.V, null),
            toggleSidePanelShortcut = primary(ShortcutKey.BACKSLASH, ShortcutModifier.SHIFT),
            outlineShortcut = primary(ShortcutKey.O, ShortcutModifier.SHIFT),
            todayShortcut = primary(ShortcutKey.D, null),
            previousDayShortcut = primary(ShortcutKey.LEFT, ShortcutModifier.ALT),
            nextDayShortcut = primary(ShortcutKey.RIGHT, ShortcutModifier.ALT),
            dialogButtonOrder = dialogButtonOrderFor(operatingSystem = operatingSystem),
            appDataDirectory = appDataDirectoryFor(operatingSystem = operatingSystem),
            homeDirectory = systemEnvironment.userHome
        )
    }

    private fun appDataDirectoryFor(
        operatingSystem: OperatingSystem
    ): String = when (operatingSystem) {
        OperatingSystem.LINUX -> linuxAppDataDirectory()

        OperatingSystem.MACOS -> joinUnixPath(
            systemEnvironment.userHome,
            MACOS_APPLICATION_SUPPORT,
            APP_DATA_FOLDER
        )

        OperatingSystem.WINDOWS -> windowsAppDataDirectory()
    }

    private fun deleteShortcutFor(
        operatingSystem: OperatingSystem
    ): Shortcut = when (operatingSystem) {
        OperatingSystem.MACOS -> Shortcut(key = ShortcutKey.BACKSPACE, modifiers = setOf(ShortcutModifier.COMMAND))
        OperatingSystem.LINUX, OperatingSystem.WINDOWS -> Shortcut(key = ShortcutKey.DELETE, modifiers = emptySet())
    }

    private fun detectOperatingSystem(
        name: String
    ): OperatingSystem {
        val normalizedName = name.trim().lowercase()
        return when {
            normalizedName.startsWith(prefix = WINDOWS_NAME_PREFIX) -> OperatingSystem.WINDOWS
            macosNamePattern.containsMatchIn(input = normalizedName) -> OperatingSystem.MACOS
            else -> OperatingSystem.LINUX
        }
    }

    private fun dialogButtonOrderFor(
        operatingSystem: OperatingSystem
    ): DialogButtonOrder = when (operatingSystem) {
        OperatingSystem.WINDOWS -> DialogButtonOrder.PRIMARY_THEN_CANCEL
        OperatingSystem.LINUX, OperatingSystem.MACOS -> DialogButtonOrder.CANCEL_THEN_PRIMARY
    }

    private fun joinUnixPath(
        vararg segments: String
    ): String = segments.joinToString(separator = UNIX_SEPARATOR) { it.removeSuffix(suffix = UNIX_SEPARATOR) }

    private fun joinWindowsPath(
        vararg segments: String
    ): String = segments.joinToString(separator = WINDOWS_SEPARATOR) { it.removeSuffix(suffix = WINDOWS_SEPARATOR) }

    private fun linuxAppDataDirectory(): String {
        val dataHome = nonBlankVariable(name = XDG_DATA_HOME_VARIABLE)
            ?: joinUnixPath(systemEnvironment.userHome, LINUX_DEFAULT_DATA_HOME)
        return joinUnixPath(dataHome, LINUX_APP_DATA_FOLDER)
    }

    private fun nonBlankVariable(
        name: String
    ): String? = systemEnvironment.environmentVariable(name = name)?.takeIf { it.isNotBlank() }

    private fun primaryModifierFor(
        operatingSystem: OperatingSystem
    ): ShortcutModifier = when (operatingSystem) {
        OperatingSystem.MACOS -> ShortcutModifier.COMMAND
        OperatingSystem.LINUX, OperatingSystem.WINDOWS -> ShortcutModifier.CONTROL
    }

    private fun quitShortcutFor(
        operatingSystem: OperatingSystem
    ): Shortcut = when (operatingSystem) {
        OperatingSystem.LINUX -> Shortcut(
            key = ShortcutKey.Q,
            modifiers = setOf(ShortcutModifier.CONTROL)
        )

        OperatingSystem.MACOS -> Shortcut(
            key = ShortcutKey.Q,
            modifiers = setOf(ShortcutModifier.COMMAND)
        )

        OperatingSystem.WINDOWS -> Shortcut(
            key = ShortcutKey.F4,
            modifiers = setOf(ShortcutModifier.ALT)
        )
    }

    private fun renameShortcutFor(
        operatingSystem: OperatingSystem
    ): Shortcut = when (operatingSystem) {
        OperatingSystem.MACOS -> Shortcut(key = ShortcutKey.ENTER, modifiers = emptySet())
        OperatingSystem.LINUX, OperatingSystem.WINDOWS -> Shortcut(key = ShortcutKey.F2, modifiers = emptySet())
    }

    private fun windowsAppDataDirectory(): String {
        val appData = nonBlankVariable(name = WINDOWS_APP_DATA_VARIABLE)
            ?: joinWindowsPath(systemEnvironment.userHome, WINDOWS_DEFAULT_APP_DATA)
        return joinWindowsPath(appData, APP_DATA_FOLDER)
    }

    companion object {
        private const val APP_DATA_FOLDER = "Notekea"
        private const val LINUX_APP_DATA_FOLDER = "notekea"
        private const val LINUX_DEFAULT_DATA_HOME = ".local/share"
        private const val MACOS_APPLICATION_SUPPORT = "Library/Application Support"
        private const val MACOS_NAME_MARKERS = "mac|darwin"
        private const val UNIX_SEPARATOR = "/"
        private const val WINDOWS_APP_DATA_VARIABLE = "APPDATA"
        private const val WINDOWS_DEFAULT_APP_DATA = "AppData\\Roaming"
        private const val WINDOWS_NAME_PREFIX = "windows"
        private const val WINDOWS_SEPARATOR = "\\"
        private const val XDG_DATA_HOME_VARIABLE = "XDG_DATA_HOME"
    }
}
