package app.notekea.core.web.domain

/**
 * The site a web address is on, without `www.` and in lower case: "fao.org". The one reading of it: cut only at `/`
 * and with its capitals kept, `https://fao.org?lang=en` and `https://FAO.org/` were sites of their own (the code
 * review of 2026-11-22).
 */
object WebHost {
    private val ENDS = setOf('/', '?', '#', '\\')

    /**
     * The host ends at the first `/`, `?`, `#` or `\`, and a `user@` before it is dropped: else
     * `https://evil.com?x=.fao.org` or `https://fao.org@evil.com` passes as fao.org.
     */
    fun of(address: String): String = address
        .substringAfter(delimiter = "://")
        .takeWhile {
            it !in ENDS
        }
        .substringAfterLast(delimiter = '@')
        .substringBefore(delimiter = ':')
        .lowercase()
        .removePrefix(prefix = "www.")
}
