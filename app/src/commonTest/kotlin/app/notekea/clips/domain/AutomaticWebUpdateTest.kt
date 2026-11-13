package app.notekea.clips.domain

import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.preferences.domain.WebUpdateEvery
import app.notekea.core.preferences.domain.WebUpdateLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** When the web sources are updated on their own (the developer's request of 2026-11-13). */
@OptIn(ExperimentalCoroutinesApi::class)
class AutomaticWebUpdateTest {
    private var now = Instant.parse(input = "2026-11-13T09:00:00Z")

    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    private val log = object : WebUpdateLog {
        val days = mutableMapOf<String, Long>()

        override suspend fun lastDay(vaultPath: String): Long? = days[vaultPath]

        override suspend fun mark(
            vaultPath: String,
            day: Long
        ) {
            days[vaultPath] = day
        }
    }

    @Test
    fun `never gives no turn, and every week gives one at once and the next a week later, a vault at a time`() =
        runTest {
            val store = FakeAppPreferencesStore(initial = AppPreferences())
            var turns = 0

            val automatic = AutomaticWebUpdate(
                preferences = store,
                log = log,
                clock = clock,
                pause = 1.hours
            )

            val job = launch {
                automatic.turns(vaultPath = "/vault").collect {
                    turns++
                }
            }

            runCurrent()
            val never = turns

            store.save(preferences = AppPreferences(webUpdate = WebUpdateEvery.WEEKLY))
            advanceTimeBy(delayTime = 1.hours)
            runCurrent()
            val first = turns

            now += 6.days
            advanceTimeBy(delayTime = 1.hours)
            runCurrent()
            val beforeAWeek = turns

            now += 1.days
            advanceTimeBy(delayTime = 1.hours)
            runCurrent()
            job.cancel()

            assertEquals(
                expected = listOf(0, 1, 1, 2),
                actual = listOf(never, first, beforeAWeek, turns)
            )
        }

    @Test
    fun `an update is due when it never ran or its days have passed, and never when it is off`() {
        assertEquals(
            expected = listOf(true, false, true, false),
            actual = listOf(
                WebUpdateEvery.DAILY.isDue(
                    today = 100,
                    lastDay = null
                ),
                WebUpdateEvery.WEEKLY.isDue(
                    today = 106,
                    lastDay = 100
                ),
                WebUpdateEvery.WEEKLY.isDue(
                    today = 107,
                    lastDay = 100
                ),
                WebUpdateEvery.NEVER.isDue(
                    today = 500,
                    lastDay = null
                )
            )
        )
    }
}
