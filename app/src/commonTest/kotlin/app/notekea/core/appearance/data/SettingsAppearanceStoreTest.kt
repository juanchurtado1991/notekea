package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.domain.FakeAppDispatchers
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
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
    fun `the app starts in Workspace Blue following the system, and a choice is kept for the next run`() =
        runTest(context = testDispatcher) {
            val firstRun = store().observe().first()

            assertEquals(
                expected = ColorTheme.WORKSPACE_BLUE,
                actual = firstRun.theme
            )

            store().save(
                appearance = Appearance(
                    theme = ColorTheme.FOREST,
                    mode = ThemeMode.DARK
                )
            )

            assertEquals(
                expected = listOf(
                    Appearance(),
                    Appearance(
                        theme = ColorTheme.FOREST,
                        mode = ThemeMode.DARK
                    )
                ),
                actual = listOf(firstRun, store().observe().first())
            )
        }

    @Test
    fun `reduce motion starts off and a choice is kept for the next run`() = runTest(
        context = testDispatcher
    ) {
        val firstRun = store().observe().first()
        store().save(appearance = Appearance(reduceMotion = true))

        assertEquals(
            expected = listOf(false, true),
            actual = listOf(
                firstRun.reduceMotion,
                store()
                    .observe()
                    .first()
                    .reduceMotion
            )
        )
    }

    @Test
    fun `whoever kept a theme that is gone gets the closest one`() = runTest(
        context = testDispatcher
    ) {
        settings.putString(
            key = "app.theme",
            value = "lagoon"
        )

        assertEquals(
            expected = ColorTheme.OCEAN,
            actual = store()
                .observe()
                .first()
                .theme
        )
    }

    @Test
    fun `whoever kept the first palette gets the default`() = runTest(context = testDispatcher) {
        settings.putString(
            key = "app.theme",
            value = "default"
        )

        assertEquals(
            expected = ColorTheme.WORKSPACE_BLUE,
            actual = store()
                .observe()
                .first()
                .theme
        )
    }

    @Test
    fun `a window following the appearance sees each choice at once`() = runTest(
        context = testDispatcher
    ) {
        val store = store()
        val seen = mutableListOf<Appearance>()

        val following = launch {
            store
                .observe()
                .take(count = 2)
                .toList(destination = seen)
        }

        advanceUntilIdle()

        store.save(appearance = Appearance(theme = ColorTheme.DESERT))
        advanceUntilIdle()
        following.join()

        assertEquals(
            expected = listOf(Appearance(), Appearance(theme = ColorTheme.DESERT)),
            actual = seen
        )
    }

    // The code review of 2026-11-22: a second save ran inside the first, which then put its own
    // appearance over it in the windows while the file kept the second.
    @Test
    fun `two saves at once end with the file and the windows on the one asked last`() = runTest(
        context = testDispatcher
    ) {
        val store = store()
        val elsewhere = HeldDispatcher()

        // The first save comes from a dispatcher of its own, held between its steps.
        launch(context = elsewhere) {
            store.save(appearance = Appearance(theme = ColorTheme.FOREST))
        }

        elsewhere.runHeld()

        launch { store.save(appearance = Appearance(theme = ColorTheme.DESERT)) }

        advanceUntilIdle()
        elsewhere.runHeld()
        advanceUntilIdle()

        assertEquals(
            expected = listOf(ColorTheme.DESERT, ColorTheme.DESERT),
            actual = listOf(store, store()).map {
                it
                    .observe()
                    .first()
                    .theme
            }
        )
    }

    // Runs what it is given only when told, so a test decides when a coroutine on it goes on.
    private class HeldDispatcher : CoroutineDispatcher() {
        private val held = mutableListOf<Runnable>()

        override fun dispatch(
            context: CoroutineContext,
            block: Runnable
        ) {
            held += block
        }

        fun runHeld() {
            while (held.isNotEmpty()) held.removeAt(index = 0).run()
        }
    }

    @Test
    fun `a theme or mode this version does not know falls back to the default`() = runTest(
        context = testDispatcher
    ) {
        settings.putString(
            key = "app.theme",
            value = "a_later_theme"
        )

        settings.putString(
            key = "app.theme_mode",
            value = "sepia"
        )

        assertEquals(
            expected = Appearance(),
            actual = store().observe().first()
        )
    }
}
