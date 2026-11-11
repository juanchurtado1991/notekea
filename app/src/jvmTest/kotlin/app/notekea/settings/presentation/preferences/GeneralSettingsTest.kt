package app.notekea.settings.presentation.preferences

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** Settings → General on screen: each group says what its own rows do (smoke of 5.2, 2026-11-11). */
@OptIn(ExperimentalTestApi::class)
class GeneralSettingsTest {
    @Test
    fun `Welcome explains itself instead of repeating the last vault's hint`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                GeneralSettings(
                    preferences = AppPreferences(),
                    onIntent = {},
                    spotlight = null
                )
            }
        }

        assertEquals(
            expected = listOf(1, 1),
            actual = listOf(
                onAllNodesWithText(text = LAST_VAULT_HINT).fetchSemanticsNodes().size,
                onAllNodesWithText(
                    text = WELCOME_HINT,
                    substring = true
                ).fetchSemanticsNodes().size
            )
        )
    }

    private companion object {
        const val LAST_VAULT_HINT =
            "Off, Notekea starts on the welcome screen with your vaults listed."
        const val WELCOME_HINT = "Shows the first-run welcome again"
    }
}
