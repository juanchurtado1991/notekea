package app.notekea.query.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.data.VaultKeyHash
import app.notekea.query.domain.QueryParse
import app.notekea.query.domain.QueryPreferencesStore
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.VaultQuery
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [QueryPreferencesStore] on Multiplatform Settings: one key per vault, the query as its block's YAML. */
class SettingsQueryPreferencesStore(
    private val settings: Settings,
    private val queryYaml: QueryYaml,
    private val dispatchers: AppDispatchers
) : QueryPreferencesStore {
    override suspend fun read(
        vaultPath: String
    ): VaultQuery = withContext(context = dispatchers.io) {
        val text = settings.getStringOrNull(key = keyOf(vaultPath = vaultPath)).orEmpty()
        (queryYaml.read(text = text) as? QueryParse.Valid)?.query ?: VaultQuery()
    }

    override suspend fun write(
        vaultPath: String,
        query: VaultQuery
    ) = withContext(context = dispatchers.io) {
        settings.putString(key = keyOf(vaultPath = vaultPath), value = queryYaml.write(query = query))
    }

    private fun keyOf(
        vaultPath: String
    ): String = KEY_PREFIX + VaultKeyHash.of(vaultPath = vaultPath)

    private companion object {
        const val KEY_PREFIX = "query."
    }
}
