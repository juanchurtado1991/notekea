package app.notekea.ai.data

import app.notekea.ai.domain.ActiveModelStore
import app.notekea.ai.domain.HardwareProbe
import app.notekea.ai.domain.ModelCatalogSource
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSetupSource
import app.notekea.ai.domain.ModelStorage
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.LlmError
import kotlinx.coroutines.flow.first

/**
 * [ModelSetupSource] on what the model manager installed (3.1): the size in use, its file and the reranker's in the
 * models [folder], and how the machine runs it. [sharesMemoryWithGpu] is true on Apple silicon.
 */
class InstalledModelSetupSource(
    private val activeModelStore: ActiveModelStore,
    private val catalogSource: ModelCatalogSource,
    private val storage: ModelStorage,
    private val hardwareProbe: HardwareProbe,
    private val folder: ModelFolder,
    private val sharesMemoryWithGpu: Boolean
) : ModelSetupSource {
    override suspend fun current(): Outcome<ModelSetup, LlmError> {
        val size =
            activeModelStore.observe().first() ?: return Outcome.Failure(error = LlmError.NoModel)

        val catalog = catalogSource.load()

        val model = catalog.models.firstOrNull {
            it.size == size
        }
            ?: return Outcome.Failure(error = LlmError.NoModel)

        val isInstalled =
            storage.isInstalled(file = model.file) && storage.isInstalled(file = catalog.reranker)

        if (!isInstalled) return Outcome.Failure(error = LlmError.NoModel)

        val hardware = hardwareProbe.probe()
        val fits = model.fits(hardware = hardware)
        if (!fits) return Outcome.Failure(error = LlmError.NotEnoughMemory)

        return Outcome.Success(
            value = ModelSetup.of(
                model = model,
                hardware = hardware,
                sharesMemoryWithGpu = sharesMemoryWithGpu,
                modelPath = folder.installed(file = model.file).toString(),
                rerankerPath = folder.installed(file = catalog.reranker).toString()
            )
        )
    }
}
