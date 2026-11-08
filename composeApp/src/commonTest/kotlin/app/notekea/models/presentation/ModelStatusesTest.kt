package app.notekea.models.presentation

import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.InstallSession
import app.notekea.models.domain.ModelError
import app.notekea.models.domain.ModelSize
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
}
