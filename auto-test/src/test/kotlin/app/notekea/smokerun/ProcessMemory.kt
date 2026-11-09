package app.notekea.smokerun

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The memory of this process, as a performance pass measures it (`ROADMAP.md`, "Closing a milestone"): the Java heap in
 * use, and the resident memory the operating system gives the whole process (heap, native, Skia), read from
 * `/proc/self/status` on Linux, `ps` on macOS and `tasklist` on Windows; null where it cannot be read.
 */
internal object ProcessMemory {
    private const val BYTES_PER_MEGABYTE = 1_048_576L
    private const val KILOBYTES_PER_MEGABYTE = 1_024L
    private const val COMMAND_SECONDS = 5L
    private const val LINUX_STATUS = "/proc/self/status"
    private const val LINUX_RESIDENT = "VmRSS:"
    private const val OS_NAME = "os.name"
    private const val MAC = "mac"
    private const val WINDOWS = "windows"
    private val DIGITS = Regex(pattern = "[^0-9]")

    /** The Java heap in use, in megabytes, after asking for a collection so garbage does not count. */
    fun heapUsedMegabytes(): Long {
        System.gc()
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MEGABYTE
    }

    /** The whole process's resident memory, in megabytes. */
    fun residentMegabytes(): Long? {
        val os = System.getProperty(OS_NAME).lowercase()
        val kilobytes = when {
            os.contains(other = WINDOWS) -> windowsKilobytes()
            os.contains(other = MAC) -> commandKilobytes(command = listOf("ps", "-o", "rss=", "-p", pid()))
            else -> linuxKilobytes()
        }
        return kilobytes?.div(other = KILOBYTES_PER_MEGABYTE)
    }

    private fun pid(): String = ProcessHandle.current().pid().toString()

    private fun linuxKilobytes(): Long? = File(LINUX_STATUS).takeIf { it.exists() }?.readLines()
        ?.firstOrNull { it.startsWith(prefix = LINUX_RESIDENT) }
        ?.replace(regex = DIGITS, replacement = "")?.toLongOrNull()

    /** `tasklist` writes `"java.exe","1234","Console","1","345,678 K"`: the last field is the memory. */
    private fun windowsKilobytes(): Long? {
        val line = commandOutput(command = listOf("tasklist", "/FI", "PID eq ${pid()}", "/FO", "CSV", "/NH"))
        return line?.split("\",\"")?.lastOrNull()?.replace(regex = DIGITS, replacement = "")?.toLongOrNull()
    }

    private fun commandKilobytes(
        command: List<String>
    ): Long? = commandOutput(command = command)?.trim()?.toLongOrNull()

    private fun commandOutput(
        command: List<String>
    ): String? = runCatching {
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        process.waitFor(COMMAND_SECONDS, TimeUnit.SECONDS)
        process.inputStream.bufferedReader().readText()
    }.getOrNull()
}
