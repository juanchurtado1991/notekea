package app.notekea.ai.domain

/** Where the catalog of tested models comes from: the copy bundled with the app. */
interface ModelCatalogSource {
    suspend fun load(): ModelCatalog
}
