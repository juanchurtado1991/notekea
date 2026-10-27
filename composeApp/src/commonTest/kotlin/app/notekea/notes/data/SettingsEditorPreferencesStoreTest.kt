package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.SidePanelPreferences
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsEditorPreferencesStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val store = SettingsEditorPreferencesStore(
        settings = settings,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `notes open in edit mode until read mode is chosen, which is remembered`() = runTest(context = testDispatcher) {
        val firstRun = store.isReadMode()

        store.setReadMode(isReading = true)
        val nextStore = SettingsEditorPreferencesStore(
            settings = settings,
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        )

        assertEquals(
            expected = false to true,
            actual = firstRun to nextStore.isReadMode()
        )
    }

    @Test
    fun `the side panel is shown at its default width until it is changed, which is remembered`() =
        runTest(context = testDispatcher) {
            val firstRun = store.sidePanel()

            store.setSidePanel(preferences = SidePanelPreferences(widthDp = 420, isVisible = false))
            val nextStore = SettingsEditorPreferencesStore(
                settings = settings,
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
            )

            assertEquals(
                expected = SidePanelPreferences(widthDp = 340, isVisible = true) to
                    SidePanelPreferences(widthDp = 420, isVisible = false),
                actual = firstRun to nextStore.sidePanel()
            )
        }
}
