package app.notekea.smokerun

import org.jetbrains.skia.Graphics
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

/**
 * What a performance pass reads from the automated smoke runs: a line per sample in `memory.csv` (the run, the moment,
 * the Java heap in use and held, the process's resident memory and its peak, Skia's caches, and the seconds since the
 * sample before), in the folder the `smokeRun` Gradle task names; a plain test run records nothing.
 */
internal object SmokeRunReport {
    private const val REPORT_DIR = "notekea.smokeRun.reportDir"
    private const val IDLE_SECONDS = "notekea.smokeRun.idleSeconds"
    private const val FILE_NAME = "memory.csv"
    private const val HEADER =
        "run,moment,heap_mb,heap_committed_mb,resident_mb,peak_mb,skia_fonts_mb,skia_resources_mb,seconds\n"
    private const val BYTES_PER_MEGABYTE = 1_048_576
    private const val SEPARATOR = ","
    private const val NANOS_PER_SECOND = 1_000_000_000.0
    private const val PEAK_MILLIS = 200L
    private val folder: File? get() = System.getProperty(REPORT_DIR)?.let(::File)
    private val peak = AtomicLong()
    private var lastSample = System.nanoTime()

    init {
        // Peaks between samples are what a budget is about, and a sample only sees the moment after the work.
        if (folder != null) {
            thread(isDaemon = true) {
                while (true) {
                    ProcessMemory.residentMegabytes()?.let { resident ->
                        peak.accumulateAndGet(resident, ::maxOf)
                    }

                    Thread.sleep(PEAK_MILLIS)
                }
            }
        }
    }

    /** How long the idle run waits with a vault open, in seconds: 0 in a plain test run. */
    val idleSeconds: Long get() = System.getProperty(IDLE_SECONDS)?.toLongOrNull() ?: 0L

    /** Samples the memory now, as [moment] of [run]. */
    fun sample(
        run: String,
        moment: String
    ) {
        val seconds = (System.nanoTime() - lastSample) / NANOS_PER_SECOND
        val folder = folder ?: return
        folder.mkdirs()
        val file = File(folder, FILE_NAME)
        if (!file.exists()) file.writeText(text = HEADER)
        val resident = ProcessMemory.residentMegabytes()

        val line = listOf(
            run,
            moment,
            ProcessMemory.heapUsedMegabytes().toString(),
            (Runtime.getRuntime().totalMemory() / BYTES_PER_MEGABYTE).toString(),
            resident?.toString().orEmpty(),
            maxOf(peak.getAndSet(0L), resident ?: 0L).toString(),
            (Graphics.fontCacheUsed / BYTES_PER_MEGABYTE).toString(),
            (Graphics.resourceCacheTotalUsed / BYTES_PER_MEGABYTE).toString(),
            "%.1f".format(Locale.ROOT, seconds)
        )

        file.appendText(text = line.joinToString(separator = SEPARATOR) + "\n")
        lastSample = System.nanoTime()
    }
}
