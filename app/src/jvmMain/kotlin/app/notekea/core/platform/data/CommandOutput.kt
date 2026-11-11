package app.notekea.core.platform.data

import co.touchlab.kermit.Logger
import java.io.IOException

/** What a system tool prints, for the settings the JDK does not read (reduce motion, the accent color). */
object CommandOutput {
    private const val LOG_TAG = "CommandOutput"

    /** Everything [command] printed, its errors included; null when it cannot start. */
    fun of(command: List<String>): String? = try {
        val process = ProcessBuilder(command).redirectErrorStream(true).start()

        process
            .inputStream
            .bufferedReader()
            .use { it.readText() }
    } catch (exception: IOException) {
        Logger
            .withTag(tag = LOG_TAG)
            .w(throwable = exception) { "${command.first()} could not start" }

        null
    }
}
