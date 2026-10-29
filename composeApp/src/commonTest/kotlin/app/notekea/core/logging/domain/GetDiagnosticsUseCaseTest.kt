package app.notekea.core.logging.domain

import app.notekea.core.platform.domain.FakeSystemEnvironment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDiagnosticsUseCaseTest {
    private val environment = FakeSystemEnvironment(
        operatingSystemName = "Windows 11",
        userHome = "C:\\Users\\ana",
        operatingSystemVersion = "10.0",
        javaVersion = "21.0.11",
        appVersion = "0.1.0-alpha (m1-rc1)"
    )

    @Test
    fun `the diagnostics hold the versions and the last lines of the log`() = runTest {
        val log = (1..150).map { "line $it" }

        val text = GetDiagnosticsUseCase(systemEnvironment = environment, logStore = FakeLogStore(lines = log))()

        val lines = text.lines()
        assertEquals(
            expected = listOf(
                "Notekea diagnostics",
                "Version: 0.1.0-alpha (m1-rc1)",
                "OS: Windows 11 10.0",
                "Java: 21.0.11",
                "Recent log:",
                "line 51"
            ),
            actual = lines.take(n = 6)
        )
        assertEquals(expected = "line 150", actual = lines.dropLast(n = 1).last())
        assertEquals(expected = GetDiagnosticsUseCase.LOG_LINES + 5, actual = lines.size - 1)
    }

    @Test
    fun `an empty log says so`() = runTest {
        val text = GetDiagnosticsUseCase(systemEnvironment = environment, logStore = FakeLogStore())()

        assertEquals(expected = "(nothing logged yet)", actual = text.trimEnd().lines().last())
    }
}
