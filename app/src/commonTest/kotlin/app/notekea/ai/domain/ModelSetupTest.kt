package app.notekea.ai.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelSetupTest {
    private val medium = ChatModel(
        size = ModelSize.MEDIUM,
        name = "Qwen3.5 4B",
        file = ModelFile(
            fileName = "m.gguf",
            url = "https://example.com/m.gguf",
            sizeBytes = 1,
            sha256 = "0"
        ),
        minimumMemoryGb = 16,
        minimumMemoryWithGpuGb = 8,
        minimumVideoMemoryGb = 4
    )

    @Test
    fun `a GPU that holds the model gets every layer and 4 slots, the CPU 2 from 16 GB and 1 below`() {
        assertEquals(
            expected = listOf(true to 4, false to 2, false to 1, true to 4, false to 1),
            actual = listOf(
                setup(
                    memoryGb = 16,
                    videoMemoryGb = 8
                ),
                setup(
                    memoryGb = 32,
                    videoMemoryGb = 2
                ),
                setup(
                    memoryGb = 8,
                    videoMemoryGb = null
                ),
                setup(
                    memoryGb = 16,
                    videoMemoryGb = null,
                    sharesMemoryWithGpu = true
                ),
                setup(
                    memoryGb = 4,
                    videoMemoryGb = null,
                    sharesMemoryWithGpu = true
                )
            ).map {
                it.onGpu to it.slots
            }
        )
    }

    private fun setup(
        memoryGb: Int,
        videoMemoryGb: Int?,
        sharesMemoryWithGpu: Boolean = false
    ): ModelSetup = ModelSetup.of(
        model = medium,
        hardware = Hardware(
            memoryGb = memoryGb,
            videoMemoryGb = videoMemoryGb
        ),
        sharesMemoryWithGpu = sharesMemoryWithGpu,
        modelPath = "m.gguf",
        rerankerPath = "r.gguf"
    )
}
