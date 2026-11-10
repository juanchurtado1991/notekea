package app.notekea.ai.domain

/**
 * How the model in use loads on this machine: its file at [modelPath] and the reranker's at [rerankerPath], with
 * [slots] replies side by side, [contextTokens] for each, and every layer on the GPU when [onGpu].
 */
data class ModelSetup(
    val size: ModelSize,
    val modelPath: String,
    val rerankerPath: String,
    val slots: Int,
    val contextTokens: Int,
    val onGpu: Boolean
) {
    companion object {
        /** A slot's context: the pipeline uses about 2k tokens (docs/spikes/2026-11-07-llm-parallel.md). */
        const val CONTEXT_TOKENS = 4_096
        private const val GPU_SLOTS = 4
        private const val LARGE_MEMORY_GB = 16
        private const val LARGE_MEMORY_SLOTS = 2

        /**
         * The setup of [model] on [hardware]: on its GPU with 4 slots when the GPU holds the model (Apple silicon's
         * GPU shares the RAM, [sharesMemoryWithGpu]), on the CPU otherwise, with 2 slots from 16 GB of RAM and 1
         * below, as the 3.0 spike measured.
         */
        fun of(
            model: ChatModel,
            hardware: Hardware,
            sharesMemoryWithGpu: Boolean,
            modelPath: String,
            rerankerPath: String
        ): ModelSetup {
            val isSharedGpu =
                sharesMemoryWithGpu && hardware.memoryGb >= model.minimumMemoryWithGpuGb

            val onGpu = isSharedGpu || (hardware.videoMemoryGb ?: 0) >= model.minimumVideoMemoryGb

            val slots = when {
                onGpu -> GPU_SLOTS
                hardware.memoryGb >= LARGE_MEMORY_GB -> LARGE_MEMORY_SLOTS
                else -> 1
            }

            return ModelSetup(
                size = model.size,
                modelPath = modelPath,
                rerankerPath = rerankerPath,
                slots = slots,
                contextTokens = CONTEXT_TOKENS,
                onGpu = onGpu
            )
        }
    }
}
