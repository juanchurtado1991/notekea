package app.notekea.shell.presentation.onboarding

import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The first-run welcome (5.1, 2026-11-11): shown once, its steps, and kept as seen when it ends. */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `it shows until seen, goes through its steps, waits for a new vault and is kept as seen at the end`() =
        runTest(dispatcher) {
            val store = FakeAppPreferencesStore()
            val viewModel = OnboardingViewModel(store = store)
            advanceUntilIdle()
            val shown = viewModel.state.value

            send(viewModel, OnboardingIntent.OnContinue)
            val notes = viewModel.state.value.step
            send(viewModel, OnboardingIntent.OnWait)
            val waiting = viewModel.state.value.isWaiting
            send(viewModel, OnboardingIntent.OnVaultOpened)
            val ai = viewModel.state.value
            send(viewModel, OnboardingIntent.OnFinish)

            assertEquals(
                expected = listOf<Any?>(
                    OnboardingState(isShown = true),
                    OnboardingStep.NOTES,
                    true,
                    OnboardingState(
                        isShown = true,
                        step = OnboardingStep.AI
                    ),
                    false,
                    true
                ),
                actual = listOf(
                    shown,
                    notes,
                    waiting,
                    ai,
                    viewModel.state.value.isShown,
                    store.saved.hasSeenWelcome
                )
            )
        }

    @Test
    fun `once seen it does not show`() = runTest(dispatcher) {
        val seen = AppPreferences(hasSeenWelcome = true)
        val viewModel = OnboardingViewModel(store = FakeAppPreferencesStore(initial = seen))

        advanceUntilIdle()

        assertEquals(
            expected = false,
            actual = viewModel.state.value.isShown
        )
    }

    private fun TestScope.send(
        viewModel: OnboardingViewModel,
        intent: OnboardingIntent
    ) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
    }
}
