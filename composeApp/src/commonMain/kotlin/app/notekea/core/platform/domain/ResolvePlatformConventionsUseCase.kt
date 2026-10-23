package app.notekea.core.platform.domain

/**
 * Maps the raw [SystemEnvironment] to the [PlatformConventions] defined in `PLATFORM_GUIDELINES.md`:
 * primary modifier (Cmd on macOS, Ctrl elsewhere), quit shortcut (Cmd+Q, Alt+F4 on Windows, Ctrl+Q on Linux),
 * dialog button order and the standard per-user application data directory, honoring `APPDATA` and
 * `XDG_DATA_HOME` when they are set.
 */
class ResolvePlatformConventionsUseCase(private val systemEnvironment: SystemEnvironment) {
    private val macosNamePattern = Regex(pattern = MACOS_NAME_MARKERS)

    operator fun invoke(): PlatformConventions {
        val operatingSystem = detectOperatingSystem(name = systemEnvironment.operatingSystemName)
        return PlatformConventions(
            operatingSystem = operatingSystem,
            primaryModifier = primaryModifierFor(operatingSystem = operatingSystem),
            quitShortcut = quitShortcutFor(operatingSystem = operatingSystem),
            dialogButtonOrder = dialogButtonOrderFor(operatingSystem = operatingSystem),
            appDataDirectory = appDataDirectoryFor(operatingSystem = operatingSystem)
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
