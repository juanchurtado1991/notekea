package app.notekea.clips.domain

import app.notekea.core.domain.Outcome

/**
 * The pages a page has under its own path, before clipping (the developer's choice of 2026-11-11): from the site's
 * llms.txt first, which lists its pages as Markdown for machines, then its sitemap.xml, then the page's own links.
 */
class FindSectionUseCase(private val fetcher: PageFetcher) {
    suspend operator fun invoke(url: String): Outcome<SectionLinks, ClipError> {
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

        return Outcome.Success(value = listed ?: SectionLinks.of(page = page))
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

            val found = SectionLinks.within(
                url = url,
                candidates = links
                    .findAll(input = text)
                    .map { it.groupValues[1].trim() }
                    .toList()
            )

            if (found.urls.isNotEmpty()) return found
        }

        return null
    }

    private companion object {
        const val LLMS = "llms.txt"
        const val SITEMAP = "sitemap.xml"
        val MARKDOWN_LINK = Regex(pattern = "\\]\\((https?://[^)\\s]+)\\)")
        val SITEMAP_LOC = Regex(pattern = "<loc>\\s*(https?://[^<\\s]+)\\s*</loc>")
    }
}
