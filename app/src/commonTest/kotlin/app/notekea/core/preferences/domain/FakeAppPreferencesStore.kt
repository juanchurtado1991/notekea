package app.notekea.core.preferences.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** [AppPreferencesStore] in memory, starting at [initial]; what was saved last is [saved]. */
class FakeAppPreferencesStore(initial: AppPreferences = AppPreferences()) : AppPreferencesStore {
    private val current = MutableStateFlow(value = initial)

    val saved: AppPreferences get() = current.value

    override fun observe(): Flow<AppPreferences> = current

    override suspend fun save(preferences: AppPreferences) {
        current.value = preferences
    }
}
