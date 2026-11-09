package app.notekea.ai.presentation

import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelError
import app.notekea.ai.domain.ModelSize
import kotlin.test.Test
import kotlin.test.assertEquals

/** What each size shows in Settings → AI model, and its sizes in gigabytes. */
class ModelStatusesTest {
    @Test
    fun `a failure belongs to its size, and the others can still be downloaded`() {
        val state = ModelManagerSamples.notInstalled.copy(
            session = InstallSession(
                size = ModelSize.MEDIUM,
                progress = InstallProgress.Failed(error = ModelError.Network)
            )
        )

        assertEquals(
            expected = listOf(
                ModelStatus.NotInstalled,
                ModelStatus.Failed(error = ModelError.Network),
                ModelStatus.NotInstalled
            ),
            actual = ModelSize.entries.map { state.statusOf(size = it) }
        )
    }

    @Test
    fun `sizes read in gigabytes with one decimal, and small ones in megabytes`() {
        val texts = listOf(2_740_937_888L, 438_376_864L, 5_680_522_464L, 42_000_000L, 0L).map {
            formatBytes(bytes = it)
        }

        assertEquals(expected = listOf("2.7 GB", "0.4 GB", "5.7 GB", "42 MB", "0 MB"), actual = texts)
    }

    @Test
    fun `speeds read in the largest unit above one, with one decimal`() {
        val texts = listOf(24_500_000L, 1_250_000_000L, 850_400L, 999L).map { formatSpeed(bytesPerSecond = it) }

        assertEquals(expected = listOf("24.5 MB/s", "1.3 GB/s", "850 KB/s", "1 KB/s"), actual = texts)
    }

    @Test
    fun `deleting the last model frees the reranker too, and another model keeps it`() {
        val medium = ModelManagerSamples.medium.copy(isInstalled = true)
        val last = ModelManagerSamples.notInstalled.copy(
            models = listOf(ModelManagerSamples.small, medium),
            isRerankerInstalled = true
        )
        val notLast = last.copy(models = listOf(ModelManagerSamples.small.copy(isInstalled = true), medium))

        assertEquals(
            expected = listOf(medium.downloadBytes + last.rerankerBytes, medium.downloadBytes),
            actual = listOf(last.freedBy(row = medium), notLast.freedBy(row = medium))
        )
    }
}
