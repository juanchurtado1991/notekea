package app.notekea.models.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** `ROADMAP.md` 3.1: the size comes from the memory and the GPU, with nothing technical to choose. */
class RecommendModelUseCaseTest {
    private val recommend = RecommendModelUseCase()

    @Test
    fun `the memory alone picks Small on 8 GB, Medium on 16 GB and Large on 32 GB`() {
        val sizes = listOf(8, 16, 32, 64).map {
            recommend(catalog = ModelCatalogs.catalog, hardware = Hardware(memoryGb = it, videoMemoryGb = null)).size
        }

        assertEquals(expected = listOf(ModelSize.SMALL, ModelSize.MEDIUM, ModelSize.LARGE, ModelSize.LARGE), actual = sizes)
    }

    @Test
    fun `a GPU that holds a larger model moves the choice up, if the memory carries the rest`() {
        val sixteenWithGpu = Hardware(memoryGb = 16, videoMemoryGb = 8)
        val eightWithGpu = Hardware(memoryGb = 8, videoMemoryGb = 12)

        val sizes = listOf(sixteenWithGpu, eightWithGpu).map { recommend(catalog = ModelCatalogs.catalog, hardware = it).size }

        assertEquals(expected = listOf(ModelSize.LARGE, ModelSize.MEDIUM), actual = sizes)
    }

    @Test
    fun `a computer below every minimum still gets the smallest model`() {
        val size = recommend(catalog = ModelCatalogs.catalog, hardware = Hardware(memoryGb = 4, videoMemoryGb = null)).size

        assertEquals(expected = ModelSize.SMALL, actual = size)
    }

    @Test
    fun `a model fits on the CPU or on the GPU, never on a GPU without the memory for the rest`() {
        val fits = listOf(
            Hardware(memoryGb = 32, videoMemoryGb = null),
            Hardware(memoryGb = 16, videoMemoryGb = 8),
            Hardware(memoryGb = 8, videoMemoryGb = 24),
            Hardware(memoryGb = 24, videoMemoryGb = 6)
        ).map { ModelCatalogs.large.fits(hardware = it) }

        assertEquals(expected = listOf(true, true, false, false), actual = fits)
    }
}
