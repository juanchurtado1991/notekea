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
import org.jsoup.nodes.Document
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
            is Outcome.Success -> outcomeOf(html = fetched.value.html)
        }
    }

    // A page with no results is either DuckDuckGo's "no results" or its challenge for bots, which a rate-limited search
    // gets (HTTP 202, its "anomaly" form): that one is a block, not nothing found (the bug hunt of 2026-11-17, E6).
    private fun outcomeOf(html: String): Outcome<List<WebResult>, WebSearchProblem> {
        val page = Jsoup.parse(html)
        val results = resultsOf(page = page)

        val saysBots = page
            .text()
            .contains(
                other = BOTS_TOO,
                ignoreCase = true
            )

        val isChallenge = page.selectFirst(CHALLENGE) != null || saysBots

        val isBlocked =
            results.isEmpty() &&
                page.selectFirst(NO_RESULTS) == null &&
                isChallenge

        return when {
            isBlocked -> Outcome.Failure(error = WebSearchProblem.BLOCKED)
            results.isEmpty() -> Outcome.Failure(error = WebSearchProblem.NOTHING_FOUND)
            else -> Outcome.Success(value = results.take(n = MOST_RESULTS))
        }
    }

    // Each result's title, its address out of DuckDuckGo's redirect, and its snippet. Ads are left out: their link is
    // DuckDuckGo's own click address, which the eval saw ticked as a source and read as nothing (2026-11-16).
    private fun resultsOf(page: Document): List<WebResult> = page
        .select(".result:not(.result--ad)")
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

        val isPage =
            address.startsWith(prefix = "https://") || address.startsWith(prefix = "http://")

        val isOwn = address
            .substringAfter(delimiter = "://")
            .substringBefore(delimiter = '/')
            .endsWith(suffix = "duckduckgo.com")

        return address.takeIf {
            isPage && !isOwn
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
        const val CHALLENGE =
            "[class*=anomaly], [id*=anomaly], form[action*=anomaly], #challenge-form"
        const val NO_RESULTS = ".no-results"
        const val BOTS_TOO = "bots use DuckDuckGo too"
        val LANGUAGES = mapOf(
            "US" to "en", "GB" to "en", "CA" to "en", "BR" to "pt", "PT" to "pt", "FR" to "fr", "DE" to "de",
            "IT" to "it", "JP" to "jp"
        )
    }
}
