package app.notekea.editor.domain

import app.notekea.core.notes.domain.sidepanel.SidePanelPreferences
import kotlinx.coroutines.delay

/**
 * The editor's side panel and properties fold in memory; a side panel save takes as long as the next of
 * [sidePanelSaveMillis], as a disk may.
 */
class FakeEditorPreferencesStore : EditorPreferencesStore {
    val sidePanelSaveMillis = ArrayDeque<Long>()
    var sidePanel = SidePanelPreferences.DEFAULT
    var isPropertiesExpanded = true

    override suspend fun sidePanel(): SidePanelPreferences = sidePanel

    override suspend fun setSidePanel(preferences: SidePanelPreferences) {
        sidePanelSaveMillis.removeFirstOrNull()?.let { delay(timeMillis = it) }

        sidePanel = preferences
    }

    override suspend fun isPropertiesExpanded(): Boolean = isPropertiesExpanded

    override suspend fun setPropertiesExpanded(isExpanded: Boolean) {
        isPropertiesExpanded = isExpanded
    }
}
