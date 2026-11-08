package app.notekea.models.domain

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** `ROADMAP.md` 3.1: the chat model and the reranker install as one, resume, and stop at the first failure. */
class InstallModelUseCaseTest {
    private val storage = FakeModelStorage()
    private val downloader = FakeModelDownloader(storage = storage)
    private val install = InstallModelUseCase(storage = storage, downloader = downloader)

    @Test
    fun `the reranker and the model download in turn, counted as one total, and both are installed`() = runTest {
        val progress = install(model = ModelCatalogs.medium, reranker = ModelCatalogs.reranker).toList()

        val done = progress.filterIsInstance<InstallProgress.Downloading>().map { it.doneBytes to it.totalBytes }
        assertEquals(expected = listOf(0L, 20L, 40L, 40L, 140L, 240L), actual = done.map { it.first })
        assertTrue(actual = done.all { it.second == 240L })
        assertEquals(expected = InstallProgress.Installed, actual = progress.last())
        assertEquals(expected = setOf("reranker.gguf", "medium.gguf"), actual = storage.installed)
    }

    @Test
    fun `a second model skips the installed reranker`() = runTest {
        storage.installed += ModelCatalogs.reranker.fileName

        install(model = ModelCatalogs.small, reranker = ModelCatalogs.reranker).toList()

        assertEquals(expected = listOf("small.gguf" to 0L), actual = downloader.calls)
    }

    @Test
    fun `a paused download resumes from the bytes it kept`() = runTest {
        storage.installed += ModelCatalogs.reranker.fileName
        storage.partial[ModelCatalogs.small.file.fileName] = 60

        val progress = install(model = ModelCatalogs.small, reranker = ModelCatalogs.reranker).toList()

        assertEquals(expected = listOf("small.gguf" to 60L), actual = downloader.calls)
        assertEquals(
            expected = InstallProgress.Downloading(doneBytes = 60, totalBytes = 100),
            actual = progress.first()
        )
    }

    @Test
    fun `a file that downloaded whole but was never checked is only checked`() = runTest {
        storage.installed += ModelCatalogs.reranker.fileName
        storage.partial[ModelCatalogs.small.file.fileName] = 100

        val progress = install(model = ModelCatalogs.small, reranker = ModelCatalogs.reranker).toList()

        assertEquals(expected = emptyList(), actual = downloader.calls)
        assertEquals(expected = InstallProgress.Installed, actual = progress.last())
    }

    @Test
    fun `too little disk says how much to free, before downloading anything`() = runTest {
        storage.freeSpace = 1_000

        val progress = install(model = ModelCatalogs.medium, reranker = ModelCatalogs.reranker).toList()

        val needed = 240L + 500_000_000L - 1_000L
        assertEquals(
            expected = listOf(InstallProgress.Failed(error = ModelError.NotEnoughDisk(neededBytes = needed))),
            actual = progress
        )
        assertEquals(expected = emptyList(), actual = downloader.calls)
    }

    @Test
    fun `a network failure stops before the model, keeping the reranker's bytes`() = runTest {
        downloader.failing += ModelCatalogs.reranker.fileName

        val progress = install(model = ModelCatalogs.medium, reranker = ModelCatalogs.reranker).toList()

        assertEquals(expected = InstallProgress.Failed(error = ModelError.Network), actual = progress.last())
        assertEquals(expected = 20L, actual = storage.partial[ModelCatalogs.reranker.fileName])
        assertEquals(expected = listOf("reranker.gguf" to 0L), actual = downloader.calls)
    }

    @Test
    fun `a damaged file fails its check and nothing is installed`() = runTest {
        storage.installed += ModelCatalogs.reranker.fileName
        storage.corrupted += ModelCatalogs.small.file.fileName

        val progress = install(model = ModelCatalogs.small, reranker = ModelCatalogs.reranker).toList()

        assertEquals(
            expected = listOf(InstallProgress.Verifying, InstallProgress.Failed(error = ModelError.ChecksumMismatch)),
            actual = progress.takeLast(n = 2)
        )
        assertEquals(expected = setOf("reranker.gguf"), actual = storage.installed)
    }

    @Test
    fun `a resumed installation counts the files it began with, installed ones as done`() = runTest {
        storage.installed += ModelCatalogs.reranker.fileName
        storage.partial[ModelCatalogs.small.file.fileName] = 60

        val progress = install(
            model = ModelCatalogs.small,
            reranker = ModelCatalogs.reranker,
            counted = listOf(ModelCatalogs.reranker, ModelCatalogs.small.file)
        ).toList()

        assertEquals(
            expected = InstallProgress.Downloading(doneBytes = 100, totalBytes = 140),
            actual = progress.first()
        )
        assertEquals(expected = listOf("small.gguf" to 60L), actual = downloader.calls)
    }
}
