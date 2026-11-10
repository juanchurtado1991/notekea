package app.notekea.ai.presentation

import app.notekea.ai.domain.FakeModelInstaller
import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The model's installation in the status bar: its progress as it goes, and a click that opens Settings. */
@OptIn(ExperimentalCoroutinesApi::class)
class ModelDownloadViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val installer = FakeModelInstaller()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the installation's progress follows the installer, and a click opens Settings`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = ModelDownloadViewModel(installer = installer)
        val effects = mutableListOf<ModelDownloadEffect>()

        backgroundScope.launch {
            viewModel.effects.collect { effects += it }
        }

        advanceUntilIdle()
        val idle = viewModel.state.value.fraction

        installer.current.value = InstallSession(
            size = ModelSize.MEDIUM,
            progress = InstallProgress.Downloading(
                doneBytes = 250,
                totalBytes = 1_000
            )
        )

        advanceUntilIdle()
        val downloading = viewModel.state.value.fraction

        installer.current.value = null
        viewModel.onIntent(intent = ModelDownloadIntent.OnOpen)
        advanceUntilIdle()
        // The effect's collector runs in the background, which advanceUntilIdle leaves alone.
        runCurrent()

        assertEquals(
            expected = listOf(null, 0.25f, null) to listOf<ModelDownloadEffect>(
                ModelDownloadEffect.OpenSettings
            ),
            actual = listOf(idle, downloading, viewModel.state.value.session) to effects.toList()
        )
    }
}
