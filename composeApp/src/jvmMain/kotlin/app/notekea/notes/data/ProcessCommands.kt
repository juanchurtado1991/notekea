package app.notekea.notes.data

import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/*
 * Runs desktop helper commands (gio, open, explorer, dbus-send, wl-paste…). [runCommand] discards their output: the exit
 * code, or null when the command does not exist or did not finish in time, is all callers need to choose a fallback.
 * [readCommand] returns what the command printed.
 */

private const val COMMAND_TIMEOUT_SECONDS = 10L
private const val SUCCESS = 0

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

/**
 * Runs a desktop helper command (wl-paste…) and returns what it printed, or null when the command does not exist,
 * fails, or does not finish in time; its errors are discarded.
 */
fun readCommand(
    command: List<String>
): ByteArray? = try {
    val process = ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start()
    val output = CompletableFuture.supplyAsync { process.inputStream.use { it.readBytes() } }
    try {
        output.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS).takeIf {
            process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS) && process.exitValue() == SUCCESS
        }
    } catch (_: TimeoutException) {
        process.destroy()
        null
    } catch (_: ExecutionException) {
        process.destroy()
        null
    }
} catch (_: IOException) {
    null
}
