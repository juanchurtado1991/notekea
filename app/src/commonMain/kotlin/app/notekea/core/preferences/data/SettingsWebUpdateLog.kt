package app.notekea.core.preferences.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.preferences.domain.WebUpdateLog
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [WebUpdateLog] on Multiplatform Settings, a key for each vault's folder. */
class SettingsWebUpdateLog(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : WebUpdateLog {
    override suspend fun lastDay(vaultPath: String): Long? = withContext(context = dispatchers.io) {
        settings.getLongOrNull(key = keyOf(vaultPath = vaultPath))
    }

    override suspend fun mark(
        vaultPath: String,
        day: Long
    ) = withContext(context = dispatchers.io) {
        settings.putLong(
            key = keyOf(vaultPath = vaultPath),
            value = day
        )
    }

    private fun keyOf(vaultPath: String): String = KEY_PREFIX + vaultPath.hashCode()

    private companion object {
        const val KEY_PREFIX = "app.web_updated."
    }
}
