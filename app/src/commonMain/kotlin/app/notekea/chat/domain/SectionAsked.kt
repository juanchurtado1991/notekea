package app.notekea.chat.domain

/** The pages a page links under its own [prefix]: [count] of them, [more] left out over the limit. */
data class SectionAsked(
    val url: String,
    val prefix: String,
    val count: Int,
    val more: Int
)
