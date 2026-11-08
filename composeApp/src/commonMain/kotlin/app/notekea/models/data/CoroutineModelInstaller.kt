package app.notekea.models.data

import app.notekea.models.domain.ActiveModelStore
import app.notekea.models.domain.ChatModel
import app.notekea.models.domain.InstallModelUseCase
import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.InstallSession
import app.notekea.models.domain.ModelCatalogSource
import app.notekea.models.domain.ModelInstaller
import app.notekea.models.domain.ModelStorage
import app.notekea.core.domain.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [ModelInstaller] on a coroutine of its own scope, which outlives every window and dialog until [close]. Pausing cancels the
 * coroutine and keeps the downloaded bytes, so starting the same model again resumes it.
 */
class CoroutineModelInstaller(
    private val install: InstallModelUseCase,
    private val catalogSource: ModelCatalogSource,
    private val storage: ModelStorage,
    private val activeModelStore: ActiveModelStore,
    dispatchers: AppDispatchers
) : ModelInstaller {
    private val scope = CoroutineScope(context = SupervisorJob() + dispatchers.io)
    private val current = MutableStateFlow<InstallSession?>(value = null)
    private val job = MutableStateFlow<Job?>(value = null)

    override val session: StateFlow<InstallSession?> = current.asStateFlow()

    override fun start(
        model: ChatModel
    ) {
        val previous = job.value
        current.value =
            InstallSession(size = model.size, progress = InstallProgress.Downloading(doneBytes = 0, totalBytes = 0))
        job.value = scope.launch {
            previous?.cancelAndJoin()
            val reranker = catalogSource.load().reranker
            install(model = model, reranker = reranker).collect { progress ->
                current.update { it?.copy(progress = progress) }
                if (progress == InstallProgress.Installed) {
                    activeModelStore.save(size = model.size)
                    current.value = null
                }
            }
        }
    }

    override fun pause() {
        job.value?.cancel()
        current.update { it?.copy(isPaused = true) }
    }

    override fun cancel() {
        val size = current.value?.size ?: return
        val previous = job.value
        current.value = null
        job.value = scope.launch {
            previous?.cancelAndJoin()
            catalogSource.load().models.firstOrNull { it.size == size }?.let { storage.delete(file = it.file) }
        }
    }

    /** Stops every download, keeping their bytes; the installer is not used after it. */
    fun close() {
        scope.cancel()
    }
}
