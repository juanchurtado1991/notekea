package app.notekea.core.platform.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Linux dark theme comes from the desktop portal (1.17 smoke: COSMIC in dark mode showed the light theme). */
class PortalSystemAppearanceTest {
    private val parser = PortalColorSchemeParser()

    @Test
    fun `the portal's answer and its changes say dark or light, anything else says nothing`() {
        assertEquals(
            expected = listOf(true, false, null, true, null, null),
            actual = listOf(
                "(<uint32 1>,)",
                "(<uint32 2>,)",
                "(<uint32 0>,)",
                "/org/freedesktop/portal/desktop: org.freedesktop.portal.Settings.SettingChanged " +
                    "('org.freedesktop.appearance', 'color-scheme', <uint32 1>)",
                "/org/freedesktop/portal/desktop: org.freedesktop.portal.Settings.SettingChanged " +
                    "('org.freedesktop.appearance', 'contrast', <uint32 1>)",
                "Error: GDBus.Error:org.freedesktop.DBus.Error.ServiceUnknown"
            ).map { parser.isDark(output = it) }
        )
    }

    @Test
    fun `macOS and Windows leave the answer to Compose`() = runTest {
        val dispatchers = FakeAppDispatchers(dispatcher = StandardTestDispatcher(scheduler = testScheduler))

        assertEquals(
            expected = listOf<Boolean?>(null, null),
            actual = listOf(OperatingSystem.MACOS, OperatingSystem.WINDOWS).map {
                PortalSystemAppearance(operatingSystem = it, dispatchers = dispatchers).observeDarkTheme().first()
            }
        )
    }
}
