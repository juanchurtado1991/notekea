package app.notekea.vault.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.vault.domain.RecentVault
import app.notekea.vault.domain.RecentVaultsStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext
import kotlin.time.Instant

/**
 * [RecentVaultsStore] on Multiplatform Settings, as indexed keys (`vaults.recent.0.path`, `vaults.recent.0.openedAt`)
 * plus a count. An entry without a path is dropped; one without a valid time is kept as opened long ago.
 */
class SettingsRecentVaultsStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : RecentVaultsStore {
    override suspend fun read(): List<RecentVault> = withContext(context = dispatchers.io) {
        (0 until settings.getInt(key = COUNT_KEY, defaultValue = 0)).mapNotNull { readEntry(index = it) }
    }

    override suspend fun write(
        recentVaults: List<RecentVault>
    ) = withContext(context = dispatchers.io) {
        val previousCount = settings.getInt(key = COUNT_KEY, defaultValue = 0)
        (recentVaults.size until previousCount).forEach { removeEntry(index = it) }
        recentVaults.forEachIndexed { index, recentVault -> writeEntry(index = index, recentVault = recentVault) }
        settings.putInt(key = COUNT_KEY, value = recentVaults.size)
    }

    private fun openedAtKey(
        index: Int
    ): String = KEY_PREFIX + index + OPENED_AT_SUFFIX

    private fun pathKey(
        index: Int
    ): String = KEY_PREFIX + index + PATH_SUFFIX

    private fun readEntry(
        index: Int
    ): RecentVault? {
        val path = settings.getStringOrNull(key = pathKey(index = index)) ?: return null
        val openedAtMillis = settings.getLongOrNull(key = openedAtKey(index = index)) ?: 0L
        return RecentVault(
            path = path,
            lastOpenedAt = Instant.fromEpochMilliseconds(epochMilliseconds = openedAtMillis)
        )
    }

    private fun removeEntry(
        index: Int
    ) {
        settings.remove(key = pathKey(index = index))
        settings.remove(key = openedAtKey(index = index))
    }

    private fun writeEntry(
        index: Int,
        recentVault: RecentVault
    ) {
        settings.putString(key = pathKey(index = index), value = recentVault.path)
        settings.putLong(key = openedAtKey(index = index), value = recentVault.lastOpenedAt.toEpochMilliseconds())
    }

    companion object {
        private const val COUNT_KEY = "vaults.recent.count"
        private const val KEY_PREFIX = "vaults.recent."
        private const val OPENED_AT_SUFFIX = ".openedAt"
        private const val PATH_SUFFIX = ".path"
    }
}
