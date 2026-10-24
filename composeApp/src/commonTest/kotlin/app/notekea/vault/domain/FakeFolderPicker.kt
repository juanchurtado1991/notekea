package app.notekea.vault.domain

/** Returns [nextPick] for every pick (null = the user cancelled) and records the initial paths it was given. */
class FakeFolderPicker : FolderPicker {
    val initialPaths = mutableListOf<String?>()
    var nextPick: String? = null
    var failure: Throwable? = null

    override suspend fun pickFolder(
        initialPath: String?
    ): String? {
        initialPaths += initialPath
        failure?.let { throw it }
        return nextPick
    }
}
