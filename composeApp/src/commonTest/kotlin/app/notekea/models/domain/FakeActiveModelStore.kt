package app.notekea.models.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** [ActiveModelStore] in memory, starting with [initial]. */
class FakeActiveModelStore(
    initial: ModelSize? = null
) : ActiveModelStore {
    val kept = MutableStateFlow(value = initial)

    override fun observe(): Flow<ModelSize?> = kept

    override suspend fun save(
        size: ModelSize?
    ) {
        kept.value = size
    }
}
