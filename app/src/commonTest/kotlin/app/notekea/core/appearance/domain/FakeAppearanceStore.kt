package app.notekea.core.appearance.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** [AppearanceStore] in memory: [observe] gives the [kept] appearance and then each one saved, as the real store does. */
class FakeAppearanceStore(
    initial: Appearance = Appearance()
) : AppearanceStore {
    private val current = MutableStateFlow(value = initial)

    /** Thrown by every save, as a bug in the store would. */
    var failure: Throwable? = null

    val kept: Appearance get() = current.value

    override fun observe(): Flow<Appearance> = current

    override suspend fun save(
        appearance: Appearance
    ) {
        failure?.let { throw it }
        current.value = appearance
    }
}
