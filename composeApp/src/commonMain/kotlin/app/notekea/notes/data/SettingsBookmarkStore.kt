package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.BookmarkStore
import app.notekea.notes.domain.Bookmarks
import app.notekea.notes.domain.RelativePath
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [BookmarkStore] on Multiplatform Settings: one key per vault (`bookmarks.<hash>`, [VaultKeyHash]) holding the
 * bookmarked paths, one per line, in their order. No bookmarks removes the key.
 */
class SettingsBookmarkStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : BookmarkStore {
    override suspend fun read(
        vaultPath: String
    ): Bookmarks = withContext(context = dispatchers.io) {
        val text = settings.getStringOrNull(key = keyOf(vaultPath = vaultPath)).orEmpty()
        val paths = text.split(LINE_SEPARATOR).filter { it.isNotBlank() }.map { RelativePath(value = it) }
        Bookmarks(paths = paths.distinct())
    }

    override suspend fun write(
        vaultPath: String,
        bookmarks: Bookmarks
    ) = withContext(context = dispatchers.io) {
        val key = keyOf(vaultPath = vaultPath)
        if (bookmarks.paths.isEmpty()) {
            settings.remove(key = key)
        } else {
            val text = bookmarks.paths.joinToString(separator = LINE_SEPARATOR) { it.value }
            settings.putString(key = key, value = text)
        }
    }

    private fun keyOf(
        vaultPath: String
    ): String = KEY_PREFIX + VaultKeyHash.of(vaultPath = vaultPath)

    companion object {
        private const val KEY_PREFIX = "bookmarks."
        private const val LINE_SEPARATOR = "\n"
    }
}
