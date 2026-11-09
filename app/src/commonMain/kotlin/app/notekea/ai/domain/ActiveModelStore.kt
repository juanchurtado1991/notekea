package app.notekea.ai.domain

import kotlinx.coroutines.flow.Flow

/** The chat model size the person uses, kept between runs. */
interface ActiveModelStore {
    /** The kept size (null when none), then each one [save]d. */
    fun observe(): Flow<ModelSize?>

    suspend fun save(
        size: ModelSize?
    )
}
