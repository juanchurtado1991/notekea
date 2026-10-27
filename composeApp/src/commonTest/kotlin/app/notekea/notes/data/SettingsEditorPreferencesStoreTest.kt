package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelTab
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
    fun `the side panel is shown at its default width on Outline until it is changed, which is remembered`() =
        runTest(context = testDispatcher) {
            val firstRun = store.sidePanel()

            store.setSidePanel(preferences = CHANGED)
            val nextStore = SettingsEditorPreferencesStore(
                settings = settings,
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
            )

            assertEquals(
                expected = SidePanelPreferences(widthDp = 340, isVisible = true) to CHANGED,
                actual = firstRun to nextStore.sidePanel()
            )
        }

    @Test
    fun `an unknown side panel tab, from another version, opens the outline`() = runTest(context = testDispatcher) {
        settings.putString(key = "editor.side_panel_tab", value = "GRAPH")

        assertEquals(
            expected = SidePanelTab.OUTLINE,
            actual = store.sidePanel().tab
        )
    }

    private companion object {
        val CHANGED = SidePanelPreferences(
            widthDp = 420,
            isVisible = false,
            tab = SidePanelTab.PROPERTIES,
            arePropertiesInNote = false
        )
    }
}
