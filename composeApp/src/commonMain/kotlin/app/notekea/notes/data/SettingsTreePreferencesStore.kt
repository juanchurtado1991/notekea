package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.TreePreferencesStore
import app.notekea.notes.domain.TreeSort
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [TreePreferencesStore] on Multiplatform Settings. The sort order is one key for all vaults; the expanded folders of
 * each vault are one key named after a hash of the vault path (`tree.<hash>.expanded`), holding relative paths one per
 * line ([VaultKeyHash]). The sort and the sidebar width
 * and visibility are one key each for all vaults. An unknown sort falls back to sorting by name and a width out of
 * bounds is clamped.
 */
class SettingsTreePreferencesStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : TreePreferencesStore {
    override suspend fun read(
        vaultPath: String
    ): TreePreferences = withContext(context = dispatchers.io) {
        val expanded = settings
            .getStringOrNull(key = expandedKey(vaultPath = vaultPath))
            ?.split(LINE_SEPARATOR)
            ?.filter { it.isNotEmpty() }
            ?.map { RelativePath(value = it) }
            .orEmpty()
        val sort = TreeSort.entries.firstOrNull { it.name == settings.getStringOrNull(key = SORT_KEY) } ?: TreeSort.NAME
        TreePreferences(
            expandedFolders = expanded.toSet(),
            sort = sort,
            sidebarWidthDp = TreePreferences.clampSidebarWidth(
                widthDp = settings.getInt(
                    key = SIDEBAR_WIDTH_KEY,
                    defaultValue = TreePreferences.DEFAULT_SIDEBAR_WIDTH_DP
                )
            ),
            isSidebarCollapsed = settings.getBoolean(key = SIDEBAR_COLLAPSED_KEY, defaultValue = false)
        )
    }

    override suspend fun write(
        vaultPath: String,
        preferences: TreePreferences
    ) = withContext(context = dispatchers.io) {
        val key = expandedKey(vaultPath = vaultPath)
        if (preferences.expandedFolders.isEmpty()) {
            settings.remove(key = key)
        } else {
            settings.putString(
                key = key,
                value = preferences.expandedFolders
                    .map {
                        it.value
                    }.sorted()
                    .joinToString(separator = LINE_SEPARATOR)
            )
        }
        settings.putString(key = SORT_KEY, value = preferences.sort.name)
        settings.putInt(key = SIDEBAR_WIDTH_KEY, value = preferences.sidebarWidthDp)
        settings.putBoolean(key = SIDEBAR_COLLAPSED_KEY, value = preferences.isSidebarCollapsed)
    }

    private fun expandedKey(
        vaultPath: String
    ): String = KEY_PREFIX + VaultKeyHash.of(vaultPath = vaultPath) + EXPANDED_SUFFIX

    companion object {
        private const val EXPANDED_SUFFIX = ".expanded"
        private const val KEY_PREFIX = "tree."
        private const val LINE_SEPARATOR = "\n"
        private const val SIDEBAR_COLLAPSED_KEY = "tree.sidebar.collapsed"
        private const val SIDEBAR_WIDTH_KEY = "tree.sidebar.width"
        private const val SORT_KEY = "tree.sort"
    }
}
