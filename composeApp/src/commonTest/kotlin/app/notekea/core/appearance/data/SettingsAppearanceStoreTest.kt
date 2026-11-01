package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.domain.FakeAppDispatchers
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsAppearanceStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()

    private fun store() = SettingsAppearanceStore(
        settings = settings,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `the app starts with the default theme following the system, and a choice is kept for the next run`() =
        runTest(context = testDispatcher) {
            val firstRun = store().observe().first()
            store().save(appearance = Appearance(theme = ColorTheme.DEEP_SAGE, mode = ThemeMode.DARK))

            assertEquals(
                expected = listOf(Appearance(), Appearance(theme = ColorTheme.DEEP_SAGE, mode = ThemeMode.DARK)),
                actual = listOf(firstRun, store().observe().first())
            )
        }

    @Test
    fun `a window following the appearance sees each choice at once`() = runTest(context = testDispatcher) {
        val store = store()
        val seen = mutableListOf<Appearance>()
        val following = launch { store.observe().take(count = 2).toList(destination = seen) }
        advanceUntilIdle()

        store.save(appearance = Appearance(theme = ColorTheme.CLAY_BUTTER))
        advanceUntilIdle()
        following.join()

        assertEquals(expected = listOf(Appearance(), Appearance(theme = ColorTheme.CLAY_BUTTER)), actual = seen)
    }

    @Test
    fun `a theme or mode this version does not know falls back to the default`() = runTest(context = testDispatcher) {
        settings.putString(key = "app.theme", value = "a_later_theme")
        settings.putString(key = "app.theme_mode", value = "sepia")

        assertEquals(expected = Appearance(), actual = store().observe().first())
    }
}
