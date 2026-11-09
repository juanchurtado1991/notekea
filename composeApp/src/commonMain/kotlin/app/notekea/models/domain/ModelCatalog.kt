package app.notekea.models.domain

/** The tested chat [models], smallest first, and the [reranker] downloaded with whichever is chosen (`ROADMAP.md` 3.1). */
data class ModelCatalog(
    val models: List<ChatModel>,
    val reranker: ModelFile
)
