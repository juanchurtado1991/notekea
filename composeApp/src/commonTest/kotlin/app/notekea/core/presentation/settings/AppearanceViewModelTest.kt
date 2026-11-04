package app.notekea.core.presentation.settings

import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.appearance.domain.ThemeMode
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

/** Settings → Appearance (`ROADMAP.md` 2.12): it shows the kept appearance and keeps each choice. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `reduce motion is kept with the theme, and off again on a second click`() = runTest(context = testDispatcher) {
        val store = FakeAppearanceStore(initial = Appearance(theme = ColorTheme.FOREST_MINT))
        val viewModel = AppearanceViewModel(appearanceStore = store)
        advanceUntilIdle()

        viewModel.onIntent(intent = AppearanceIntent.OnReduceMotionToggle)
        advanceUntilIdle()
        val turnedOn = store.kept
        viewModel.onIntent(intent = AppearanceIntent.OnReduceMotionToggle)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                Appearance(theme = ColorTheme.FOREST_MINT, reduceMotion = true),
                Appearance(theme = ColorTheme.FOREST_MINT)
            ),
            actual = listOf(turnedOn, store.kept)
        )
    }

    @Test
    fun `it shows the kept appearance, and a theme and a mode chosen are kept together`() = runTest(
        context = testDispatcher
    ) {
        val store = FakeAppearanceStore(initial = Appearance(theme = ColorTheme.FOREST_MINT))
        val viewModel = AppearanceViewModel(appearanceStore = store)
        advanceUntilIdle()
        val shown = viewModel.state.value.appearance

        viewModel.onIntent(intent = AppearanceIntent.OnModeChoose(mode = ThemeMode.DARK))
        viewModel.onIntent(intent = AppearanceIntent.OnThemeChoose(theme = ColorTheme.COSMIC_VIOLET))
        advanceUntilIdle()

        val chosen = Appearance(theme = ColorTheme.COSMIC_VIOLET, mode = ThemeMode.DARK)
        assertEquals(
            expected = listOf(Appearance(theme = ColorTheme.FOREST_MINT), chosen, chosen),
            actual = listOf(shown, viewModel.state.value.appearance, store.kept)
        )
    }
}
