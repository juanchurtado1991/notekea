package app.notekea.notes.domain

/** How the tree of a vault looked when it was last shown, so it reopens the same way. */
class GetTreePreferencesUseCase(private val treePreferencesStore: TreePreferencesStore) {
    suspend operator fun invoke(
        vaultPath: String
    ): TreePreferences = treePreferencesStore.read(vaultPath = vaultPath)
}
