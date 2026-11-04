package app.notekea.core.platform.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.SystemMotion
import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * [SystemMotion] through each system's own tool, as the JDK does not read it: `defaults` on macOS, the registry on
 * Windows, and on Linux GNOME's animations through the settings portal (KDE and COSMIC do not answer, so it is false
 * there). [run] gives what a command printed, null when it cannot start.
 */
class CommandSystemMotion(
    private val operatingSystem: OperatingSystem,
    private val dispatchers: AppDispatchers,
    private val run: (List<String>) -> String? = ::outputOf
) : SystemMotion {
    override suspend fun isReduced(): Boolean = withContext(context = dispatchers.io) {
        when (operatingSystem) {
            OperatingSystem.LINUX -> run(LINUX_COMMAND)?.contains(other = LINUX_ANIMATIONS_OFF) == true
            OperatingSystem.MACOS -> run(MACOS_COMMAND)?.trim() == MACOS_REDUCED
            OperatingSystem.WINDOWS -> run(WINDOWS_COMMAND)?.trim()?.endsWith(suffix = WINDOWS_ANIMATIONS_OFF) == true
        }
    }

    companion object {
        private const val LINUX_ANIMATIONS_OFF = "<<false>>"
        private const val LOG_TAG = "SystemMotion"
        private const val MACOS_REDUCED = "1"
        private const val UNAVAILABLE_MESSAGE = "The system's reduce motion setting cannot be read"
        private const val WINDOWS_ANIMATIONS_OFF = " 0"
        private val LINUX_COMMAND = listOf(
            "gdbus", "call", "--session", "--dest", "org.freedesktop.portal.Desktop",
            "--object-path", "/org/freedesktop/portal/desktop",
            "--method", "org.freedesktop.portal.Settings.ReadOne", "org.gnome.desktop.interface", "enable-animations"
        )
        private val MACOS_COMMAND = listOf("defaults", "read", "com.apple.universalaccess", "reduceMotion")
        private val WINDOWS_COMMAND = listOf(
            "reg",
            "query",
            "HKCU\\Control Panel\\Desktop\\WindowMetrics",
            "/v",
            "MinAnimate"
        )

        private fun outputOf(
            command: List<String>
        ): String? = try {
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (exception: IOException) {
            Logger.withTag(tag = LOG_TAG).w(throwable = exception) { UNAVAILABLE_MESSAGE }
            null
        }
    }
}
