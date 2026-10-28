package app.notekea.smokerun

import org.jetbrains.skia.Graphics
import java.io.File

/**
 * What a performance pass reads from the automated smoke runs (`ROADMAP.md`, "Closing a milestone"): a line per
 * sample in `memory.csv` (the run, the moment, the Java heap in use and the heap the JVM holds for it,
 * the process's resident memory and Skia's font and resource caches, in megabytes), in the folder the `smokeRun` Gradle task names; a plain test run (no folder) records nothing.
 */
internal object SmokeRunReport {
    private const val REPORT_DIR = "notekea.smokeRun.reportDir"
    private const val IDLE_SECONDS = "notekea.smokeRun.idleSeconds"
    private const val FILE_NAME = "memory.csv"
    private const val HEADER = "run,moment,heap_mb,heap_committed_mb,resident_mb,skia_fonts_mb,skia_resources_mb\n"
    private const val BYTES_PER_MEGABYTE = 1_048_576
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
        val line = listOf(
            run,
            moment,
            ProcessMemory.heapUsedMegabytes().toString(),
            (Runtime.getRuntime().totalMemory() / BYTES_PER_MEGABYTE).toString(),
            resident,
            (Graphics.fontCacheUsed / BYTES_PER_MEGABYTE).toString(),
            (Graphics.resourceCacheTotalUsed / BYTES_PER_MEGABYTE).toString()
        )
        file.appendText(text = line.joinToString(separator = SEPARATOR) + "\n")
    }
}
