package app.notekea.core.preferences.data

import app.notekea.core.domain.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * A value of the settings file, read once on the first [observe] and then held, so every window sees each [save] at
 * once. Saves go one at a time, in the order asked: two quick changes left the file on one and the windows on the
 * other (the code review of 2026-11-22).
 */
class HeldSetting<T : Any>(
    private val dispatchers: AppDispatchers,
    private val read: () -> T,
    private val write: (T) -> Unit
) {
    private val current = MutableStateFlow<T?>(value = null)
    private val saving = Mutex()

    fun observe(): Flow<T> = flow {
        if (current.value == null) {
            current.compareAndSet(
                expect = null,
                update = withContext(context = dispatchers.io) { read() }
            )
        }

        emitAll(current.filterNotNull())
    }

    suspend fun save(value: T) = saving.withLock {
        withContext(context = dispatchers.io) { write(value) }
        current.value = value
    }
}
