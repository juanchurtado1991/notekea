package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.SidePanelMode
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelSection
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
    fun `the side panel is shown at its default width, open, until it is changed, which is remembered`() =
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
    fun `a mode or folded section of another version is left out`() = runTest(context = testDispatcher) {
        settings.putString(key = "editor.side_panel_folded", value = "BACKLINKS,NOTE_INFO")
        settings.putString(key = "editor.side_panel_mode", value = "GRAPH")

        assertEquals(
            expected = SidePanelPreferences(foldedSections = setOf(SidePanelSection.NOTE_INFO)),
            actual = store.sidePanel()
        )
    }

    private companion object {
        val CHANGED = SidePanelPreferences(
            widthDp = 420,
            isVisible = false,
            mode = SidePanelMode.CHAT,
            foldedSections = setOf(SidePanelSection.OUTLINE, SidePanelSection.NOTE_INFO)
        )
    }
}
