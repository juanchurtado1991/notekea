package app.notekea.core.appearance.domain

import kotlinx.coroutines.flow.Flow

/** Where the app's [Appearance] is kept between runs, the same for every vault and every window. */
interface AppearanceStore {
    /** The kept appearance, then each one [save]d, so every window follows a change at once. */
    fun observe(): Flow<Appearance>

    suspend fun save(
        appearance: Appearance
    )
}
