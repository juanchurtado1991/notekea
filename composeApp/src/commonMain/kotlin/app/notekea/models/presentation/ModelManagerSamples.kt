package app.notekea.models.presentation

import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.InstallSession
import app.notekea.models.domain.ModelSize

/** Sample states of Settings → AI model for the previews, on a 16 GB computer. */
internal object ModelManagerSamples {
    val small = ModelRow(
        size = ModelSize.SMALL,
        name = "Qwen3.5 2B",
        downloadBytes = 1_280_835_840,
        isInstalled = false,
        fits = true,
        minimumMemoryGb = 8
    )
    val medium = small.copy(
        size = ModelSize.MEDIUM,
        name = "Qwen3.5 4B",
        downloadBytes = 2_740_937_888,
        minimumMemoryGb = 16
    )
    val large = small.copy(
        size = ModelSize.LARGE,
        name = "Qwen3.5 9B",
        downloadBytes = 5_680_522_464,
        fits = false,
        minimumMemoryGb = 32
    )

    val notInstalled = ModelManagerState(
        isLoading = false,
        memoryGb = 16,
        models = listOf(small, medium, large),
        recommended = ModelSize.MEDIUM,
        rerankerBytes = 438_376_864,
        folder = "/home/me/.local/share/notekea/models"
    )
    val downloading = notInstalled.copy(
        session = InstallSession(
            size = ModelSize.MEDIUM,
            progress = InstallProgress.Downloading(
                doneBytes = 1_940_000_000,
                totalBytes = 3_179_314_752,
                bytesPerSecond = 24_500_000
            )
        )
    )
    val inUse = notInstalled.copy(
        models = listOf(small, medium.copy(isInstalled = true), large),
        active = ModelSize.MEDIUM,
        isRerankerInstalled = true,
        isOtherSizesShown = true
    )
}
