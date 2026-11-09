package app.notekea.ai.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.platform.domain.OperatingSystem
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The memory read as the size people know their computer by, and the video memory of its GPU. */
@OptIn(ExperimentalPathApi::class)
class JvmHardwareProbeTest {
    private val drm = Files.createTempDirectory(TEMP_PREFIX)

    @AfterTest
    fun tearDown() {
        drm.deleteRecursively()
    }

    private fun probe(
        operatingSystem: OperatingSystem,
        nvidia: String?
    ) = JvmHardwareProbe(
        operatingSystem = operatingSystem,
        readCommand = { nvidia?.encodeToByteArray() },
        dispatchers = JvmAppDispatchers(),
        drmFolder = drm
    )

    @Test
    fun `the memory the system shows rounds up to the usual size`() {
        val sizes = listOf(7.3, 14.1, 15.6, 22.9, 30.8, 62.5, 200.2).map {
            JvmHardwareProbe.installedGb(visibleBytes = (it * GIBIBYTE).toLong())
        }

        assertEquals(
            expected = listOf(8, 16, 16, 24, 32, 64, 201),
            actual = sizes
        )
    }

    @Test
    fun `an NVIDIA GPU reports its largest card in gigabytes`() = runBlocking {
        val hardware = probe(
            operatingSystem = OperatingSystem.LINUX,
            nvidia = "16311\n8192\n"
        ).probe()

        assertEquals(
            expected = 16,
            actual = hardware.videoMemoryGb
        )

        assertTrue(actual = hardware.memoryGb > 0)
    }

    @Test
    fun `without nvidia-smi an AMD card is read on Linux, and nothing on macOS`() = runBlocking {
        drm
            .resolve("card0/device")
            .createDirectories()
            .resolve("mem_info_vram_total")
            .writeText(text = "8573157376\n")

        val linux = probe(
            operatingSystem = OperatingSystem.LINUX,
            nvidia = null
        ).probe().videoMemoryGb

        val windows = probe(
            operatingSystem = OperatingSystem.WINDOWS,
            nvidia = null
        ).probe().videoMemoryGb

        val macos = probe(
            operatingSystem = OperatingSystem.MACOS,
            nvidia = "8192"
        ).probe().videoMemoryGb

        assertEquals(
            expected = listOf(8, null, null),
            actual = listOf(linux, windows, macos)
        )
    }

    private companion object {
        const val GIBIBYTE = 1073741824.0
        const val TEMP_PREFIX = "notekea-drm"
    }
}
