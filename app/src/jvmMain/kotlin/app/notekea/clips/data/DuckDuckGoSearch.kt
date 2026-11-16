package app.notekea.clips.data

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.domain.Outcome
import app.notekea.core.web.domain.WebResult
import app.notekea.core.web.domain.WebSearchProblem
import app.notekea.core.web.domain.WebSearchProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Web search with DuckDuckGo's HTML results (`docs/agent-web.md`, #144): nothing to set up, so it may stop working.
 * Read through [fetcher], with its limits and its refusal of the local network, one search at a time and at least
 * [PAUSE] apart; never retried when blocked.
 */
class DuckDuckGoSearch(private val fetcher: PageFetcher) : WebSearchProvider {
    private val oneAtATime = Mutex()

    // When the last search was sent, as a monotonic mark: the next waits for the pause after it.
    private var lastSent: TimeMark? = null

    override suspend fun search(
        query: String,
        country: String?
    ): Outcome<List<WebResult>, WebSearchProblem> = oneAtATime.withLock {
        lastSent?.let { sent ->
            val waited = sent.elapsedNow()
            if (waited < PAUSE) delay(duration = PAUSE - waited)
        }

        lastSent = TimeSource.Monotonic.markNow()

        val region = country?.let { "&kl=${it.lowercase()}-${languageOf(country = it)}" }.orEmpty()
        val url = "$ADDRESS?q=${URLEncoder.encode(query, Charsets.UTF_8)}$region"

        when (val fetched = fetcher.fetch(url = url)) {
            is Outcome.Failure -> Outcome.Failure(error = problemOf(error = fetched.error))

            is Outcome.Success -> {
                val results = resultsOf(html = fetched.value.html)

                if (results.isEmpty()) {
                    Outcome.Failure(error = WebSearchProblem.NOTHING_FOUND)
                } else {
                    Outcome.Success(value = results.take(n = MOST_RESULTS))
                }
            }
        }
    }

    // Each result's title, its address out of DuckDuckGo's redirect, and its snippet.
    private fun resultsOf(html: String): List<WebResult> = Jsoup
        .parse(html)
        .select(".result")
        .mapNotNull { result ->
            val link = result.selectFirst("a.result__a") ?: return@mapNotNull null
            val url = addressOf(href = link.attr("href")) ?: return@mapNotNull null

            WebResult(
                title = link.text(),
                url = url,
                snippet = result
                    .selectFirst(".result__snippet")
                    ?.text()
                    .orEmpty()
            )
        }

    private fun addressOf(href: String): String? {
        val wrapped = href.substringAfter(
            delimiter = "uddg=",
            missingDelimiterValue = ""
        )

        val address = if (wrapped.isEmpty()) {
            href
        } else {
            URLDecoder.decode(wrapped.substringBefore(delimiter = '&'), Charsets.UTF_8)
        }

        return address.takeIf {
            it.startsWith(prefix = "https://") || it.startsWith(prefix = "http://")
        }
    }

    private fun problemOf(error: ClipError): WebSearchProblem = when (error) {
        is ClipError.Refused -> WebSearchProblem.BLOCKED
        else -> WebSearchProblem.OFFLINE
    }

    // DuckDuckGo's region is a country and a language: "co-es", "us-en".
    private fun languageOf(country: String): String = LANGUAGES[country.uppercase()] ?: "es"

    private companion object {
        const val ADDRESS = "https://html.duckduckgo.com/html/"
        const val MOST_RESULTS = 8
        val PAUSE = 2_500.milliseconds
        val LANGUAGES = mapOf(
            "US" to "en", "GB" to "en", "CA" to "en", "BR" to "pt", "PT" to "pt", "FR" to "fr", "DE" to "de",
            "IT" to "it", "JP" to "jp"
        )
    }
}
