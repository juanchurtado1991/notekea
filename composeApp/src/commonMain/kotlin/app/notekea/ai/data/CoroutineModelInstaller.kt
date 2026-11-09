package app.notekea.ai.data

import app.notekea.ai.domain.ActiveModelStore
import app.notekea.ai.domain.ChatModel
import app.notekea.ai.domain.InstallModelUseCase
import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelCatalogSource
import app.notekea.ai.domain.ModelInstaller
import app.notekea.ai.domain.ModelFile
import app.notekea.ai.domain.ModelSize
import app.notekea.ai.domain.ModelStorage
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
import kotlin.time.Clock

/**
 * [ModelInstaller] on a coroutine of its own scope, which outlives every window and dialog until [close]. Pausing cancels the
 * coroutine and keeps the downloaded bytes, so starting the same model again resumes it.
 */
class CoroutineModelInstaller(
    private val install: InstallModelUseCase,
    private val catalogSource: ModelCatalogSource,
    private val storage: ModelStorage,
    private val activeModelStore: ActiveModelStore,
    dispatchers: AppDispatchers,
    private val clock: Clock
) : ModelInstaller {
    private val scope = CoroutineScope(context = SupervisorJob() + dispatchers.io)
    private val current = MutableStateFlow<InstallSession?>(value = null)
    private val job = MutableStateFlow<Job?>(value = null)
    private val counted = MutableStateFlow<Counted?>(value = null)

    override val session: StateFlow<InstallSession?> = current.asStateFlow()

    override fun start(
        model: ChatModel
    ) {
        val previous = job.value
        val resumed = counted.value?.takeIf { it.size == model.size }?.files
        current.value =
            InstallSession(size = model.size, progress = InstallProgress.Downloading(doneBytes = 0, totalBytes = 0))
        job.value = scope.launch {
            previous?.cancelAndJoin()
            val reranker = catalogSource.load().reranker
            val files = resumed ?: listOf(reranker, model.file).filterNot { storage.isInstalled(file = it) }
            counted.value = Counted(size = model.size, files = files)
            val speed = DownloadSpeedMeter(clock = clock)
            install(model = model, reranker = reranker, counted = files).collect { progress ->
                val measured = (progress as? InstallProgress.Downloading)
                    ?.let { it.copy(bytesPerSecond = speed.measure(doneBytes = it.doneBytes)) }
                    ?: progress
                current.update { it?.copy(progress = measured) }
                if (progress == InstallProgress.Installed) {
                    activeModelStore.save(size = model.size)
                    counted.value = null
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
        val started = counted.value?.takeIf { it.size == size }?.files.orEmpty()
        val previous = job.value
        current.value = null
        counted.value = null
        job.value = scope.launch {
            previous?.cancelAndJoin()
            val model = catalogSource.load().models.firstOrNull { it.size == size }?.file
            (started + listOfNotNull(model)).distinct()
                .filterNot { it != model && storage.isInstalled(file = it) }
                .forEach { storage.delete(file = it) }
        }
    }

    /** The files an installation of [size] counted when it began, kept while it is paused or failed. */
    private data class Counted(
        val size: ModelSize,
        val files: List<ModelFile>
    )

    /** Stops every download, keeping their bytes; the installer is not used after it. */
    fun close() {
        scope.cancel()
    }
}
