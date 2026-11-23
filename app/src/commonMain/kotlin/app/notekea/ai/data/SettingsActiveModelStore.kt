package app.notekea.ai.data

import app.notekea.ai.domain.ActiveModelStore
import app.notekea.ai.domain.ModelSize
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.preferences.data.HeldSetting
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** [ActiveModelStore] on Multiplatform Settings: `models.active`, by the size's id, read once and then held. */
class SettingsActiveModelStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : ActiveModelStore {
    private val held = HeldSetting(
        dispatchers = dispatchers,
        read = { Kept(size = ModelSize.fromId(id = settings.getStringOrNull(key = ACTIVE_KEY))) },
        write = ::write
    )

    override fun observe(): Flow<ModelSize?> = held
        .observe()
        .map { it.size }

    override suspend fun save(size: ModelSize?) = held.save(value = Kept(size = size))

    private fun write(kept: Kept) {
        if (kept.size == null) {
            settings.remove(key = ACTIVE_KEY)
        } else {
            settings.putString(
                key = ACTIVE_KEY,
                value = kept.size.id
            )
        }
    }

    /** A size read or saved: [size] null means none was kept. */
    private data class Kept(val size: ModelSize?)

    private companion object {
        const val ACTIVE_KEY = "models.active"
    }
}
