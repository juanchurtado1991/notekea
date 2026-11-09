package app.notekea.core.presentation.platform

import app.notekea.core.appearance.data.SettingsWindowBoundsStore
import app.notekea.core.appearance.domain.WindowBounds
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.domain.FakeAppDispatchers
import com.russhwolf.settings.MapSettings
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

/** Where the window is left is kept for the next run, and maximizing never loses its own size (audit 48). */
@OptIn(ExperimentalCoroutinesApi::class)
class WindowBoundsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val store = SettingsWindowBoundsStore(
        settings = MapSettings(),
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a window never kept has no bounds, and one moved is kept for the next run`() = runTest(
        context = testDispatcher
    ) {
        val firstRun = store.bounds()
        val viewModel = WindowBoundsViewModel(windowBoundsStore = store)

        viewModel.onIntent(intent = WindowBoundsIntent.OnFloating(bounds = BOUNDS))
        advanceUntilIdle()

        assertEquals(
            expected = listOf(null, BOUNDS),
            actual = listOf(firstRun, store.bounds())
        )
    }

    @Test
    fun `a maximized window keeps the size it goes back to`() = runTest(context = testDispatcher) {
        store.save(bounds = BOUNDS)
        val viewModel = WindowBoundsViewModel(windowBoundsStore = store)
        advanceUntilIdle()

        viewModel.onIntent(intent = WindowBoundsIntent.OnMaximized)
        advanceUntilIdle()

        assertEquals(
            expected = BOUNDS.copy(isMaximized = true),
            actual = store.bounds()
        )
    }

    @Test
    fun `a window that cannot be kept stays as it is, without an error`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = WindowBoundsViewModel(windowBoundsStore = FailingStore())
        advanceUntilIdle()

        viewModel.onIntent(intent = WindowBoundsIntent.OnFloating(bounds = BOUNDS))
        advanceUntilIdle()

        assertEquals(
            expected = BOUNDS,
            actual = viewModel.state.value.bounds
        )
    }

    private class FailingStore : WindowBoundsStore {
        override suspend fun bounds(): WindowBounds? = error(MESSAGE)

        override suspend fun save(bounds: WindowBounds) = error(MESSAGE)
    }

    private companion object {
        const val MESSAGE = "The settings file is read-only"
        val BOUNDS = WindowBounds(
            widthDp = 900,
            heightDp = 700,
            xDp = null,
            yDp = null,
            isMaximized = false
        )
    }
}
