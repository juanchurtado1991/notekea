package app.notekea.notes.data

import java.io.IOException
import java.util.concurrent.TimeUnit

/*
 * Runs a desktop helper command (gio, open, explorer, dbus-send…) with its output discarded. The exit code, or null
 * when the command does not exist or did not finish in time, is all callers need to choose a fallback.
 */

private const val COMMAND_TIMEOUT_SECONDS = 10L

fun runCommand(
    command: List<String>
): Int? = try {
    val process = ProcessBuilder(
        command
    ).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start()
    if (process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        process.exitValue()
    } else {
        process.destroy()
        null
    }
} catch (_: IOException) {
    null
}
