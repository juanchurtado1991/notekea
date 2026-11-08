package app.notekea.models.data

import com.ghost.serialization.annotations.GhostSerialization

/** A file of `files/model-catalog.json`. */
@GhostSerialization
data class ModelFileDto(
    val fileName: String,
    val url: String,
    val sizeBytes: Long,
    val sha256: String
)
