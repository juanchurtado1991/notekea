package app.notekea.core.presentation

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cancellation is not reported as an error`() = runTest(context = testDispatcher) {
        val viewModel = CounterViewModel()

        viewModel.onIntent(intent = CounterIntent.Cancel)
        advanceUntilIdle()

        assertEquals(
            expected = null,
            actual = viewModel.state.value.error
        )
    }

    @Test
    fun `effects are delivered once even when emitted before collection`() = runTest(context = testDispatcher) {
        val viewModel = CounterViewModel()

        viewModel.onIntent(intent = CounterIntent.Notify)

        viewModel.effects.test {
            assertEquals(
                expected = CounterEffect.Notified,
                actual = awaitItem()
            )
            expectNoEvents()
        }
    }

    @Test
    fun `initial state is exposed before any intent`() {
        val viewModel = CounterViewModel()

        assertEquals(
            expected = CounterState(count = INITIAL_COUNT),
            actual = viewModel.state.value
        )
    }

    @Test
    fun `reduce updates the state`() {
        val viewModel = CounterViewModel()

        viewModel.onIntent(intent = CounterIntent.Increment)
        viewModel.onIntent(intent = CounterIntent.Increment)

        assertEquals(
            expected = INITIAL_COUNT + 2,
            actual = viewModel.state.value.count
        )
    }

    @Test
    fun `unexpected exceptions in guarded work are routed to onError`() = runTest(context = testDispatcher) {
        val viewModel = CounterViewModel()

        viewModel.onIntent(intent = CounterIntent.Fail)
        advanceUntilIdle()

        assertEquals(
            expected = FAILURE_MESSAGE,
            actual = viewModel.state.value.error
        )
    }

    private data class CounterState(
        val count: Int = INITIAL_COUNT,
        val error: String? = null
    )

    private sealed interface CounterIntent {
        data object Cancel : CounterIntent

        data object Fail : CounterIntent

        data object Increment : CounterIntent

        data object Notify : CounterIntent
    }

    private sealed interface CounterEffect {
        data object Notified : CounterEffect
    }

    private class CounterViewModel : BaseViewModel<CounterState, CounterIntent, CounterEffect>(
        initialState = CounterState()
    ) {
        override fun handleIntent(
            intent: CounterIntent
        ) {
            when (intent) {
                CounterIntent.Cancel -> launchGuarded(onError = ::showError) {
                    throw CancellationException(FAILURE_MESSAGE)
                }

                CounterIntent.Fail -> launchGuarded(onError = ::showError) {
                    error(FAILURE_MESSAGE)
                }

                CounterIntent.Increment -> reduce { it.copy(count = it.count + 1) }

                CounterIntent.Notify -> emit(effect = CounterEffect.Notified)
            }
        }

        private fun showError(
            throwable: Throwable
        ) {
            reduce { it.copy(error = throwable.message) }
        }
    }

    companion object {
        private const val FAILURE_MESSAGE = "boom"
        private const val INITIAL_COUNT = 0
    }
}
