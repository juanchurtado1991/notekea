package app.notekea.notes.domain

/** Remembers how the tree of a vault looks now (expanded folders, sort order). */
class SaveTreePreferencesUseCase(private val treePreferencesStore: TreePreferencesStore) {
    suspend operator fun invoke(
        vaultPath: String,
        preferences: TreePreferences
    ) = treePreferencesStore.write(vaultPath = vaultPath, preferences = preferences)
}
