package app.notekea.core.platform.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.SystemAppearance
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * [SystemAppearance] on Linux, through the freedesktop settings portal that GNOME, KDE and COSMIC implement, asked with
 * `gdbus` (part of GLib on every such desktop): the current color scheme, then every change while the app runs. On
 * macOS and Windows, or when the portal cannot be asked, it says nothing and Compose's own answer is used.
 */
class PortalSystemAppearance(
    private val operatingSystem: OperatingSystem,
    private val dispatchers: AppDispatchers
) : SystemAppearance {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val parser = PortalColorSchemeParser()

    override fun observeDarkTheme(): Flow<Boolean?> {
        if (operatingSystem != OperatingSystem.LINUX) return flowOf(value = null)
        return callbackFlow {
            val monitor = start(command = MONITOR_COMMAND)
            trySend(element = start(command = READ_COMMAND)?.let { read(process = it) })
            launch {
                monitor?.inputStream?.bufferedReader()?.useLines { lines ->
                    lines.mapNotNull { parser.isDark(output = it) }.forEach { trySend(element = it) }
                }
            }
            awaitClose { monitor?.destroy() }
        }.flowOn(context = dispatchers.io)
    }

    private fun read(
        process: Process
    ): Boolean? = process.inputStream.bufferedReader().use { parser.isDark(output = it.readText()) }

    private fun start(
        command: List<String>
    ): Process? = try {
        ProcessBuilder(command).redirectErrorStream(true).start()
    } catch (exception: IOException) {
        logger.w(throwable = exception) { UNAVAILABLE_MESSAGE }
        null
    }

    companion object {
        private const val DESTINATION = "org.freedesktop.portal.Desktop"
        private const val LOG_TAG = "SystemAppearance"
        private const val OBJECT_PATH = "/org/freedesktop/portal/desktop"
        private const val UNAVAILABLE_MESSAGE = "The desktop portal cannot be asked for the color scheme"
        private val MONITOR_COMMAND = listOf(
            "gdbus",
            "monitor",
            "--session",
            "--dest",
            DESTINATION,
            "--object-path",
            OBJECT_PATH
        )
        private val READ_COMMAND = listOf(
            "gdbus", "call", "--session", "--dest", DESTINATION, "--object-path", OBJECT_PATH,
            "--method", "org.freedesktop.portal.Settings.ReadOne", "org.freedesktop.appearance", "color-scheme"
        )
    }
}
