package app.notekea.core.web.domain

/** One result of a web search: the page's [title], its [url], and the provider's [snippet] of it. */
data class WebResult(
    val title: String,
    val url: String,
    val snippet: String
) {
    /**
     * The site the page is on, without `www.`: "fao.org". The host ends at the first `/`, `?`, `#` or `\`, and a
     * `user@` before it is dropped: else `https://evil.com?x=.fao.org` or `https://fao.org@evil.com` passes as fao.org.
     */
    val domain: String
        get() = url
            .substringAfter(delimiter = "://")
            .takeWhile {
                it !in HOST_ENDS
            }
            .substringAfterLast(delimiter = '@')
            .substringBefore(delimiter = ':')
            .lowercase()
            .removePrefix(prefix = "www.")

    private companion object {
        val HOST_ENDS = setOf('/', '?', '#', '\\')
    }
}
