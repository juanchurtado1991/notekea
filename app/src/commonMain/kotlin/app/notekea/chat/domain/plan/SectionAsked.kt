package app.notekea.chat.domain.plan

/** The pages a page links under its own [prefix]: [count] of them, [more] left out over the limit. */
data class SectionAsked(
    val url: String,
    val prefix: String,
    val count: Int,
    val more: Int,
    val summary: String? = null,
    // The pages those pages link can be followed too: the question offers two and three levels.
    val canGoDeeper: Boolean = false,
    // The page is a list of links: its pages are likely lists too, and going deeper is what the question suggests.
    val isIndex: Boolean = false,
    // Counted [depth] levels deep before clipping: [count] is every page found, and the question is to confirm.
    val depth: Int = 1,
    val isCounted: Boolean = false
)
