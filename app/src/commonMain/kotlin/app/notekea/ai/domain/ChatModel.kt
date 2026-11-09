package app.notekea.ai.domain

/**
 * A chat model of the catalog: its [size], the [name] shown and its [file]. A machine can chat with it when it has
 * [minimumMemoryGb] of RAM, or [minimumMemoryWithGpuGb] with a GPU of [minimumVideoMemoryGb]; the numbers include the
 * editor and the reranker, as measured in `docs/spikes/2026-11-07-llm-parallel.md`.
 */
data class ChatModel(
    val size: ModelSize,
    val name: String,
    val file: ModelFile,
    val minimumMemoryGb: Int,
    val minimumMemoryWithGpuGb: Int,
    val minimumVideoMemoryGb: Int
) {
    /** Whether [hardware] can chat with this model, on the CPU or on its GPU. */
    fun fits(
        hardware: Hardware
    ): Boolean {
        val fitsGpu = (hardware.videoMemoryGb ?: 0) >= minimumVideoMemoryGb &&
            hardware.memoryGb >= minimumMemoryWithGpuGb
        return hardware.memoryGb >= minimumMemoryGb || fitsGpu
    }
}
