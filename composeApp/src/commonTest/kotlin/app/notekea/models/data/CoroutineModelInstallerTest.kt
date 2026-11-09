package app.notekea.models.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.FakeClock
import app.notekea.models.domain.FakeActiveModelStore
import app.notekea.models.domain.FakeModelCatalogSource
import app.notekea.models.domain.FakeModelDownloader
import app.notekea.models.domain.FakeModelStorage
import app.notekea.models.domain.InstallModelUseCase
import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.InstallSession
import app.notekea.models.domain.ModelCatalogs
import app.notekea.models.domain.ModelError
import app.notekea.models.domain.ModelSize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/** `ROADMAP.md` 3.1: one installation at a time, paused, resumed or cancelled, that outlives Settings. */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineModelInstallerTest {
    private val testDispatcher = StandardTestDispatcher()
    private val storage = FakeModelStorage()
    private val downloader = FakeModelDownloader(storage = storage)
    private val activeModelStore = FakeActiveModelStore()
    private val installer = CoroutineModelInstaller(
        install = InstallModelUseCase(storage = storage, downloader = downloader),
        catalogSource = FakeModelCatalogSource(),
        storage = storage,
        activeModelStore = activeModelStore,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
        clock = FakeClock(currentTime = Instant.fromEpochMilliseconds(epochMilliseconds = 0))
    )

    @AfterTest
    fun tearDown() {
        installer.close()
    }

    @Test
    fun `an installed model becomes the one in use, and the installation ends`() = runTest(context = testDispatcher) {
        installer.start(model = ModelCatalogs.medium)
        advanceUntilIdle()

        assertEquals(expected = ModelSize.MEDIUM, actual = activeModelStore.kept.value)
        assertNull(actual = installer.session.value)
    }

    @Test
    fun `a paused installation keeps its bytes and resumes from them`() = runTest(context = testDispatcher) {
        storage.installed += ModelCatalogs.reranker.fileName
        downloader.held += ModelCatalogs.small.file.fileName
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()
        installer.pause()
        advanceUntilIdle()
        val paused = installer.session.value
        downloader.held.clear()
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()

        assertEquals(
            expected = InstallSession(
                size = ModelSize.SMALL,
                progress = InstallProgress.Downloading(doneBytes = 50, totalBytes = 100),
                isPaused = true
            ),
            actual = paused
        )
        assertEquals(expected = listOf("small.gguf" to 0L, "small.gguf" to 50L), actual = downloader.calls)
        assertEquals(expected = ModelSize.SMALL, actual = activeModelStore.kept.value)
    }

    @Test
    fun `cancelling deletes what the model downloaded and ends the installation`() = runTest(context = testDispatcher) {
        storage.installed += ModelCatalogs.reranker.fileName
        downloader.held += ModelCatalogs.small.file.fileName
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()
        installer.cancel()
        advanceUntilIdle()

        assertNull(actual = installer.session.value)
        assertEquals(expected = listOf("small.gguf"), actual = storage.deleted)
        assertNull(actual = activeModelStore.kept.value)
    }

    @Test
    fun `a failure stays on show until the next try`() = runTest(context = testDispatcher) {
        downloader.failing += ModelCatalogs.reranker.fileName
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()
        val failed = installer.session.value?.progress
        downloader.failing.clear()
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()

        assertEquals(expected = InstallProgress.Failed(error = ModelError.Network), actual = failed)
        assertNull(actual = installer.session.value)
    }

    @Test
    fun `a resumed installation keeps its total after the reranker finished`() = runTest(context = testDispatcher) {
        downloader.held += ModelCatalogs.small.file.fileName
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()
        installer.pause()
        advanceUntilIdle()
        val paused = installer.session.value?.progress
        installer.start(model = ModelCatalogs.small)
        advanceUntilIdle()

        assertEquals(expected = InstallProgress.Downloading(doneBytes = 90, totalBytes = 140), actual = paused)
        assertEquals(
            expected = InstallProgress.Downloading(doneBytes = 115, totalBytes = 140),
            actual = installer.session.value?.progress
        )
    }

    @Test
    fun `cancelling while the reranker downloads deletes its bytes too, but never a finished reranker`() =
        runTest(context = testDispatcher) {
            downloader.held += ModelCatalogs.reranker.fileName
            installer.start(model = ModelCatalogs.small)
            advanceUntilIdle()
            installer.cancel()
            advanceUntilIdle()
            val firstCancel = storage.deleted.toList()
            storage.deleted.clear()
            downloader.held.clear()
            downloader.held += ModelCatalogs.small.file.fileName
            installer.start(model = ModelCatalogs.small)
            advanceUntilIdle()
            installer.cancel()
            advanceUntilIdle()

            assertEquals(expected = listOf("reranker.gguf", "small.gguf"), actual = firstCancel)
            assertEquals(expected = listOf("small.gguf"), actual = storage.deleted)
            assertEquals(expected = setOf("reranker.gguf"), actual = storage.installed)
        }
}
