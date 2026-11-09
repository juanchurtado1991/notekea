package app.notekea.ai.data

import com.ghost.serialization.annotations.GhostSerialization

/** A chat model of `files/model-catalog.json`; [size] is a `ModelSize` id. */
@GhostSerialization
data class ChatModelDto(
    val size: String,
    val name: String,
    val file: ModelFileDto,
    val minimumMemoryGb: Int,
    val minimumMemoryWithGpuGb: Int,
    val minimumVideoMemoryGb: Int
)
