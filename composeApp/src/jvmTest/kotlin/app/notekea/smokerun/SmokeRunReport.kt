package app.notekea.smokerun

import java.io.File

/**
 * What a performance pass reads from the automated smoke runs (`ROADMAP.md`, "Closing a milestone"): a line per
 * sample in `memory.csv` (the run, the moment, the Java heap and the process's resident memory in megabytes), in the
 * folder the `smokeRun` Gradle task names; a plain test run (no folder) records nothing.
 */
internal object SmokeRunReport {
    private const val REPORT_DIR = "notekea.smokeRun.reportDir"
    private const val IDLE_SECONDS = "notekea.smokeRun.idleSeconds"
    private const val FILE_NAME = "memory.csv"
    private const val HEADER = "run,moment,heap_mb,resident_mb\n"
    private const val SEPARATOR = ","

    /** How long the idle run waits with a vault open, in seconds: 0 in a plain test run. */
    val idleSeconds: Long get() = System.getProperty(IDLE_SECONDS)?.toLongOrNull() ?: 0L

    /** Samples the memory now, as [moment] of [run]. */
    fun sample(
        run: String,
        moment: String
    ) {
        val folder = System.getProperty(REPORT_DIR)?.let(::File) ?: return
        folder.mkdirs()
        val file = File(folder, FILE_NAME)
        if (!file.exists()) file.writeText(text = HEADER)
        val resident = ProcessMemory.residentMegabytes()?.toString().orEmpty()
        val line = listOf(run, moment, ProcessMemory.heapUsedMegabytes().toString(), resident)
        file.appendText(text = line.joinToString(separator = SEPARATOR) + "\n")
    }
}
