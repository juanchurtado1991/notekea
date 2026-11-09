package app.notekea.core.presentation.platform

import app.notekea.core.appearance.data.SettingsUiScaleStore
import app.notekea.core.appearance.domain.GetUiScaleUseCase
import app.notekea.core.appearance.domain.SaveUiScaleUseCase
import app.notekea.core.appearance.domain.UiScaleStore
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

/** The window's zoom lives in a view model: it comes back, is kept, and a failing store breaks nothing (audit 60). */
@OptIn(ExperimentalCoroutinesApi::class)
class UiZoomViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val store = SettingsUiScaleStore(
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

    private fun viewModel(uiScaleStore: UiScaleStore = store) = UiZoomViewModel(
        getUiScale = GetUiScaleUseCase(uiScaleStore = uiScaleStore),
        saveUiScale = SaveUiScaleUseCase(uiScaleStore = uiScaleStore)
    )

    @Test
    fun `the zoom starts where it was left, moves a step at a time and is kept`() = runTest(
        context = testDispatcher
    ) {
        store.setScale(scale = 1.3f)
        val viewModel = viewModel()
        advanceUntilIdle()
        val kept = viewModel.state.value

        viewModel.onIntent(intent = UiZoomIntent.OnLarger)
        advanceUntilIdle()
        val larger = store.scale()
        viewModel.onIntent(intent = UiZoomIntent.OnSmaller)
        viewModel.onIntent(intent = UiZoomIntent.OnSmaller)
        advanceUntilIdle()
        val smaller = store.scale()
        viewModel.onIntent(intent = UiZoomIntent.OnReset)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(1.3f, 1.4f, 1.2f, 1f),
            actual = listOf(kept, larger, smaller, store.scale())
        )
    }

    @Test
    fun `a zoom that cannot be read or kept still changes the window`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = viewModel(uiScaleStore = FailingStore())
        advanceUntilIdle()

        viewModel.onIntent(intent = UiZoomIntent.OnLarger)
        advanceUntilIdle()

        assertEquals(
            expected = 1.1f,
            actual = viewModel.state.value
        )
    }

    private class FailingStore : UiScaleStore {
        override suspend fun scale(): Float = error(MESSAGE)

        override suspend fun setScale(scale: Float) = error(MESSAGE)
    }

    private companion object {
        const val MESSAGE = "The settings file is read-only"
    }
}
