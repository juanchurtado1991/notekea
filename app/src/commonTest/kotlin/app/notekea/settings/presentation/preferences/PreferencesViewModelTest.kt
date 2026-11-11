package app.notekea.settings.presentation.preferences

import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.preferences.domain.LineWidth
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

/** Settings → General and Editor (5.2): each change is saved at once. */
@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {
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
    fun `the language, the last vault, the welcome, the text size and the line width are saved as chosen`() =
        runTest(dispatcher) {
            val store = FakeAppPreferencesStore(initial = AppPreferences(hasSeenWelcome = true))
            val viewModel = PreferencesViewModel(store = store)
            advanceUntilIdle()

            listOf(
                PreferencesIntent.OnLanguageChoose(language = AppLanguage.SPANISH),
                PreferencesIntent.OnOpenLastVaultToggle,
                PreferencesIntent.OnShowWelcomeAgain,
                PreferencesIntent.OnTextSizeChoose(size = 18),
                PreferencesIntent.OnLineWidthChoose(width = LineWidth.READABLE)
            ).forEach {
                viewModel.onIntent(intent = it)
                advanceUntilIdle()
            }

            assertEquals(
                expected = AppPreferences(
                    language = AppLanguage.SPANISH,
                    opensLastVault = false,
                    textSize = 18,
                    lineWidth = LineWidth.READABLE,
                    hasSeenWelcome = false
                ),
                actual = store.saved
            )
        }
}
