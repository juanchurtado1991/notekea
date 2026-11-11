package app.notekea.clips.domain

/**
 * The pages a page links under its own path ([prefix]), as a documentation site's index does: same site, a deeper or a
 * sibling page of the same folder, no file, no anchor of itself. At most [MAX_PAGES]; [more] says how many were left.
 */
data class SectionLinks(
    val prefix: String,
    val urls: List<String>,
    val more: Int
) {
    companion object {
        const val MAX_PAGES = 30

        private val HREF = Regex(
            pattern = "href\\s*=\\s*[\"']([^\"'#]+)",
            option = RegexOption.IGNORE_CASE
        )
        private val ORIGIN = Regex(
            pattern = "^(https?://[^/?#]+)(/[^?#]*)?",
            option = RegexOption.IGNORE_CASE
        )

        private val FILES = Regex(
            pattern = "\\.(png|jpe?g|gif|svg|webp|ico|css|js|json|xml|pdf|zip|gz|mp4|mp3|woff2?|ttf|md|txt)$",
            option = RegexOption.IGNORE_CASE
        )

        /** The links of [page] under its folder: "/docs/a/intro" holds "/docs/a/axiom" and "/docs/a/b/c". */
        fun of(page: FetchedPage): SectionLinks {
            val (origin, path) = ORIGIN
                .find(input = page.url)
                ?.let {
                    it.groupValues[1] to it.groupValues[2].ifEmpty { "/" }
                }
                ?: return SectionLinks(
                    prefix = page.url,
                    urls = emptyList(),
                    more = 0
                )

            val self = path.trimEnd('/')
            val prefix = origin + path.substringBeforeLast(delimiter = '/') + "/"

            val found = HREF
                .findAll(input = page.html)
                .map { it.groupValues[1].trim() }
                .mapNotNull {
                    resolved(
                        href = it,
                        origin = origin,
                        path = path
                    )
                }
                .map { it.substringBefore(delimiter = '?').trimEnd('/') }
                .filter {
                    it.startsWith(prefix = prefix) &&
                        it != origin + self &&
                        !FILES.containsMatchIn(input = it)
                }
                .distinct()
                .toList()

            return SectionLinks(
                prefix = prefix,
                urls = found.take(n = MAX_PAGES),
                more = (found.size - MAX_PAGES).coerceAtLeast(minimumValue = 0)
            )
        }

        // An address as the browser reads it from the page at [origin][path]: whole, from the root, or beside it.
        private fun resolved(
            href: String,
            origin: String,
            path: String
        ): String? = when {
            href.startsWith(prefix = "http://") || href.startsWith(prefix = "https://") -> href
            href.startsWith(prefix = "//") -> origin.substringBefore(delimiter = "//") + href
            href.startsWith(prefix = "/") -> origin + href
            href.contains(char = ':') -> null
            else -> "$origin${path.substringBeforeLast(delimiter = '/')}/${href.removePrefix(prefix = "./")}"
        }
    }
}
