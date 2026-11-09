package app.notekea.models.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.models.domain.ModelCatalog
import app.notekea.models.domain.ModelCatalogSource
import app.notekea.resources.Res
import com.ghost.serialization.Ghost
import kotlinx.coroutines.withContext

/**
 * [ModelCatalogSource] on `files/model-catalog.json` in the app's resources, read once. The catalog ships with the
 * app, so a model only changes with a new version, after the evaluation set (3.4) has tested it.
 */
class BundledModelCatalogSource(
    private val dispatchers: AppDispatchers
) : ModelCatalogSource {
    private var catalog: ModelCatalog? = null

    override suspend fun load(): ModelCatalog = catalog ?: withContext(context = dispatchers.io) {
        Ghost.deserialize<ModelCatalogDto>(Res.readBytes(path = CATALOG_PATH).decodeToString()).toDomain()
    }.also { catalog = it }

    private companion object {
        const val CATALOG_PATH = "files/model-catalog.json"
    }
}
