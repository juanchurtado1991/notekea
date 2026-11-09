package app.notekea.ai.data

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * A local HTTP server of one file, [body], as Hugging Face serves a model: it answers a `Range` with 206 and the rest
 * of the bytes, unless [ignoresRange]; [status] other than 200 answers with no body, and [cutAfter] closes the
 * connection after that many bytes, as a dropped connection does. Every `Range` asked is kept in [ranges].
 */
class RangeFileServer(
    private val body: ByteArray
) : AutoCloseable {
    var ignoresRange = false
    var status = OK
    var cutAfter: Int? = null
    val ranges = mutableListOf<String?>()
    private val server = HttpServer.create(InetSocketAddress(LOOPBACK, 0), 0).apply {
        createContext(PATH) { exchange -> exchange.use { answer(exchange = it) } }
        start()
    }

    val url: String get() = "http://$LOOPBACK:${server.address.port}$PATH"

    private fun answer(
        exchange: HttpExchange
    ) {
        val range = exchange.requestHeaders.getFirst(RANGE_HEADER)
        ranges += range
        if (status != OK) {
            exchange.sendResponseHeaders(status, NO_BODY)
            return
        }
        val from =
            range?.takeUnless { ignoresRange }?.removePrefix(prefix = RANGE_PREFIX)?.removeSuffix(suffix = "-")?.toInt()
                ?: 0
        val bytes = body.copyOfRange(fromIndex = from, toIndex = body.size)
        exchange.sendResponseHeaders(if (from > 0) PARTIAL else OK, bytes.size.toLong())
        exchange.responseBody.write(bytes, 0, cutAfter?.coerceAtMost(maximumValue = bytes.size) ?: bytes.size)
    }

    override fun close() {
        server.stop(0)
    }

    private companion object {
        const val LOOPBACK = "127.0.0.1"
        const val NO_BODY = -1L
        const val OK = 200
        const val PARTIAL = 206
        const val PATH = "/model.gguf"
        const val RANGE_HEADER = "Range"
        const val RANGE_PREFIX = "bytes="
    }
}
