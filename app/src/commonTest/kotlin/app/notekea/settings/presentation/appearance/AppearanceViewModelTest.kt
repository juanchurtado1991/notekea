package app.notekea.settings.presentation.appearance

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

/** Settings → Appearance: it shows the kept appearance and keeps each choice. */
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
    fun `a choice that cannot be kept says so, until the next one is`() = runTest(
        context = testDispatcher
    ) {
        val store = FakeAppearanceStore().apply { failure = IllegalStateException("bug") }

        val viewModel = AppearanceViewModel(appearanceStore = store)
        advanceUntilIdle()

        viewModel.onIntent(intent = AppearanceIntent.OnThemeChoose(theme = ColorTheme.SAGE))
        advanceUntilIdle()
        val failed = viewModel.state.value.hasSaveFailed
        store.failure = null
        viewModel.onIntent(intent = AppearanceIntent.OnThemeChoose(theme = ColorTheme.SAGE))
        advanceUntilIdle()

        assertEquals(
            expected = true to false,
            actual = failed to viewModel.state.value.hasSaveFailed
        )
    }

    @Test
    fun `reduce motion is kept with the theme, and off again on a second click`() = runTest(
        context = testDispatcher
    ) {
        val store = FakeAppearanceStore(initial = Appearance(theme = ColorTheme.SAGE))
        val viewModel = AppearanceViewModel(appearanceStore = store)
        advanceUntilIdle()

        viewModel.onIntent(intent = AppearanceIntent.OnReduceMotionToggle)
        advanceUntilIdle()
        val turnedOn = store.kept
        viewModel.onIntent(intent = AppearanceIntent.OnReduceMotionToggle)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                Appearance(
                    theme = ColorTheme.SAGE,
                    reduceMotion = true
                ),
                Appearance(theme = ColorTheme.SAGE)
            ),
            actual = listOf(turnedOn, store.kept)
        )
    }

    @Test
    fun `use the system's accent is kept as it is turned on and off`() =
        runTest(context = testDispatcher) {
            val store = FakeAppearanceStore()
            val viewModel = AppearanceViewModel(appearanceStore = store)
            advanceUntilIdle()

            viewModel.onIntent(intent = AppearanceIntent.OnSystemAccentToggle)
            advanceUntilIdle()
            val turnedOn = store.kept
            viewModel.onIntent(intent = AppearanceIntent.OnSystemAccentToggle)
            advanceUntilIdle()

            assertEquals(
                expected = listOf(
                    Appearance(usesSystemAccent = true),
                    Appearance()
                ),
                actual = listOf(turnedOn, store.kept)
            )
        }

    @Test
    fun `it shows the kept appearance, and a theme and a mode chosen are kept together`() = runTest(
        context = testDispatcher
    ) {
        val store = FakeAppearanceStore(initial = Appearance(theme = ColorTheme.SAGE))
        val viewModel = AppearanceViewModel(appearanceStore = store)
        advanceUntilIdle()
        val shown = viewModel.state.value.appearance

        viewModel.onIntent(intent = AppearanceIntent.OnModeChoose(mode = ThemeMode.DARK))
        viewModel.onIntent(intent = AppearanceIntent.OnThemeChoose(theme = ColorTheme.IRIS))
        advanceUntilIdle()

        val chosen = Appearance(
            theme = ColorTheme.IRIS,
            mode = ThemeMode.DARK
        )

        assertEquals(
            expected = listOf(Appearance(theme = ColorTheme.SAGE), chosen, chosen),
            actual = listOf(shown, viewModel.state.value.appearance, store.kept)
        )
    }
}
