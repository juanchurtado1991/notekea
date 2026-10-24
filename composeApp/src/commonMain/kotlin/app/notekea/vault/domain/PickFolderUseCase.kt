package app.notekea.vault.domain

/** Shows the native folder picker, starting at [initialPath] when given; null means the user cancelled. */
class PickFolderUseCase(private val folderPicker: FolderPicker) {
    suspend operator fun invoke(
        initialPath: String?
    ): String? = folderPicker.pickFolder(initialPath = initialPath)
}
