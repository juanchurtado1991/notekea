package app.notekea.models.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.models.domain.ActiveModelStore
import app.notekea.models.domain.ModelSize
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
    private val current = MutableStateFlow<Kept?>(value = null)

    override fun observe(): Flow<ModelSize?> = flow {
        if (current.value == null) {
            val kept = withContext(context = dispatchers.io) { settings.getStringOrNull(key = ACTIVE_KEY) }
            current.compareAndSet(expect = null, update = Kept(size = ModelSize.fromId(id = kept)))
        }
        emitAll(current.filterNotNull().map { it.size })
    }

    override suspend fun save(
        size: ModelSize?
    ) {
        withContext(context = dispatchers.io) {
            if (size ==
                null
            ) {
                settings.remove(key = ACTIVE_KEY)
            } else {
                settings.putString(key = ACTIVE_KEY, value = size.id)
            }
        }
        current.value = Kept(size = size)
    }

    /** A size read or saved; null means not read yet, while [size] null means none was kept. */
    private data class Kept(
        val size: ModelSize?
    )

    private companion object {
        const val ACTIVE_KEY = "models.active"
    }
}
