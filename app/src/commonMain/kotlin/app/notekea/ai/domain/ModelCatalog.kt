package app.notekea.ai.domain

/** The tested chat [models], smallest first, and the [reranker] downloaded with whichever is chosen. */
data class ModelCatalog(
    val models: List<ChatModel>,
    val reranker: ModelFile
)
