package app.notekea.ai.domain

/** [ModelCatalogSource] that returns [catalog]. */
class FakeModelCatalogSource(
    private val catalog: ModelCatalog = ModelCatalogs.catalog
) : ModelCatalogSource {
    override suspend fun load(): ModelCatalog = catalog
}
