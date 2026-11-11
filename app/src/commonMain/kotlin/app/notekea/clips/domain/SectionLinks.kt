package app.notekea.clips.domain

/**
 * The pages a page has under its own path ([prefix]), as a documentation site's index does, from its llms.txt, its
 * sitemap or its links; at most [MAX_PAGES], [more] says how many were left out. A news page has none.
 */
data class SectionLinks(
    val prefix: String,
    val urls: List<String>,
    val more: Int,
    // What each page is, when the site says it (llms.txt: "Axiom: send traces to Axiom"), by its address.
    val about: Map<String, String> = emptyMap()
) {
    /** What the page at [url] is: what the site says of it, or the words of its address. */
    fun aboutOf(url: String): String = about[url] ?: wordsOf(url = url)

    /** At most [MAX_PAGES], the first of [chosen] (the most valuable first), kept in the site's own order. */
    fun capped(chosen: List<String> = urls): SectionLinks {
        val kept = chosen.take(n = MAX_PAGES).toSet()

        return copy(
            urls = urls.filter {
                it in kept
            },
            more = urls.size - kept.size
        )
    }

    companion object {
        const val MAX_PAGES = 30

        // Quoted or not: Node.js's minified docs write href=assert.html.
        private val HREF = Regex(
            pattern = "href\\s*=\\s*(?:\"([^\"#]*)\"|'([^'#]*)'|([^\\s>#\"']+))",
            option = RegexOption.IGNORE_CASE
        )

        private val ORIGIN = Regex(
            pattern = "^(https?://[^/?#]+)(/[^?#]*)?",
            option = RegexOption.IGNORE_CASE
        )

        private val FILES = Regex(
            pattern = "\\.(png|jpe?g|gif|svg|webp|ico|css|js|json|xml|pdf|zip|gz|mp4|mp3|woff2?|ttf)$",
            option = RegexOption.IGNORE_CASE
        )

        // A copy of a page for machines: the same page, by another name.
        private val COPY = Regex(
            pattern = "\\.(md|txt|html?)$",
            option = RegexOption.IGNORE_CASE
        )

        // "/2026/11/11/eeuu/": a news site's day, whose pages are other stories, not a section (CNN, 2026-11-11).
        private val DATED = Regex(pattern = "/\\d{4}/\\d{1,2}/")

        // A news site's or a blog's folder, whose pages are other stories (WCK's /es/news/, 2026-11-11).
        private val NEWS = Regex(
            pattern = "/(news|noticias|noticia|blog|blogs|posts?|articles?|articulos?|stories|press|prensa)/",
            option = RegexOption.IGNORE_CASE
        )

        /** The links [page] writes under its path, at most [MAX_PAGES]. */
        fun of(page: FetchedPage): SectionLinks = within(
            url = page.url,
            candidates = linksIn(page = page)
        ).capped()

        /** The addresses [page] links, whole. */
        fun linksIn(page: FetchedPage): List<String> {
            val (origin, path) = partsOf(url = page.url) ?: return emptyList()

            return HREF
                .findAll(input = page.html)
                .map { match ->
                    match
                        .groupValues
                        .drop(n = 1)
                        .first { it.isNotEmpty() }
                        .trim()
                }
                .mapNotNull {
                    resolved(
                        href = it,
                        origin = origin,
                        path = path
                    )
                }
                .toList()
        }

        /**
         * Of [candidates] (a site's index, its sitemap, or a page's links), all those under the page at
         * [url]; beside it in its folder, only those the page itself [linked] (a flat docs site lists every page of it
         * in its index).
         */
        fun within(
            url: String,
            candidates: List<String>,
            linked: List<String>? = null
        ): SectionLinks {
            val (origin, path) = partsOf(url = url) ?: return none(prefix = url)
            val self = normalized(path = path)

            val pages = candidates
                .map { it.substringBefore(delimiter = '?').substringBefore(delimiter = '#') }
                .filter {
                    it.startsWith(prefix = origin) && !FILES.containsMatchIn(input = it)
                }
                .distinctBy {
                    keyOf(
                        url = it,
                        origin = origin
                    )
                }
                .filter {
                    keyOf(
                        url = it,
                        origin = origin
                    ) != self
                }

            // The page as a folder first ("/develop" holds "/develop/ui"), else the folder it is in
            // ("/integrations/introduction" beside "/integrations/axiom").
            val keys = pages.map {
                keyOf(
                    url = it,
                    origin = origin
                )
            }

            val own = "$self/"
            val parent = self.substringBeforeLast(delimiter = '/') + "/"
            val folder = if (keys.any { it.startsWith(prefix = own) }) own else parent

            val isNews =
                DATED.containsMatchIn(input = folder) || NEWS.containsMatchIn(input = folder)

            if (isNews) return none(prefix = origin + folder)

            val isBeside = folder != own

            val linkedKeys = linked?.map {
                keyOf(
                    url = it,
                    origin = origin
                )
            }?.toSet()

            val under = pages.filterIndexed { index, _ ->
                val key = keys[index]

                key.startsWith(prefix = folder) && (
                    !isBeside ||
                        linkedKeys == null ||
                        key in linkedKeys
                    )
            }

            return SectionLinks(
                prefix = origin + folder,
                urls = under,
                more = 0
            )
        }

        /** Where a site may keep a file [name] about the page at [url]: its folder and each above it, deepest first. */
        fun placesAbove(
            url: String,
            name: String
        ): List<String> {
            val (origin, path) = partsOf(url = url) ?: return emptyList()

            val folders = path
                .trim('/')
                .split('/')
                .dropLast(n = 1)
                .filter { it.isNotEmpty() }

            return (folders.size downTo 0).map { depth ->
                val folder = folders.take(n = depth).joinToString(separator = "/")
                if (folder.isEmpty()) "$origin/$name" else "$origin/$folder/$name"
            }
        }

        /** "https://kotlinlang.org/docs/_llms/android-overview.txt" as "android overview". */
        fun wordsOf(url: String): String = url
            .substringAfter(delimiter = "://")
            .substringAfter(delimiter = '/')
            .replace(COPY, replacement = "")
            .split('/', '-', '_')
            .filter {
                it.isNotEmpty() &&
                    it != "docs" &&
                    it != "llms"
            }
            .takeLast(n = WORDS_PARTS)
            .joinToString(separator = " ")

        private const val WORDS_PARTS = 3

        private fun partsOf(url: String): Pair<String, String>? = ORIGIN
            .find(input = url)
            ?.let {
                it.groupValues[1] to it.groupValues[2].ifEmpty { "/" }
            }

        private fun keyOf(
            url: String,
            origin: String
        ): String = normalized(path = url.removePrefix(prefix = origin))

        // A page's path as one name, however it is written: "/docs/a/", "/docs/a.md", "/docs/_llms/a.txt".
        private fun normalized(path: String): String = path
            .replace(
                oldValue = "/_llms/",
                newValue = "/"
            )
            .replace(COPY, replacement = "")
            .removeSuffix(suffix = "/index")
            .trimEnd('/')

        private fun none(prefix: String) = SectionLinks(
            prefix = prefix,
            urls = emptyList(),
            more = 0
        )

        // An address as the browser reads it from the page at [origin][path]: whole, from the root, or beside it.
        private fun resolved(
            href: String,
            origin: String,
            path: String
        ): String? = when {
            href.isEmpty() -> null
            href.startsWith(prefix = "http://") || href.startsWith(prefix = "https://") -> href
            href.startsWith(prefix = "//") -> origin.substringBefore(delimiter = "//") + href
            href.startsWith(prefix = "/") -> origin + href
            href.contains(char = ':') -> null
            else -> "$origin${path.substringBeforeLast(delimiter = '/')}/${href.removePrefix(prefix = "./")}"
        }
    }
}
