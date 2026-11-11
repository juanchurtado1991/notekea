package app.notekea.core.preferences.domain

import kotlinx.coroutines.flow.Flow

/** Where [AppPreferences] are kept, for the app and not a vault: read once, then every change seen at once. */
interface AppPreferencesStore {
    fun observe(): Flow<AppPreferences>

    suspend fun save(preferences: AppPreferences)
}
