package app.notekea.settings.presentation.preferences

import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.preferences.domain.LineWidth
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Settings → General and Editor (5.2): each change is saved at once. */
@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest : ViewModelTestBase() {
    @Test
    fun `the language, the last vault, the welcome, the text size and the line width are saved as chosen`() =
        runTest(testDispatcher) {
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
