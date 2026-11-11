package app.notekea.core.platform.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The system's accent from each system's own tool (redesign 4B): macOS's named accents, Windows' registry. */
class CommandSystemAccentTest {
    @Test
    fun `each system's answer becomes its accent color`() = runTest {
        val dispatchers = FakeAppDispatchers(dispatcher = StandardTestDispatcher(testScheduler))

        val read = listOf(
            OperatingSystem.MACOS to "1\n",
            OperatingSystem.MACOS to MACOS_NO_ACCENT,
            OperatingSystem.WINDOWS to WINDOWS_BLUE,
            OperatingSystem.WINDOWS to "ERROR: The system was unable to find the specified registry key or value.",
            OperatingSystem.LINUX to "anything"
        ).map { (system, output) ->
            CommandSystemAccent(
                operatingSystem = system,
                dispatchers = dispatchers,
                run = { output }
            ).read()
        }

        assertEquals(
            expected = listOf(0xFFF7821BL, 0xFF007AFFL, 0xFF0078D7L, null, null),
            actual = read
        )
    }

    private companion object {
        const val MACOS_NO_ACCENT =
            "The domain/default pair of (kCFPreferencesAnyApplication, AppleAccentColor) does not exist"
        const val WINDOWS_BLUE =
            "\nHKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\DWM\n    AccentColor    REG_DWORD    0xffd77800\n"
    }
}
