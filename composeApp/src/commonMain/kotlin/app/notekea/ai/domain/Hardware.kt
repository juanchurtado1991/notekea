package app.notekea.ai.domain

/**
 * What the machine offers to a model: its [memoryGb] of RAM, and the [videoMemoryGb] of its dedicated GPU, or null
 * when it has none or it could not be read. Apple Silicon reports no video memory: its GPU shares the RAM.
 */
data class Hardware(
    val memoryGb: Int,
    val videoMemoryGb: Int?
)
