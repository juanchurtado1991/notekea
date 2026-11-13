package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a probe of fifteen real sites found (2026-11-13): the search for a section stopped on an empty link, a wiki's
 * article offered hundreds of other subjects, and a link written with ".." or left by a template became a page.
 */
class RobustClipTest {
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

    private suspend fun found(
        url: String,
        html: String
    ): List<String> {
        val section = FindSectionUseCase(fetcher = fetcher(mapOf(url to html)))(url = url)

        return (section as Outcome.Success).value.urls
    }

    @Test
    fun `an empty link, a stylesheet's address and a template's leftover are no pages, and dots are walked`() =
        runTest {
            val html = "<link rel=\"dns-prefetch\" href=\"/docs/fonts\"><a href=\"\">Top</a>" +
                "<a class=\"x\" href=\"/docs/a\">A</a><a href=\"/docs/b/../c\">C</a>" +
                "<a href=\"/docs/\\\\\">Broken</a><a href=\"/docs/{{ page }}\">Template</a>" +
                "<a href=\"./d\">D</a>"

            assertEquals(
                expected = listOf("https://x.com/docs/a", "https://x.com/docs/c", "https://x.com/docs/d"),
                actual = found(
                    url = "https://x.com/docs/",
                    html = html
                )
            )
        }

    @Test
    fun `an encyclopedia's article has no section, a docs folder of many pages beside it has`() =
        runTest {
            val subjects =
                (1..80).joinToString(separator = "") { "<a href=\"/wiki/Subject_$it\">S</a>" }

            val wiki = subjects + "<a href=\"/wiki/Special:Random\">Random</a>"

            val handbook =
                (1..80).joinToString(separator = "") { "<a href=\"/docs/handbook/page-$it.html\">P</a>" }

            val counts = listOf(
                found(
                    url = "https://x.org/wiki/Arepa",
                    html = wiki
                ).size,
                found(
                    url = "https://x.org/docs/handbook/intro.html",
                    html = handbook
                ).size,
                found(
                    url = "https://x.org/wiki/Arepa",
                    html = "<a href=\"/wiki/Help:Contents\">Help</a><a href=\"/wiki/Corn\">Corn</a>"
                ).size
            )

            assertEquals(
                expected = listOf(0, SectionLinks.MAX_PAGES, 1),
                actual = counts
            )
        }

    @Test
    fun `the links above an article and its buttons' words are left out, an index of links is kept`() {
        val article = ArticleBlocks.cleaned(
            blocks = listOf(
                "[Docs](https://x.com/docs)",
                "- [Guides](https://x.com/guides)\n- [Reference](https://x.com/reference)",
                "### Layouts Stay organized with collections Save and categorize content based on your preferences.",
                "Back to top",
                "Compose lays out [rows](https://x.com/rows) and columns.",
                "[Edit](https://github.com/x/edit)",
                "- [Rows](https://x.com/rows)"
            )
        )

        val index = ArticleBlocks.cleaned(
            blocks = listOf("- [Rows](https://x.com/rows)\n- [Columns](https://x.com/columns)")
        )

        assertEquals(
            expected = listOf(
                listOf(
                    "### Layouts",
                    "Compose lays out [rows](https://x.com/rows) and columns.",
                    "- [Rows](https://x.com/rows)"
                ),
                listOf("- [Rows](https://x.com/rows)\n- [Columns](https://x.com/columns)")
            ),
            actual = listOf(article, index)
        )
    }

    @Test
    fun `a note is named after its page, not after its site`() {
        assertEquals(
            expected = listOf("Create pages", "HTTPS – Advanced Administration Handbook", "| odd"),
            actual = listOf(
                ArticleBlocks.titleOf(title = "Create pages | Documentation | WordPress.org"),
                ArticleBlocks.titleOf(title = " HTTPS – Advanced Administration Handbook "),
                ArticleBlocks.titleOf(title = " | odd")
            )
        )
    }
}
