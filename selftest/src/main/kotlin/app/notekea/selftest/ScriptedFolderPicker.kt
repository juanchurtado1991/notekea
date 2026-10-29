package app.notekea.selftest

import app.notekea.vault.domain.FolderPicker

/** The folder picker in a case: it answers [nextPick] (null as a cancel) instead of showing the system's dialog. */
class ScriptedFolderPicker : FolderPicker {
    var nextPick: String? = null

    override suspend fun pickFolder(initialPath: String?): String? = nextPick
}
