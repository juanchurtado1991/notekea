package app.notekea.clips.data

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.charset
import io.ktor.http.contentLength
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException

/**
 * [PageFetcher] with Ktor (`GUIDELINES.md` §13): http and https only; every address, the first and each redirect's,
 * is resolved and refused when it is on this computer or its local network; at most [MAX_BYTES] in [TIMEOUT_MILLIS].
 */
class KtorPageFetcher(private val dispatchers: AppDispatchers) : PageFetcher {
    private val client by lazy {
        HttpClient(engineFactory = OkHttp) {
            engine {
                config { dns(LocalRefusingDns()) }
            }

            expectSuccess = false
            followRedirects = false

            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MILLIS
                connectTimeoutMillis = CONNECT_MILLIS
            }
        }
    }

    override suspend fun fetch(url: String): Outcome<FetchedPage, ClipError> =
        withContext(context = dispatchers.io) {
            var current = url

            repeat(times = MAX_REDIRECTS) {
                checked(url = current)?.let {
                    return@withContext Outcome.Failure(error = it)
                }

                // Streamed, not `get`: a `get` holds the whole body in memory before the cap could be checked.
                val step = try {
                    client
                        .prepareGet(urlString = current) {
                            header(HttpHeaders.UserAgent, USER_AGENT)
                        }
                        .execute {
                            stepOf(
                                response = it,
                                url = current
                            )
                        }
                } catch (exception: Exception) {
                    if (exception is kotlinx.coroutines.CancellationException) throw exception

                    return@withContext Outcome.Failure(error = errorOf(exception = exception))
                }

                when (step) {
                    is Step.Redirect -> current = step.to
                    is Step.Done -> return@withContext step.outcome
                }
            }

            Outcome.Failure(error = ClipError.Refused(status = TOO_MANY_REDIRECTS))
        }

    private suspend fun stepOf(
        response: HttpResponse,
        url: String
    ): Step {
        val status = response.status.value
        val refusal = Outcome.Failure(error = ClipError.Refused(status = status))
        val refused = Step.Done(outcome = refusal)

        val location = response
            .headers[HttpHeaders.Location]
            ?.takeIf {
                status in REDIRECTS
            }

        if (location != null) {
            // A Location no address can be made of ("/docs/my page") is a page that cannot be read.
            val next = try {
                URI(url).resolve(location).toString()
            } catch (_: IllegalArgumentException) {
                return refused
            }

            return Step.Redirect(to = next)
        }

        if (status !in OK) return refused

        val html = PageBody.read(
            channel = response.bodyAsChannel(),
            declaredLength = response.contentLength(),
            declaredCharset = response.charset(),
            maxBytes = MAX_BYTES
        )

        val outcome = html?.let {
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = it
                )
            )
        } ?: Outcome.Failure(error = ClipError.TooLarge)

        return Step.Done(outcome = outcome)
    }

    // An address Notekea may fetch: http or https to a host on the internet.
    private fun checked(url: String): ClipError? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return ClipError.BadAddress
        val scheme = uri.scheme?.lowercase()
        val host = uri.host ?: return ClipError.BadAddress
        if (scheme != HTTP && scheme != HTTPS) return ClipError.BadAddress

        val addresses = try {
            InetAddress.getAllByName(host).toList()
        } catch (_: UnknownHostException) {
            return ClipError.Offline
        }

        val isLocal = addresses.any { LocalRefusingDns.isLocal(address = it) }

        return if (isLocal) ClipError.LocalAddress else null
    }

    private fun errorOf(exception: Exception): ClipError {
        val name = exception::class.simpleName.orEmpty()

        val isLocal = generateSequence<Throwable>(exception) { it.cause }.any {
            it is LocalRefusingDns.Refused
        }

        return when {
            isLocal -> ClipError.LocalAddress
            "Timeout" in name -> ClipError.TooSlow
            else -> ClipError.Offline
        }
    }

    // What one request leads to: the next address of a redirect, or the fetch's outcome.
    private sealed interface Step {
        data class Redirect(val to: String) : Step

        data class Done(val outcome: Outcome<FetchedPage, ClipError>) : Step
    }

    private companion object {
        const val HTTP = "http"
        const val HTTPS = "https"
        const val USER_AGENT = "Notekea (web clip)"
        const val MAX_REDIRECTS = 5
        const val TOO_MANY_REDIRECTS = 310

        // A news page of 2026 weighs 4 MB with its scripts (CNN, the developer's try of 2026-11-11).
        const val MAX_BYTES = 10 * 1024 * 1024
        const val TIMEOUT_MILLIS = 20_000L
        const val CONNECT_MILLIS = 10_000L
        val OK = 200..299
        val REDIRECTS = setOf(301, 302, 303, 307, 308)
    }
}
