package app.notekea.vault.domain

/** The operating system's native folder picker. */
interface FolderPicker {
    /** Returns the chosen folder's path, or null when the user cancels. */
    suspend fun pickFolder(
        initialPath: String?
    ): String?
}
