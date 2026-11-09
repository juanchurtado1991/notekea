package app.notekea.core.domain

import kotlin.time.Clock
import kotlin.time.Instant

/** A clock that stays at [currentTime] until a test moves it. */
class FakeClock(var currentTime: Instant) : Clock {
    override fun now(): Instant = currentTime
}
