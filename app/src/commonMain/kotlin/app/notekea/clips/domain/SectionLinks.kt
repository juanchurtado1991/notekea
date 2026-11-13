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
    val about: Map<String, String> = emptyMap(),
    // The pages past the first [MAX_PAGES], clipped only when the person asks for all of them.
    val rest: List<String> = emptyList(),
    // Found in the page's own links, not in an index of the site: the pages those link may be followed too.
    val canGoDeeper: Boolean = false,
    // The pages are under the page itself ("/documentation/…" for "/documentation"), not only beside it.
    val isUnderPage: Boolean = false
) {
    /** Every page, the [rest] too: what "all of them" clips. */
    fun everyPage(): SectionLinks = copy(
        urls = urls + rest,
        more = 0,
        rest = emptyList()
    )

    /** What the page at [url] is: what the site says of it, or the words of its address. */
    fun aboutOf(url: String): String = about[url] ?: wordsOf(url = url)

    /** At most [MAX_PAGES], the first of [chosen] (the most valuable first), kept in the site's own order. */
    fun capped(chosen: List<String> = urls): SectionLinks {
        val kept = chosen.take(n = MAX_PAGES).toSet()

        // The rest in the same order of value, then any page the order left out.
        val rest = chosen.filterNot {
            it in kept
        } + urls.filterNot {
            it in kept || it in chosen
        }

        return copy(
            urls = urls.filter {
                it in kept
            },
            more = urls.size - kept.size,
            rest = rest
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

        // What a site's software serves beside its pages (WordPress's feeds and API, 2026-11-13): never a page to clip.
        private val MACHINE = Regex(
            pattern = "/(feed|wp-json|wp-includes|wp-content|wp-admin|xmlrpc\\.php|cdn-cgi)(/|$)|\\.php$",
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

        /** The deepest the links are followed, and the most pages a clip that goes deeper than one level takes. */
        const val MAX_DEPTH = 3
        const val MAX_DEEP_PAGES = 150

        /**
         * The pages [page] links under [prefix] that are not among [known] yet: what one more level of a section adds
         * (the developer's request of 2026-11-13, a documentation's pages link the pages under them).
         */
        fun newUnder(
            prefix: String,
            page: FetchedPage,
            known: Collection<String>
        ): List<String> {
            val (origin, _) = partsOf(url = prefix) ?: return emptyList()
            val folder = prefix.removePrefix(prefix = origin)

            val knownKeys = known
                .map {
                    keyOf(
                        url = it,
                        origin = origin
                    )
                }
                .toSet()

            return linksIn(page = page)
                .map { it.substringBefore(delimiter = '?').substringBefore(delimiter = '#') }
                .filter {
                    it.startsWith(prefix = origin) && isPage(url = it)
                }
                .distinctBy {
                    keyOf(
                        url = it,
                        origin = origin
                    )
                }
                .filter {
                    val key = keyOf(
                        url = it,
                        origin = origin
                    )

                    key.startsWith(prefix = folder) && key !in knownKeys
                }
        }

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
                    it.startsWith(prefix = origin) && isPage(url = it)
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
                more = 0,
                isUnderPage = !isBeside
            )
        }

        // An address of a page, not of a file or of something the site's software serves to programs.
        private fun isPage(url: String): Boolean =
            !FILES.containsMatchIn(input = url) && !MACHINE.containsMatchIn(input = url)

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
