package app.notekea.core.platform.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolvePlatformConventionsUseCaseTest {
    @Test
    fun `linux honors XDG_DATA_HOME and ignores a trailing slash`() {
        val conventions = resolve(
            operatingSystemName = LINUX_NAME,
            userHome = LINUX_HOME,
            environmentVariables = mapOf(XDG_DATA_HOME_VARIABLE to "/data/ana/")
        )

        assertEquals(
            expected = "/data/ana/notekea",
            actual = conventions.appDataDirectory
        )
    }

    @Test
    fun `linux uses ctrl, ctrl+o, ctrl+q, f2, delete and cancel first with the default data home`() {
        val conventions = resolve(
            operatingSystemName = LINUX_NAME,
            userHome = LINUX_HOME
        )

        assertEquals(
            expected = expectedConventions(
                operatingSystem = OperatingSystem.LINUX,
                primary = ShortcutModifier.CONTROL,
                quit = Shortcut(key = ShortcutKey.Q, modifiers = setOf(ShortcutModifier.CONTROL)),
                rename = Shortcut(key = ShortcutKey.F2, modifiers = emptySet()),
                delete = Shortcut(key = ShortcutKey.DELETE, modifiers = emptySet()),
                buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
                appDataDirectory = "/home/ana/.local/share/notekea",
                homeDirectory = LINUX_HOME
            ),
            actual = conventions
        )
    }

    @Test
    fun `linux treats a blank XDG_DATA_HOME as unset`() {
        val conventions = resolve(
            operatingSystemName = LINUX_NAME,
            userHome = LINUX_HOME,
            environmentVariables = mapOf(XDG_DATA_HOME_VARIABLE to "  ")
        )

        assertEquals(
            expected = "/home/ana/.local/share/notekea",
            actual = conventions.appDataDirectory
        )
    }

    @Test
    fun `macos uses cmd, cmd+o, cmd+q, enter, cmd+backspace, cancel first and application support`() {
        val conventions = resolve(
            operatingSystemName = MACOS_NAME,
            userHome = MACOS_HOME
        )

        assertEquals(
            expected = expectedConventions(
                operatingSystem = OperatingSystem.MACOS,
                primary = ShortcutModifier.COMMAND,
                quit = Shortcut(key = ShortcutKey.Q, modifiers = setOf(ShortcutModifier.COMMAND)),
                rename = Shortcut(key = ShortcutKey.ENTER, modifiers = emptySet()),
                delete = Shortcut(key = ShortcutKey.BACKSPACE, modifiers = setOf(ShortcutModifier.COMMAND)),
                buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
                appDataDirectory = "/Users/ana/Library/Application Support/Notekea",
                homeDirectory = MACOS_HOME
            ),
            actual = conventions
        )
    }

    @Test
    fun `operating system names are detected case-insensitively`() {
        val detected = listOf(
            MACOS_NAME,
            "Darwin",
            WINDOWS_NAME,
            "WINDOWS SERVER 2022",
            LINUX_NAME,
            "FreeBSD",
            "SomethingNew"
        ).map {
            resolve(
                operatingSystemName = it,
                userHome = LINUX_HOME
            ).operatingSystem
        }

        assertEquals(
            expected = listOf(
                OperatingSystem.MACOS,
                OperatingSystem.MACOS,
                OperatingSystem.WINDOWS,
                OperatingSystem.WINDOWS,
                OperatingSystem.LINUX,
                OperatingSystem.LINUX,
                OperatingSystem.LINUX
            ),
            actual = detected
        )
    }

    @Test
    fun `windows falls back to the roaming folder when APPDATA is missing`() {
        val conventions = resolve(
            operatingSystemName = WINDOWS_NAME,
            userHome = WINDOWS_HOME
        )

        assertEquals(
            expected = "C:\\Users\\ana\\AppData\\Roaming\\Notekea",
            actual = conventions.appDataDirectory
        )
    }

    @Test
    fun `windows uses ctrl, ctrl+o, alt+f4, f2, delete, primary first and APPDATA`() {
        val conventions = resolve(
            operatingSystemName = WINDOWS_NAME,
            userHome = WINDOWS_HOME,
            environmentVariables = mapOf(APP_DATA_VARIABLE to "D:\\Profiles\\ana\\Roaming\\")
        )

        assertEquals(
            expected = expectedConventions(
                operatingSystem = OperatingSystem.WINDOWS,
                primary = ShortcutModifier.CONTROL,
                quit = Shortcut(key = ShortcutKey.F4, modifiers = setOf(ShortcutModifier.ALT)),
                rename = Shortcut(key = ShortcutKey.F2, modifiers = emptySet()),
                delete = Shortcut(key = ShortcutKey.DELETE, modifiers = emptySet()),
                buttonOrder = DialogButtonOrder.PRIMARY_THEN_CANCEL,
                appDataDirectory = "D:\\Profiles\\ana\\Roaming\\Notekea",
                homeDirectory = WINDOWS_HOME
            ),
            actual = conventions
        )
    }

    /** The conventions of an operating system: what differs is given, every other shortcut uses [primary]. */
    private fun expectedConventions(
        operatingSystem: OperatingSystem,
        primary: ShortcutModifier,
        quit: Shortcut,
        rename: Shortcut,
        delete: Shortcut,
        buttonOrder: DialogButtonOrder,
        appDataDirectory: String,
        homeDirectory: String
    ): PlatformConventions {
        val withPrimary = { key: ShortcutKey -> Shortcut(key = key, modifiers = setOf(primary)) }
        val withShift = { key: ShortcutKey -> Shortcut(key = key, modifiers = setOf(primary, ShortcutModifier.SHIFT)) }
        return PlatformConventions(
            operatingSystem = operatingSystem,
            primaryModifier = primary,
            openFolderShortcut = withPrimary(ShortcutKey.O),
            quitShortcut = quit,
            newNoteShortcut = withPrimary(ShortcutKey.N),
            renameShortcut = rename,
            deleteShortcut = delete,
            toggleSidebarShortcut = withPrimary(ShortcutKey.BACKSLASH),
            saveShortcut = withPrimary(ShortcutKey.S),
            findShortcut = withPrimary(ShortcutKey.F),
            toggleReadModeShortcut = withPrimary(ShortcutKey.E),
            quickSwitcherShortcut = withPrimary(ShortcutKey.P),
            searchNotesShortcut = withShift(ShortcutKey.F),
            toggleTaskShortcut = withPrimary(ShortcutKey.ENTER),
            boldShortcut = withPrimary(ShortcutKey.B),
            italicShortcut = withPrimary(ShortcutKey.I),
            noteLinkShortcut = withPrimary(ShortcutKey.K),
            webLinkShortcut = withShift(ShortcutKey.K),
            pasteShortcut = withPrimary(ShortcutKey.V),
            toggleSidePanelShortcut = withShift(ShortcutKey.BACKSLASH),
            outlineShortcut = withShift(ShortcutKey.O),
            todayShortcut = withPrimary(ShortcutKey.D),
            previousDayShortcut = Shortcut(key = ShortcutKey.LEFT, modifiers = setOf(primary, ShortcutModifier.ALT)),
            nextDayShortcut = Shortcut(key = ShortcutKey.RIGHT, modifiers = setOf(primary, ShortcutModifier.ALT)),
            closeTabShortcut = withPrimary(ShortcutKey.W),
            nextTabShortcut = Shortcut(key = ShortcutKey.TAB, modifiers = CONTROL),
            previousTabShortcut = Shortcut(key = ShortcutKey.TAB, modifiers = CONTROL + ShortcutModifier.SHIFT),
            splitRightShortcut = withShift(ShortcutKey.D),
            templateShortcut = withPrimary(ShortcutKey.T),
            goToTabShortcuts = DIGITS.map(withPrimary),
            dialogButtonOrder = buttonOrder,
            appDataDirectory = appDataDirectory,
            homeDirectory = homeDirectory
        )
    }

    private fun resolve(
        operatingSystemName: String,
        userHome: String,
        environmentVariables: Map<String, String> = emptyMap()
    ): PlatformConventions = ResolvePlatformConventionsUseCase(
        systemEnvironment = FakeSystemEnvironment(
            operatingSystemName = operatingSystemName,
            userHome = userHome,
            environmentVariables = environmentVariables
        )
    ).invoke()

    companion object {
        private val CONTROL = setOf(ShortcutModifier.CONTROL)
        private val DIGITS = listOf(
            ShortcutKey.DIGIT_1, ShortcutKey.DIGIT_2, ShortcutKey.DIGIT_3, ShortcutKey.DIGIT_4, ShortcutKey.DIGIT_5,
            ShortcutKey.DIGIT_6, ShortcutKey.DIGIT_7, ShortcutKey.DIGIT_8, ShortcutKey.DIGIT_9
        )
        private const val APP_DATA_VARIABLE = "APPDATA"
        private const val LINUX_HOME = "/home/ana"
        private const val LINUX_NAME = "Linux"
        private const val MACOS_HOME = "/Users/ana"
        private const val MACOS_NAME = "Mac OS X"
        private const val WINDOWS_HOME = "C:\\Users\\ana"
        private const val WINDOWS_NAME = "Windows 11"
        private const val XDG_DATA_HOME_VARIABLE = "XDG_DATA_HOME"
    }
}
