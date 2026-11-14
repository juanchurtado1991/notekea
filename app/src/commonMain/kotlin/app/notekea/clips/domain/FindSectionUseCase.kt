package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * The pages a page has under its own path, before clipping (the developer's choice of 2026-11-11): from the site's
 * llms.txt first, which lists its pages as Markdown for machines, then its sitemap.xml, then the page's own links.
 */
class FindSectionUseCase(
    private val fetcher: PageFetcher,
    private val ranker: SectionRanker? = null
) {
    /**
     * The pages under the page at [url]. With a [depth] over 1 and no index of the site to list them, the pages
     * those link are followed too, [depth] levels in all and at most [most] pages, [onFound] told how many so far.
     */
    suspend operator fun invoke(
        url: String,
        depth: Int = 1,
        most: Int = SectionLinks.MAX_DEEP_PAGES,
        onFound: suspend (pages: Int) -> Unit = {}
    ): Outcome<SectionLinks, ClipError> {
        val page = when (val fetched = fetcher.fetch(url = url.trim())) {
            is Outcome.Success -> fetched.value
            is Outcome.Failure -> return fetched
        }

        val linked = SectionLinks.linksIn(page = page)

        val listed = indexed(
            url = page.url,
            name = LLMS,
            links = MARKDOWN_LINK,
            linked = linked
        )
            ?: indexed(
                url = page.url,
                name = SITEMAP,
                links = SITEMAP_LOC,
                linked = linked
            )

        val ownLinks = SectionLinks.within(
            url = page.url,
            candidates = linked
        ).copy(canGoDeeper = true)

        // A page with nothing under it that links hundreds beside it is an article of an encyclopedia or a shop's
        // product: those are other subjects, not its section (Wikipedia offered 395 pages, 2026-11-13). A docs
        // site's flat folder (TypeScript's handbook) lists its pages the same way, deeper in the site and fewer.
        val most = SectionLinks.mostBeside(prefix = ownLinks.prefix)
        val isLoose = !ownLinks.isUnderPage && ownLinks.urls.size > most

        val shown = if (isLoose) ownLinks.copy(urls = emptyList()) else ownLinks

        val fromLinks = shown.copy(
            isIndex = isIndex(
                html = page.html,
                links = shown.urls.size
            )
        )

        // An index of the site counts when it lists pages under this page. WordPress's llms.txt lists the site's
        // general pages, none of its documentation: the page's own links are its section then (2026-11-13).
        val found = when {
            listed?.isUnderPage == true -> listed
            fromLinks.isUnderPage && fromLinks.urls.isNotEmpty() -> fromLinks
            else -> listed ?: fromLinks
        }

        val levels = depth.coerceIn(1, SectionLinks.FULL_DEPTH)

        val isDeepened =
            found.canGoDeeper &&
                levels > 1 &&
                found.urls.isNotEmpty()

        if (isDeepened) {
            return Outcome.Success(
                value = deepened(
                    start = page.url,
                    found = found,
                    levels = levels,
                    most = most,
                    onFound = onFound
                )
            )
        }

        if (found.urls.size <= SectionLinks.MAX_PAGES) return Outcome.Success(value = found)

        // More than can be clipped: the pages closest to the one pasted first (500 of Kotlin's, its Android ones).
        val ranked = ranker?.ranked(
            page = page,
            links = found
        ) ?: found.urls

        return Outcome.Success(value = found.capped(chosen = ranked))
    }

    // The section one level at a time: each page of a level read for the pages it links under the section, a few at
    // once, until [levels] are done or there are more than a deep clip takes.
    private suspend fun deepened(
        start: String,
        found: SectionLinks,
        levels: Int,
        most: Int,
        onFound: suspend (Int) -> Unit
    ): SectionLinks {
        val all = found.urls.toMutableList()
        var level = found.urls

        repeat(times = levels - 1) {
            val next = mutableListOf<String>()

            for (batch in level.chunked(size = AT_ONCE)) {
                if (all.size >= most) break

                val pages = coroutineScope {
                    batch
                        .map {
                            async { (fetcher.fetch(url = it) as? Outcome.Success)?.value }
                        }
                        .awaitAll()
                }

                pages.filterNotNull().forEach { read ->
                    val added = SectionLinks.newUnder(
                        prefix = found.prefix,
                        page = read,
                        known = all + start
                    )

                    all += added
                    next += added
                }

                onFound(all.size)
            }

            level = next
        }

        return found.copy(
            urls = all.take(n = most),
            more = (all.size - most).coerceAtLeast(minimumValue = 0),
            rest = all.drop(n = most)
        )
    }

    // A page of few words for each page it links is a list of them (WordPress's documentation: 23 links, a line each).
    private fun isIndex(
        html: String,
        links: Int
    ): Boolean {
        if (links < FEW_LINKS) return false

        val words = html
            .replace(NOT_TEXT, replacement = " ")
            .replace(TAG, replacement = " ")
            .split(SPACES)
            .count { it.isNotEmpty() }

        return words / links < WORDS_A_LINK
    }

    // The first index file [name] above the page that lists pages under its path; null when none does.
    private suspend fun indexed(
        url: String,
        name: String,
        links: Regex,
        linked: List<String>
    ): SectionLinks? {
        SectionLinks.placesAbove(
            url = url,
            name = name
        ).forEach { place ->
            val text =
                (fetcher.fetch(url = place) as? Outcome.Success)?.value?.html ?: return@forEach

            // A site answers a missing file with its web page: that is no index.
            val isPage = !MarkdownArticle.isMarkdown(text = text) && "<urlset" !in text
            if (isPage) return@forEach

            val about = if (links == MARKDOWN_LINK) aboutIn(text = text) else emptyMap()

            val found = SectionLinks.within(
                url = url,
                candidates = links
                    .findAll(input = text)
                    .map { it.groupValues[1].trim() }
                    .toList(),
                linked = linked
            )

            if (found.urls.isNotEmpty()) {
                return found.copy(
                    about = about.filterKeys {
                        it in found.urls
                    }
                )
            }
        }

        return null
    }

    // "- [Axiom](https://x.com/axiom.md): Send traces to Axiom" as "Axiom: Send traces to Axiom", by its address.
    private fun aboutIn(text: String): Map<String, String> = ENTRY
        .findAll(input = text)
        .associate { match ->
            val (title, url, description) = match.destructured

            url.trim() to listOf(title.trim(), description.trim()).filter {
                it.isNotEmpty()
            }.joinToString(separator = ": ")
        }

    private companion object {
        // The pages read at once for their links: a site answers a few at a time without slowing.
        const val AT_ONCE = 4
        const val FEW_LINKS = 5
        const val WORDS_A_LINK = 60

        val NOT_TEXT = Regex(
            pattern = "<(script|style|nav|header|footer|svg)\\b.*?</\\1>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )

        val TAG = Regex(pattern = "<[^>]+>")
        val SPACES = Regex(pattern = "\\s+")
        val ENTRY =
            Regex(pattern = "\\[([^\\]]+)\\]\\((https?://[^)\\s]+)\\)(?::[ \\t]*([^\\n]*))?")
        const val LLMS = "llms.txt"
        const val SITEMAP = "sitemap.xml"
        val MARKDOWN_LINK = Regex(pattern = "\\]\\((https?://[^)\\s]+)\\)")
        val SITEMAP_LOC = Regex(pattern = "<loc>\\s*(https?://[^<\\s]+)\\s*</loc>")
    }
}
