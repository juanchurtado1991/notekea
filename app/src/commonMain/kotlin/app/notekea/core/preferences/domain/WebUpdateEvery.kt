package app.notekea.core.preferences.domain

/**
 * How often the web sources are updated on their own (the developer's request of 2026-11-13): never, or once every
 * [days]. [id] is how the choice is kept.
 */
enum class WebUpdateEvery(
    val id: String,
    val days: Int?
) {
    NEVER(
        id = "never",
        days = null
    ),
    DAILY(
        id = "daily",
        days = 1
    ),
    WEEKLY(
        id = "weekly",
        days = 7
    ),
    MONTHLY(
        id = "monthly",
        days = 30
    );

    /** Whether an update is due on [today] (a day's number) when the last one was on [lastDay], null for never. */
    fun isDue(
        today: Long,
        lastDay: Long?
    ): Boolean {
        val every = days ?: return false

        return lastDay == null || today - lastDay >= every
    }

    companion object {
        fun fromId(id: String?): WebUpdateEvery = entries.firstOrNull {
            it.id == id
        } ?: NEVER
    }
}
