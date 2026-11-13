package app.notekea.clips.domain

import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.preferences.domain.WebUpdateLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * When the web sources of a vault are updated on their own (the developer's request of 2026-11-13): looked at when
 * the vault opens and every [pause] after, a turn comes when Settings says how often and that long has passed since
 * the last update that read its pages ([done] writes its day down; one that read none is tried again next time).
 */
class AutomaticWebUpdate(
    private val preferences: AppPreferencesStore,
    private val log: WebUpdateLog,
    private val clock: Clock,
    private val pause: Duration = 1.hours
) {
    /** Emits each time an update of the vault at [vaultPath] is due, for as long as it is collected. */
    fun turns(vaultPath: String): Flow<Unit> = flow {
        while (true) {
            val isDue = preferences
                .observe()
                .first()
                .webUpdate
                .isDue(
                    today = today(),
                    lastDay = log.lastDay(vaultPath = vaultPath)
                )

            if (isDue) emit(value = Unit)
            delay(duration = pause)
        }
    }

    /** The update of the vault at [vaultPath] read its pages today: its next turn counts from here. */
    suspend fun done(vaultPath: String) = log.mark(
        vaultPath = vaultPath,
        day = today()
    )

    private fun today(): Long = clock.now().epochSeconds / SECONDS_IN_DAY

    private companion object {
        const val SECONDS_IN_DAY = 86_400L
    }
}
