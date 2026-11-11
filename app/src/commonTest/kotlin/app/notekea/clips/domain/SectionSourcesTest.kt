package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Where a docs page's section comes from (the developer's choice of 2026-11-11, tried on Traceloop, Node.js, Rust,
 * Android, Kotlin, CNN and WCK): llms.txt first, then sitemap.xml, then the page's links; news is never a section.
 */
class SectionSourcesTest {
    private fun fetcher(pages: Map<String, String>) = PageFetcher { url ->
        pages[url]?.let {
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = it
                )
            )
        }
            ?: Outcome.Failure(error = ClipError.Refused(status = 404))
    }

    @Test
    fun `llms txt above the page is read first, its Markdown pages under the page's folder`() =
        runTest {
            val pages = mapOf(
                "https://x.com/docs/integrations/introduction" to "<a href=\"/docs/integrations/axiom\">Axiom</a>",
                "https://x.com/docs/llms.txt" to "# Docs\n\n- [Axiom](https://x.com/docs/integrations/axiom.md): Axiom\n" +
                    "- [Tracing](https://x.com/docs/tracing/intro.md): Tracing",
                "https://x.com/docs/sitemap.xml" to "<urlset><url><loc>https://x.com/docs/integrations/other</loc></url></urlset>"
            )

            val found =
                FindSectionUseCase(fetcher = fetcher(pages))(url = "https://x.com/docs/integrations/introduction")

            assertEquals(
                expected = listOf("https://x.com/docs/integrations/axiom.md"),
                actual = (found as Outcome.Success).value.urls
            )
        }

    @Test
    fun `without llms txt the sitemap is read, and without either the page's links`() = runTest {
        val page = "https://x.com/develop"

        val html =
            "<a href=/develop/ui>UI</a> <a href=/develop/xr>XR</a> <a href=/studio>Studio</a>"

        val fromSitemap = FindSectionUseCase(
            fetcher = fetcher(
                mapOf(
                    page to html,
                    "https://x.com/sitemap.xml" to "<urlset><url><loc>https://x.com/develop/ui</loc></url></urlset>"
                )
            )
        )(url = page) as Outcome.Success

        val alone = fetcher(mapOf(page to html))
        val fromLinks = FindSectionUseCase(fetcher = alone)(url = page) as Outcome.Success

        assertEquals(
            expected = listOf(
                listOf("https://x.com/develop/ui"),
                listOf("https://x.com/develop/ui", "https://x.com/develop/xr")
            ),
            actual = listOf(fromSitemap.value.urls, fromLinks.value.urls)
        )
    }

    @Test
    fun `a news page's day or folder is never a section`() {
        val related =
            listOf("https://x.com/2026/11/11/eeuu/other-story", "https://x.com/es/news/other-story/")

        assertEquals(
            expected = listOf(0, 0),
            actual = listOf(
                SectionLinks
                    .within(
                        url = "https://x.com/2026/11/11/eeuu/story",
                        candidates = related
                    )
                    .urls
                    .size,
                SectionLinks
                    .within(
                        url = "https://x.com/es/news/story/",
                        candidates = related
                    )
                    .urls
                    .size
            )
        )
    }

    @Test
    fun `a page's Markdown copy is read without its comments, frontmatter or the site's index note`() {
        val article = MarkdownArticle.of(
            text = "---\ntitle: Assert\n---\n> ## Documentation Index\n> Fetch it at https://x.com/llms.txt\n\n" +
                "# Assert\n\n<!--introduced_in=v0.1.21-->\n\nThe assert module provides a set of assertion functions " +
                "for verifying invariants in your code.\n\n```js\nassert.ok(true)\n\nassert.ok(1)\n```"
        )

        assertEquals(
            expected = "Assert" to listOf(
                "The assert module provides a set of assertion functions for verifying invariants in your code.",
                "```js\nassert.ok(true)\n\nassert.ok(1)\n```"
            ),
            actual = article?.title to article?.paragraphs
        )
    }
}
