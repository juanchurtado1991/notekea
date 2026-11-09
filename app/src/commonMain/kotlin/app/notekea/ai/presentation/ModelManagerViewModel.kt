package app.notekea.ai.presentation

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import app.notekea.ai.domain.ActiveModelStore
import app.notekea.ai.domain.HardwareProbe
import app.notekea.ai.domain.ModelCatalog
import app.notekea.ai.domain.ModelCatalogSource
import app.notekea.ai.domain.ModelInstaller
import app.notekea.ai.domain.ModelSize
import app.notekea.ai.domain.ModelStorage
import app.notekea.ai.domain.RecommendModelUseCase
import co.touchlab.kermit.Logger

/**
 * Settings → AI model (`ROADMAP.md` 3.1): reads the catalog and this computer once, recommends a size, and follows the
 * model in use and the installation, which runs in [ModelInstaller] and so goes on when Settings closes.
 */
class ModelManagerViewModel(
    private val catalogSource: ModelCatalogSource,
    private val hardwareProbe: HardwareProbe,
    private val storage: ModelStorage,
    private val activeModelStore: ActiveModelStore,
    private val installer: ModelInstaller,
    private val recommendModel: RecommendModelUseCase,
    platformConventions: PlatformConventions
) : BaseViewModel<ModelManagerState, ModelManagerIntent, Nothing>(
    initialState = ModelManagerState(buttonOrder = platformConventions.dialogButtonOrder)
) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    /** The catalog once read, to find a size's files; the state keeps only what the screen shows. */
    private var loadedCatalog: ModelCatalog? = null

    init {
        launchGuarded(onError = ::onLoadError) { load() }
        launchGuarded(onError = ::onLoadError) {
            activeModelStore.observe().collect { active -> reduce { it.copy(active = active) } }
        }
        launchGuarded(onError = ::onLoadError) {
            installer.session.collect { session ->
                val finished = session == null && state.value.session != null
                reduce { it.copy(session = session) }
                if (finished) refreshInstalled()
            }
        }
    }

    override fun handleIntent(
        intent: ModelManagerIntent
    ) {
        when (intent) {
            is ModelManagerIntent.OnInstall -> modelOf(size = intent.size)?.let { installer.start(model = it) }

            ModelManagerIntent.OnPause -> installer.pause()

            ModelManagerIntent.OnCancel -> installer.cancel()

            is ModelManagerIntent.OnUse -> launchGuarded(onError = ::onLoadError) {
                activeModelStore.save(size = intent.size)
            }

            is ModelManagerIntent.OnDeleteAsk -> reduce { it.copy(pendingDelete = intent.size) }

            ModelManagerIntent.OnDeleteConfirm -> deletePending()

            ModelManagerIntent.OnDeleteDismiss -> reduce { it.copy(pendingDelete = null) }

            ModelManagerIntent.OnOtherSizesToggle -> reduce { it.copy(isOtherSizesShown = !it.isOtherSizesShown) }
        }
    }

    private suspend fun load() {
        val catalog = catalogSource.load()
        val hardware = hardwareProbe.probe()
        val rows = catalog.models.map { model ->
            ModelRow(
                size = model.size,
                name = model.name,
                downloadBytes = model.file.sizeBytes,
                isInstalled = storage.isInstalled(file = model.file),
                fits = model.fits(hardware = hardware),
                minimumMemoryGb = model.minimumMemoryGb
            )
        }
        loadedCatalog = catalog
        val rerankerInstalled = storage.isInstalled(file = catalog.reranker)
        reduce {
            it.copy(
                isLoading = false,
                memoryGb = hardware.memoryGb,
                models = rows,
                recommended = recommendModel(catalog = catalog, hardware = hardware).size,
                rerankerBytes = catalog.reranker.sizeBytes,
                isRerankerInstalled = rerankerInstalled,
                folder = storage.folder
            )
        }
    }

    private suspend fun refreshInstalled() {
        val catalog = loadedCatalog ?: return
        val installed = catalog.models.associate { it.size to storage.isInstalled(file = it.file) }
        val rerankerInstalled = storage.isInstalled(file = catalog.reranker)
        reduce { state ->
            state.copy(
                models = state.models.map { it.copy(isInstalled = installed[it.size] == true) },
                isRerankerInstalled = rerankerInstalled
            )
        }
    }

    private fun deletePending() {
        val size = state.value.pendingDelete ?: return
        reduce { it.copy(pendingDelete = null) }
        launchGuarded(onError = ::onLoadError) {
            modelOf(size = size)?.let { storage.delete(file = it.file) }
            if (state.value.active == size) activeModelStore.save(size = null)
            deleteRerankerIfAlone()
            refreshInstalled()
        }
    }

    /** The reranker only serves the chat models, so it goes with the last of them. */
    private suspend fun deleteRerankerIfAlone() {
        val catalog = loadedCatalog ?: return
        if (catalog.models.none { storage.isInstalled(file = it.file) }) storage.delete(file = catalog.reranker)
    }

    private fun modelOf(
        size: ModelSize
    ) = loadedCatalog?.models?.firstOrNull { it.size == size }

    private fun onLoadError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false, hasLoadFailed = true) }
    }

    private companion object {
        const val LOG_TAG = "ModelManagerViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the model manager"
    }
}
