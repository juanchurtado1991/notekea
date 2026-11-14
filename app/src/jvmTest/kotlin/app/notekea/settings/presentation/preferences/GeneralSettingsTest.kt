package app.notekea.settings.presentation.preferences

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.core.presentation.platform.LocalWindowWebSources
import app.notekea.core.presentation.platform.WindowWebSources
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

    // The developer's smoke of 2026-11-14: the web sources' update was in General and was not found.
    @Test
    fun `with a vault open the web sources lead the page, say their last update and update now`() =
        runComposeUiTest {
            var updates = 0

            val sources = WindowWebSources().apply {
                updateNow = {
                    updates++
                }

                daysSinceUpdate = 3
            }

            setContent {
                NotekeaTheme(darkTheme = false) {
                    CompositionLocalProvider(LocalWindowWebSources provides sources) {
                        GeneralSettings(
                            preferences = AppPreferences(),
                            onIntent = {},
                            spotlight = null
                        )
                    }
                }
            }

            onNodeWithText(text = "Update now").performClick()
            waitForIdle()

            val titleTop = onNodeWithText(text = "Web sources")
                .fetchSemanticsNode()
                .boundsInRoot
                .top

            val languageTop = onNodeWithText(text = "Language")
                .fetchSemanticsNode()
                .boundsInRoot
                .top

            assertEquals(
                expected = listOf<Any>(1, 1, true),
                actual = listOf<Any>(
                    updates,
                    onAllNodesWithText(text = "3 days ago").fetchSemanticsNodes().size,
                    titleTop < languageTop
                )
            )
        }

    // Code review, 2026-11-14: Update now runs without its dialog, and offline it ended without a word.
    @Test
    fun `an update that could read no page says so in place of the last update`() =
        runComposeUiTest {
            val sources = WindowWebSources().apply {
                updateNow = {}
                daysSinceUpdate = 3
                hasFailed = true
            }

            setContent {
                NotekeaTheme(darkTheme = false) {
                    CompositionLocalProvider(LocalWindowWebSources provides sources) {
                        GeneralSettings(
                            preferences = AppPreferences(),
                            onIntent = {},
                            spotlight = null
                        )
                    }
                }
            }

            assertEquals(
                expected = listOf(1, 0),
                actual = listOf(
                    onAllNodesWithText(
                        text = "Could not read the pages",
                        substring = true
                    ).fetchSemanticsNodes().size,
                    onAllNodesWithText(text = "3 days ago").fetchSemanticsNodes().size
                )
            )
        }

    @Test
    fun `with no vault open there is nothing to update now`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                CompositionLocalProvider(LocalWindowWebSources provides WindowWebSources()) {
                    GeneralSettings(
                        preferences = AppPreferences(),
                        onIntent = {},
                        spotlight = null
                    )
                }
            }
        }

        assertEquals(
            expected = listOf(0, 1),
            actual = listOf(
                onAllNodesWithText(text = "Update now").fetchSemanticsNodes().size,
                onAllNodesWithText(text = "Update on their own").fetchSemanticsNodes().size
            )
        )
    }

    private companion object {
        const val LAST_VAULT_HINT =
            "Off, Notekea starts on the welcome screen with your vaults listed."
        const val WELCOME_HINT = "Shows the first-run welcome again"
    }
}
