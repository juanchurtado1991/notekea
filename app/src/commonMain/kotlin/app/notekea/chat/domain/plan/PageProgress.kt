package app.notekea.chat.domain.plan

/**
 * How far a clip of many pages is (the developer's request of 2026-11-13, a documentation takes minutes): [found]
 * pages so far while the links are followed, then page [done] + 1 of [total] being clipped, at [url].
 */
data class PageProgress(
    val done: Int = 0,
    val total: Int = 0,
    val url: String = "",
    val found: Int? = null
)
