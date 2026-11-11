package app.notekea.clips.data

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.charset
import io.ktor.utils.io.readAvailable
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

                val response = try {
                    client.get(urlString = current) { header(HttpHeaders.UserAgent, USER_AGENT) }
                } catch (exception: Exception) {
                    if (exception is kotlinx.coroutines.CancellationException) throw exception

                    return@withContext Outcome.Failure(error = errorOf(exception = exception))
                }

                val status = response.status.value
                val location = response.headers[HttpHeaders.Location]

                if (status in REDIRECTS && location != null) {
                    current = URI(current).resolve(location).toString()

                    return@repeat
                }

                val refused = ClipError.Refused(status = status)
                if (status !in OK) return@withContext Outcome.Failure(error = refused)

                return@withContext bodyOf(response = response)?.let {
                    Outcome.Success(
                        value = FetchedPage(
                            url = current,
                            html = it
                        )
                    )
                } ?: Outcome.Failure(error = ClipError.TooLarge)
            }

            Outcome.Failure(error = ClipError.Refused(status = TOO_MANY_REDIRECTS))
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

    private suspend fun bodyOf(response: HttpResponse): String? {
        val channel = response.bodyAsChannel()
        val bytes = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(size = BUFFER)

        while (!channel.isClosedForRead) {
            val read = channel.readAvailable(buffer)
            if (read > 0) bytes.write(buffer, 0, read)
            if (bytes.size() > MAX_BYTES) return null
            if (read < 0) break
        }

        // The charset the response declares, else UTF-8: an older site's Latin-1 page keeps its accents.
        return bytes.toString(response.charset() ?: Charsets.UTF_8)
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

    private companion object {
        const val HTTP = "http"
        const val HTTPS = "https"
        const val USER_AGENT = "Notekea (web clip)"
        const val MAX_REDIRECTS = 5
        const val TOO_MANY_REDIRECTS = 310
        const val MAX_BYTES = 3 * 1024 * 1024
        const val BUFFER = 64 * 1024
        const val TIMEOUT_MILLIS = 20_000L
        const val CONNECT_MILLIS = 10_000L
        val OK = 200..299
        val REDIRECTS = setOf(301, 302, 303, 307, 308)
    }
}
