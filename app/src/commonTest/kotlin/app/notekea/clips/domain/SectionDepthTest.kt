package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How deep a section's links are followed (the developer's request of 2026-11-13: the Android docs gave only the pages
 * their first page links).
 */
class SectionDepthTest {
    private val site = mapOf(
        "https://x.com/develop" to "<a href=/develop/ui>UI</a> <a href=/develop/xr>XR</a> <a href=/studio>Studio</a>",
        "https://x.com/develop/ui" to "<a href=/develop/ui/compose>Compose</a> <a href=/develop>Up</a> <a href=/blog/x>B</a>",
        "https://x.com/develop/xr" to "<a href=/develop/ui/compose>Compose</a> <a href=/develop/xr/glasses>Glasses</a>",
        "https://x.com/develop/ui/compose" to "<a href=/develop/ui/compose/layouts>Layouts</a>",
        "https://x.com/develop/xr/glasses" to "",
        "https://x.com/develop/ui/compose/layouts" to "<a href=/develop/ui/compose/layouts/flow>Flow</a>"
    )

    private val fetched = mutableListOf<String>()

    private val fetcher = PageFetcher { url ->
        fetched += url

        site[url]?.let {
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = it
                )
            )
        } ?: Outcome.Failure(error = ClipError.Refused(status = 404))
    }

    private suspend fun found(depth: Int): SectionLinks {
        val found = FindSectionUseCase(fetcher = fetcher)(
            url = "https://x.com/develop",
            depth = depth
        )

        return (found as Outcome.Success).value
    }

    @Test
    fun `one level is the page's own links, each level more the pages those link, each once and under the page`() =
        runTest {
            val one = found(depth = 1)
            val two = found(depth = 2)
            val three = found(depth = 3)

            assertEquals(
                expected = listOf(
                    listOf("ui", "xr"),
                    listOf("ui", "xr", "ui/compose", "xr/glasses"),
                    listOf("ui", "xr", "ui/compose", "xr/glasses", "ui/compose/layouts")
                ),
                actual = listOf(one, two, three).map { links ->
                    links.urls.map { it.removePrefix(prefix = "https://x.com/develop/") }
                }
            )
        }

    @Test
    fun `how many pages there are so far is told as each level is read, and one level reads no page twice`() =
        runTest {
            val counts = mutableListOf<Int>()

            FindSectionUseCase(fetcher = fetcher)(
                url = "https://x.com/develop",
                depth = 2,
                onFound = { counts += it }
            )

            assertEquals(
                expected = listOf<Any>(listOf(4), true, true),
                actual = listOf(
                    counts,
                    one(depth = 1).canGoDeeper,
                    fetched.count {
                        it == "https://x.com/develop/ui"
                    } == 1
                )
            )
        }

    private suspend fun one(depth: Int): SectionLinks {
        val copy = fetched.toList()
        val links = found(depth = depth)
        fetched.clear()
        fetched += copy

        return links
    }

    // The developer's clip of https://wordpress.org/documentation/ (2026-11-13): the site's llms.txt lists its
    // general pages, none of the documentation, and the clip took the ten pages of the top menu, with no levels asked.
    @Test
    fun `a site's index that lists nothing under the page gives way to the page's own links, and levels are offered`() =
        runTest {
            val pages = mapOf(
                "https://w.org/documentation/" to
                    "<a href=\"https://w.org/about/\">About</a> <a href=\"https://w.org/plugins/\">Plugins</a> " +
                    "<a href=\"https://w.org/documentation/category/faqs/\">FAQs</a> " +
                    "<a href=\"https://w.org/documentation/overview/\">Overview</a> " +
                    "<a href=\"https://w.org/documentation/feed/\">Feed</a> " +
                    "<a href=\"https://w.org/documentation/wp-json/\">API</a> " +
                    "<a href=\"https://w.org/documentation/xmlrpc.php?rsd\">RSD</a> " +
                    "<a href=\"https://w.org/documentation/?output_format=md\">Markdown</a>",
                "https://w.org/llms.txt" to
                    "# W\n\n- [About](https://w.org/about/): The project\n- [Plugins](https://w.org/plugins/): Plugins\n" +
                    "- [Documentation](https://w.org/documentation/): Guides",
                "https://w.org/documentation/category/faqs/" to
                    "<a href=\"https://w.org/documentation/article/faq-install/\">Installing</a>",
                "https://w.org/documentation/overview/" to ""
            )

            val fetcher = PageFetcher { url ->
                pages[url]?.let {
                    Outcome.Success(
                        value = FetchedPage(
                            url = url,
                            html = it
                        )
                    )
                } ?: Outcome.Failure(error = ClipError.Refused(status = 404))
            }

            val one =
                FindSectionUseCase(fetcher = fetcher)(url = "https://w.org/documentation/") as Outcome.Success

            val two = FindSectionUseCase(fetcher = fetcher)(
                url = "https://w.org/documentation/",
                depth = 2
            ) as Outcome.Success

            assertEquals(
                expected = listOf<Any>(
                    listOf("category/faqs/", "overview/"),
                    true,
                    listOf("category/faqs/", "overview/", "article/faq-install/")
                ),
                actual = listOf(
                    one
                        .value
                        .urls
                        .map { it.removePrefix(prefix = "https://w.org/documentation/") },
                    one.value.canGoDeeper,
                    two
                        .value
                        .urls
                        .map { it.removePrefix(prefix = "https://w.org/documentation/") }
                )
            )
        }
}
