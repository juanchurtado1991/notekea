package app.notekea.clips.data

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.core.domain.Outcome
import app.notekea.core.web.domain.WebResult
import app.notekea.core.web.domain.WebSearchProblem
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * DuckDuckGo's HTML results read with no network (`docs/agent-web.md`): a page as it gives one, recorded. Its ads,
 * links to DuckDuckGo's own click address, are never results (the web's evaluation, 2026-11-16).
 */
class DuckDuckGoSearchTest {
    private val asked = mutableListOf<String>()

    private val page = """
        <div class="result"><a class="result__a"
          href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.fao.org%2Ftomato&amp;rut=1">Tomato under glass</a>
          <a class="result__snippet">Water at the base, in the morning.</a></div>
        <div class="result"><a class="result__a" href="javascript:void(0)">Not a page</a></div>
        <div class="result result--ad"><a class="result__a"
          href="https://duckduckgo.com/y.js?ad_domain=amazon.com.mx&amp;u3=x">Kindle, sold here</a></div>
        <div class="result"><a class="result__a"
          href="https://duckduckgo.com/y.js?ad_domain=ebay.com&amp;u3=x">An ad without its class</a></div>
    """.trimIndent()

    @Test
    fun `results are read with their address out of the redirect, the query and the region in the address`() =
        runTest {
            val search = DuckDuckGoSearch { url ->
                asked += url

                Outcome.Success(
                    value = FetchedPage(
                        url = url,
                        html = page
                    )
                )
            }

            assertEquals(
                expected = listOf<Any>(
                    Outcome.Success(
                        value = listOf(
                            WebResult(
                                title = "Tomato under glass",
                                url = "https://www.fao.org/tomato",
                                snippet = "Water at the base, in the morning."
                            )
                        )
                    ),
                    listOf("https://html.duckduckgo.com/html/?q=tomato+watering&kl=co-es")
                ),
                actual = listOf(
                    search.search(
                        query = "tomato watering",
                        country = "CO"
                    ),
                    asked
                )
            )
        }

    @Test
    fun `a refusal is a block, and a page with no results is nothing found`() = runTest {
        val refused = DuckDuckGoSearch { Outcome.Failure(error = ClipError.Refused(status = 403)) }

        val empty = DuckDuckGoSearch { url ->
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = "<p>no</p>"
                )
            )
        }

        assertEquals(
            expected = listOf<Any>(
                Outcome.Failure(error = WebSearchProblem.BLOCKED),
                Outcome.Failure(error = WebSearchProblem.NOTHING_FOUND)
            ),
            actual = listOf(
                refused.search(
                    query = "x",
                    country = null
                ),
                empty.search(
                    query = "x",
                    country = null
                )
            )
        )
    }
}
