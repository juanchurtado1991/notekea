package app.notekea.ai.presentation

import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.ai.domain.FakeActiveModelStore
import app.notekea.ai.domain.FakeHardwareProbe
import app.notekea.ai.domain.FakeModelCatalogSource
import app.notekea.ai.domain.FakeModelInstaller
import app.notekea.ai.domain.FakeModelStorage
import app.notekea.ai.domain.Hardware
import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelCatalogs
import app.notekea.ai.domain.ModelSize
import app.notekea.ai.domain.RecommendModelUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Settings → AI model (`ROADMAP.md` 3.1): the recommended size first, then what the person does with each size. */
@OptIn(ExperimentalCoroutinesApi::class)
class ModelManagerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val storage = FakeModelStorage()
    private val hardware = FakeHardwareProbe()
    private val activeModelStore = FakeActiveModelStore()
    private val installer = FakeModelInstaller()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ModelManagerViewModel(
        catalogSource = FakeModelCatalogSource(),
        hardwareProbe = hardware,
        storage = storage,
        activeModelStore = activeModelStore,
        installer = installer,
        recommendModel = RecommendModelUseCase(),
        platformConventions = PreviewPlatformConventions.linux
    )

    @Test
    fun `a 16 GB computer is shown Medium first, with what Large lacks`() = runTest(context = testDispatcher) {
        val viewModel = viewModel()
        val loading = viewModel.state.value.isLoading
        advanceUntilIdle()
        val state = viewModel.state.value

        assertEquals(expected = true, actual = loading)
        assertEquals(expected = ModelSize.MEDIUM to ModelSize.MEDIUM, actual = state.recommended to state.featured)
        assertEquals(expected = listOf(true, true, false), actual = state.models.map { it.fits })
        assertEquals(expected = ModelStatus.NotInstalled, actual = state.statusOf(size = ModelSize.MEDIUM))
    }

    @Test
    fun `the model in use is shown first, even when another size is recommended`() = runTest(context = testDispatcher) {
        storage.installed += ModelCatalogs.small.file.fileName
        activeModelStore.kept.value = ModelSize.SMALL
        hardware.hardware = Hardware(memoryGb = 32, videoMemoryGb = null)
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(expected = ModelSize.LARGE to ModelSize.SMALL, actual = state.recommended to state.featured)
        assertEquals(expected = ModelStatus.InUse, actual = state.statusOf(size = ModelSize.SMALL))
    }

    @Test
    fun `download, pause, resume and cancel go to the installer`() = runTest(context = testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(intent = ModelManagerIntent.OnInstall(size = ModelSize.MEDIUM))
        viewModel.onIntent(intent = ModelManagerIntent.OnPause)
        viewModel.onIntent(intent = ModelManagerIntent.OnInstall(size = ModelSize.MEDIUM))
        viewModel.onIntent(intent = ModelManagerIntent.OnCancel)

        assertEquals(expected = listOf(ModelSize.MEDIUM, ModelSize.MEDIUM), actual = installer.started)
        assertEquals(expected = 1 to 1, actual = installer.pauses to installer.cancels)
    }

    @Test
    fun `while one size installs, the others wait, and its end shows it downloaded`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = viewModel()
        advanceUntilIdle()
        installer.current.value = InstallSession(
            size = ModelSize.MEDIUM,
            progress = InstallProgress.Downloading(doneBytes = 10, totalBytes = 240)
        )
        advanceUntilIdle()
        val during = ModelSize.entries.map { viewModel.state.value.statusOf(size = it) }
        storage.installed += ModelCatalogs.medium.file.fileName
        storage.installed += ModelCatalogs.reranker.fileName
        installer.current.value = null
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                ModelStatus.Waiting,
                ModelStatus.Installing(
                    progress = InstallProgress.Downloading(doneBytes = 10, totalBytes = 240),
                    isPaused = false
                ),
                ModelStatus.Waiting
            ),
            actual = during
        )
        assertEquals(expected = ModelStatus.Installed, actual = viewModel.state.value.statusOf(size = ModelSize.MEDIUM))
        assertEquals(expected = true, actual = viewModel.state.value.isRerankerInstalled)
    }

    @Test
    fun `deleting the model in use asks first, then frees it and leaves none in use`() = runTest(
        context = testDispatcher
    ) {
        storage.installed += ModelCatalogs.medium.file.fileName
        activeModelStore.kept.value = ModelSize.MEDIUM
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(intent = ModelManagerIntent.OnDeleteAsk(size = ModelSize.MEDIUM))
        val asked = viewModel.state.value.pendingDelete
        viewModel.onIntent(intent = ModelManagerIntent.OnDeleteConfirm)
        advanceUntilIdle()

        assertEquals(expected = ModelSize.MEDIUM, actual = asked)
        assertEquals(expected = listOf("medium.gguf", "reranker.gguf"), actual = storage.deleted)
        assertEquals(
            expected = null to null,
            actual =
            activeModelStore.kept.value to viewModel.state.value.pendingDelete
        )
        assertEquals(
            expected = ModelStatus.NotInstalled,
            actual = viewModel.state.value.statusOf(size = ModelSize.MEDIUM)
        )
    }

    @Test
    fun `dismissing the question deletes nothing, and Use switches the model`() = runTest(context = testDispatcher) {
        storage.installed += ModelCatalogs.small.file.fileName
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(intent = ModelManagerIntent.OnDeleteAsk(size = ModelSize.SMALL))
        viewModel.onIntent(intent = ModelManagerIntent.OnDeleteDismiss)
        viewModel.onIntent(intent = ModelManagerIntent.OnUse(size = ModelSize.SMALL))
        viewModel.onIntent(intent = ModelManagerIntent.OnOtherSizesToggle)
        advanceUntilIdle()

        assertEquals(expected = emptyList(), actual = storage.deleted)
        assertEquals(expected = ModelSize.SMALL, actual = activeModelStore.kept.value)
        assertEquals(expected = true, actual = viewModel.state.value.isOtherSizesShown)
    }

    @Test
    fun `the reranker stays while another chat model is installed`() = runTest(context = testDispatcher) {
        storage.installed +=
            listOf(ModelCatalogs.small.file.fileName, ModelCatalogs.medium.file.fileName, "reranker.gguf")
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(intent = ModelManagerIntent.OnDeleteAsk(size = ModelSize.MEDIUM))
        viewModel.onIntent(intent = ModelManagerIntent.OnDeleteConfirm)
        advanceUntilIdle()

        assertEquals(expected = listOf("medium.gguf"), actual = storage.deleted)
    }
}
