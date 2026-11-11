package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A documentation page clipped with the pages it links under its own path (the developer's request of 2026-11-11):
 * which links count, how the pages are clipped, and the folder of notes with the page as their index.
 */
class ClipSectionTest {
    private val engine = FakeLlmEngine().apply {
        readiness = Outcome.Failure(error = LlmError.NoModel)
    }

    @Test
    fun `the links under the page's folder count, the rest of the site, files and the page itself do not`() {
        val page = FetchedPage(
            url = "https://docs.example.com/docs/openllmetry/integrations/introduction",
            html = """
                <a href="/docs/openllmetry/integrations/axiom">Axiom</a>
                <a href="datadog">Datadog</a>
                <a href="https://docs.example.com/docs/openllmetry/integrations/axiom/">Axiom again</a>
                <a href="/docs/openllmetry/integrations/introduction#catalog">This page</a>
                <a href="/docs/openllmetry/tracing/annotations">Another section</a>
                <a href="https://elsewhere.com/docs/openllmetry/integrations/x">Another site</a>
                <a href="/docs/openllmetry/integrations/logo.png">A file</a>
                <a href="mailto:hi@example.com">Mail</a>
            """.trimIndent()
        )

        val links = SectionLinks.of(page = page)

        assertEquals(
            expected = listOf(
                "https://docs.example.com/docs/openllmetry/integrations/",
                "https://docs.example.com/docs/openllmetry/integrations/axiom",
                "https://docs.example.com/docs/openllmetry/integrations/datadog"
            ),
            actual = listOf(links.prefix) + links.urls
        )
    }

    @Test
    fun `at most thirty pages are taken, the rest counted as left out`() {
        val html = (1..34).joinToString(separator = "\n") { "<a href=\"/docs/a/page-$it\">$it</a>" }

        val links = SectionLinks.of(
            page = FetchedPage(
                url = "https://x.com/docs/a/intro",
                html = html
            )
        )

        assertEquals(
            expected = 30 to 4,
            actual = links.urls.size to links.more
        )
    }

    @Test
    fun `each page is clipped in turn, one that is no article is skipped, and the page is their index`() =
        runTest {
            val clip = ClipWebPageUseCase(
                fetcher = { url ->
                    Outcome.Success(
                        value = FetchedPage(
                            url = url,
                            html = "<html>$url</html>"
                        )
                    )
                },
                extractor = { page ->
                    when {
                        page.url.endsWith(suffix = "/empty") -> NOT_ARTICLE

                        else -> Outcome.Success(
                            value = Article(
                                title = page
                                    .url
                                    .substringAfterLast(delimiter = '/')
                                    .replaceFirstChar { it.uppercase() },
                                paragraphs = listOf("About ${page.url}.")
                            )
                        )
                    }
                },
                llmEngine = engine,
                supportCheck = SupportCheck(llmEngine = engine)
            )

            val links = SectionLinks(
                prefix = "https://x.com/docs/",
                urls = listOf("https://x.com/docs/axiom", "https://x.com/docs/empty", "https://x.com/docs/datadog"),
                more = 0
            )

            val steps = ClipSectionUseCase(clipWebPage = clip)(
                url = "https://x.com/docs/overview",
                links = links
            ).toList()

            val done = steps.last() as SectionStep.Done

            val notes = WebClipSection.notesOf(
                done = done,
                today = "2026-11-11"
            )

            assertEquals(
                expected = listOf<Any?>(
                    listOf(1, 2, 3, 4).map {
                        it - 1
                    },
                    listOf("Web sources/Overview.md", "Web sources/Overview/Axiom.md", "Web sources/Overview/Datadog.md"),
                    true,
                    true
                ),
                actual = listOf(
                    steps.filterIsInstance<SectionStep.Clipping>().map { it.done },
                    notes.map { it.first.value },
                    PAGES in notes.first().second,
                    "- https://x.com/docs/empty" in notes.first().second
                )
            )
        }

    @Test
    fun `two pages of one title get two names`() {
        val clip = { title: String ->
            WebClip(
                url = "u",
                title = title,
                notes = "",
                article = Article(title, listOf("t"))
            )
        }

        val notes = WebClipSection.notesOf(
            done = SectionStep.Done(
                main = clip("Docs"),
                pages = listOf(clip("Setup"), clip("Setup")),
                skipped = emptyList()
            ),
            today = "2026-11-11"
        )

        assertEquals(
            expected = listOf("Web sources/Docs.md", "Web sources/Docs/Setup.md", "Web sources/Docs/Setup 2.md"),
            actual = notes.map { it.first.value }
        )
    }

    private companion object {
        val NOT_ARTICLE = Outcome.Failure(error = ClipError.NotArticle)
        const val PAGES = "## Pages\n\n- [[Overview/Axiom|Axiom]]\n- [[Overview/Datadog|Datadog]]"
    }
}
