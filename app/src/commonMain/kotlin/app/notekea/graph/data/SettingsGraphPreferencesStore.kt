package app.notekea.graph.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.data.VaultKeyHash
import app.notekea.graph.domain.GraphPreferences
import app.notekea.graph.domain.GraphPreferencesStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [GraphPreferencesStore] on Multiplatform Settings: one key per vault, the folders left out, one per line. */
class SettingsGraphPreferencesStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : GraphPreferencesStore {
    override suspend fun read(vaultPath: String): GraphPreferences = withContext(
        context = dispatchers.io
    ) {
        val text = settings.getStringOrNull(key = keyOf(vaultPath = vaultPath)).orEmpty()

        GraphPreferences(
            excludedFolders = text
                .split(LINE_SEPARATOR)
                .filter { it.isNotBlank() }
                .toSet()
        )
    }

    override suspend fun write(
        vaultPath: String,
        preferences: GraphPreferences
    ) = withContext(context = dispatchers.io) {
        val text = preferences.excludedFolders.joinToString(separator = LINE_SEPARATOR)

        settings.putString(
            key = keyOf(vaultPath = vaultPath),
            value = text
        )
    }

    private fun keyOf(vaultPath: String): String =
        KEY_PREFIX + VaultKeyHash.of(vaultPath = vaultPath)

    private companion object {
        const val KEY_PREFIX = "graph."
        const val LINE_SEPARATOR = "\n"
    }
}
