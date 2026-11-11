package app.notekea.sync.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.data.VaultKeyHash
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncSettingsStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [SyncSettingsStore] on Multiplatform Settings: three keys per vault; the token is never here (`TokenStore`). */
class SettingsSyncSettingsStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : SyncSettingsStore {
    override suspend fun read(vaultPath: String): SyncSettings? =
        withContext(context = dispatchers.io) {
            val key = keyOf(vaultPath = vaultPath)
            val url = settings.getStringOrNull(key = key + URL) ?: return@withContext null

            SyncSettings(
                remoteUrl = url,
                isAuto = settings.getBoolean(
                    key = key + AUTO,
                    defaultValue = true
                ),
                intervalMinutes = settings.getInt(
                    key = key + INTERVAL,
                    defaultValue = SyncSettings.DEFAULT_INTERVAL_MINUTES
                )
            )
        }

    override suspend fun write(
        vaultPath: String,
        settings: SyncSettings?
    ) = withContext(context = dispatchers.io) {
        val key = keyOf(vaultPath = vaultPath)

        if (settings == null) {
            listOf(URL, AUTO, INTERVAL).forEach {
                this@SettingsSyncSettingsStore.settings.remove(key = key + it)
            }

            return@withContext
        }

        this@SettingsSyncSettingsStore.settings.putString(
            key = key + URL,
            value = settings.remoteUrl
        )

        this@SettingsSyncSettingsStore.settings.putBoolean(
            key = key + AUTO,
            value = settings.isAuto
        )

        this@SettingsSyncSettingsStore.settings.putInt(
            key = key + INTERVAL,
            value = settings.intervalMinutes
        )
    }

    private fun keyOf(vaultPath: String): String =
        KEY_PREFIX + VaultKeyHash.of(vaultPath = vaultPath)

    private companion object {
        const val KEY_PREFIX = "sync."
        const val URL = ".url"
        const val AUTO = ".auto"
        const val INTERVAL = ".interval"
    }
}
