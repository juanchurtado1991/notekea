package app.notekea.settings.presentation.about

import app.notekea.core.logging.domain.FakeLogStore
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.logging.domain.TextClipboard
import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.settings.domain.GetDiagnosticsUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val clipboard = FakeTextClipboard()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Copy diagnostics says so when they cannot be copied`() = runTest(testDispatcher) {
        val viewModel = AboutViewModel(
            getDiagnostics = GetDiagnosticsUseCase(
                systemEnvironment = FakeSystemEnvironment(
                    operatingSystemName = "Linux",
                    userHome = "/home/ana"
                ),
                logStore = FakeLogStore(lines = emptyList())
            ),
            clipboard = object : TextClipboard {
                override fun copy(text: String): Unit = error("bug")
            }
        )

        viewModel.onIntent(intent = AboutIntent.OnCopyDiagnostics)
        advanceUntilIdle()

        assertEquals(
            expected = AboutState(hasCopyFailed = true),
            actual = viewModel.state.value
        )
    }

    @Test
    fun `Copy diagnostics puts them on the clipboard and says it did`() = runTest(testDispatcher) {
        val viewModel = AboutViewModel(
            getDiagnostics = GetDiagnosticsUseCase(
                systemEnvironment = FakeSystemEnvironment(
                    operatingSystemName = "Linux",
                    userHome = "/home/ana"
                ),
                logStore = FakeLogStore(
                    lines = listOf("I VaultTreeViewModel: Vault tree loaded: 30 notes")
                )
            ),
            clipboard = clipboard
        )

        viewModel.onIntent(intent = AboutIntent.OnCopyDiagnostics)
        advanceUntilIdle()

        assertEquals(
            expected = true,
            actual = viewModel.state.value.isDiagnosticsCopied
        )

        assertEquals(
            expected = "Notekea diagnostics",
            actual = clipboard
                .copied
                .single()
                .lines()
                .first()
        )

        assertEquals(
            expected = "I VaultTreeViewModel: Vault tree loaded: 30 notes",
            actual = clipboard
                .copied
                .single()
                .trimEnd()
                .lines()
                .last()
        )
    }
}
