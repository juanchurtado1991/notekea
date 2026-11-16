package app.notekea.core.web.domain

/** One result of a web search: the page's [title], its [url], and the provider's [snippet] of it. */
data class WebResult(
    val title: String,
    val url: String,
    val snippet: String
) {
    /** The site the page is on, without `www.`: "fao.org". */
    val domain: String
        get() = url
            .substringAfter(delimiter = "://")
            .substringBefore(delimiter = '/')
            .substringBefore(delimiter = ':')
            .removePrefix(prefix = "www.")
            .lowercase()
}
