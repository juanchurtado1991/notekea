package app.notekea.ai.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.ai.domain.Hardware
import app.notekea.ai.domain.HardwareProbe
import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.withContext
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.math.ceil

/**
 * [HardwareProbe] on the JVM. The RAM the system shows is a little under what is installed (the integrated GPU and
 * the firmware keep some), so it is rounded up to the next usual size. The video memory of a dedicated GPU comes from
 * `nvidia-smi` (NVIDIA, on Linux and Windows) or from `/sys/class/drm` (AMD, on Linux); macOS reports none, since
 * Apple Silicon shares the RAM. Other GPUs are not read yet, and their machines are recommended by RAM.
 */
class JvmHardwareProbe(
    private val operatingSystem: OperatingSystem,
    private val readCommand: (List<String>) -> ByteArray?,
    private val dispatchers: AppDispatchers,
    private val drmFolder: Path = Path.of(DRM_FOLDER)
) : HardwareProbe {
    override suspend fun probe(): Hardware = withContext(context = dispatchers.io) {
        val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        Hardware(memoryGb = installedGb(visibleBytes = bean.totalMemorySize), videoMemoryGb = videoMemoryGb())
    }

    private fun videoMemoryGb(): Int? = when (operatingSystem) {
        OperatingSystem.MACOS -> null
        OperatingSystem.WINDOWS -> nvidiaGb()
        OperatingSystem.LINUX -> nvidiaGb() ?: amdGb()
    }

    private fun nvidiaGb(): Int? = readCommand(NVIDIA_QUERY)
        ?.decodeToString()
        ?.lines()
        ?.mapNotNull { it.trim().toLongOrNull() }
        ?.maxOrNull()
        ?.let { mebibytes -> roundedGb(bytes = mebibytes * MEBIBYTE) }

    private fun amdGb(): Int? = runCatching {
        drmFolder.listDirectoryEntries(glob = CARD_GLOB)
            .map { it.resolve(AMD_VRAM_FILE) }
            .filter { Files.isRegularFile(it) }
            .mapNotNull { Files.readString(it).trim().toLongOrNull() }
            .maxOrNull()
    }.getOrNull()?.let { roundedGb(bytes = it) }?.takeIf { it > 0 }

    private fun roundedGb(
        bytes: Long
    ): Int = Math.round(bytes.toDouble() / GIBIBYTE).toInt()

    companion object {
        private const val AMD_VRAM_FILE = "device/mem_info_vram_total"
        private const val CARD_GLOB = "card[0-9]*"
        private const val DRM_FOLDER = "/sys/class/drm"
        private const val GIBIBYTE = 1073741824.0
        private const val MEBIBYTE = 1048576L
        private val NVIDIA_QUERY = listOf("nvidia-smi", "--query-gpu=memory.total", "--format=csv,noheader,nounits")
        private val USUAL_SIZES_GB = listOf(4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128)

        /** The usual RAM size at or above [visibleBytes], or the gibibytes rounded up past the largest one. */
        fun installedGb(
            visibleBytes: Long
        ): Int {
            val visible = ceil(visibleBytes / GIBIBYTE).toInt()
            return USUAL_SIZES_GB.firstOrNull { it >= visible } ?: visible
        }
    }
}
