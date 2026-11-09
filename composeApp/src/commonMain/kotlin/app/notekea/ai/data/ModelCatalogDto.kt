package app.notekea.ai.data

import com.ghost.serialization.annotations.GhostSerialization

/** `files/model-catalog.json`, the catalog of tested models bundled with the app. */
@GhostSerialization
data class ModelCatalogDto(
    val models: List<ChatModelDto>,
    val reranker: ModelFileDto
)
