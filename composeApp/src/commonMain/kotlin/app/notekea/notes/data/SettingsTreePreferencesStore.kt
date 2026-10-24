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
 * line. The hash is FNV-1a, computed here so it is the same on every platform and run. An unknown sort falls back to
 * sorting by name.
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
        TreePreferences(expandedFolders = expanded.toSet(), sort = sort)
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
    }

    private fun expandedKey(
        vaultPath: String
    ): String = KEY_PREFIX + fnv1a(text = vaultPath).toULong().toString(radix = HEX_RADIX) + EXPANDED_SUFFIX

    private fun fnv1a(
        text: String
    ): Long = text.encodeToByteArray().fold(initial = FNV_OFFSET_BASIS) { hash, byte ->
        (hash xor (byte.toLong() and BYTE_MASK)) * FNV_PRIME
    }

    companion object {
        private const val BYTE_MASK = 0xFFL
        private const val EXPANDED_SUFFIX = ".expanded"
        private const val FNV_OFFSET_BASIS = -0x340d631b7bdddcdbL
        private const val FNV_PRIME = 0x100000001b3L
        private const val HEX_RADIX = 16
        private const val KEY_PREFIX = "tree."
        private const val LINE_SEPARATOR = "\n"
        private const val SORT_KEY = "tree.sort"
    }
}
