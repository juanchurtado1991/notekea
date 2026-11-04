package app.notekea.core.platform.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The system's reduce-motion setting, from what each system's tool prints (audit 50). */
class CommandSystemMotionTest {
    @Test
    fun `each system's answer says reduced or not, and no answer says not`() = runTest {
        val dispatchers = FakeAppDispatchers(dispatcher = StandardTestDispatcher(scheduler = testScheduler))
        val answers = listOf(
            Triple(OperatingSystem.LINUX, "(<false>,)\n", true),
            Triple(OperatingSystem.LINUX, "(<<false>>,)\n", true),
            Triple(OperatingSystem.LINUX, "(<true>,)\n", false),
            Triple(OperatingSystem.LINUX, "Error: GDBus.Error:org.freedesktop.portal.Error.NotFound", false),
            Triple(OperatingSystem.LINUX, null, false),
            Triple(OperatingSystem.MACOS, "1\n", true),
            Triple(OperatingSystem.MACOS, "0\n", false),
            Triple(OperatingSystem.MACOS, "The domain/default pair does not exist\n", false),
            Triple(OperatingSystem.WINDOWS, "    MinAnimate    REG_SZ    0\r\n\r\n", true),
            Triple(OperatingSystem.WINDOWS, "    MinAnimate    REG_SZ    1\r\n\r\n", false)
        )

        assertEquals(
            expected = answers.map { it.third },
            actual = answers.map { (system, output, _) ->
                CommandSystemMotion(operatingSystem = system, dispatchers = dispatchers, run = { output }).isReduced()
            }
        )
    }
}
