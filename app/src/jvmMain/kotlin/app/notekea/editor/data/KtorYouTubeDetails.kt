package app.notekea.editor.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.YouTubeLinks
import app.notekea.editor.domain.YouTubeDetails
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * [YouTubeDetails] from YouTube's oEmbed (its title and picture address) and the picture itself, at YouTube's own
 * addresses only, with limits on time and size; the picture is kept in [folder] by the video's id.
 */
class KtorYouTubeDetails(
    private val folder: Path,
    private val dispatchers: AppDispatchers
) : YouTubeDetails {
    private val logger = Logger.withTag(tag = LOG_TAG)

    private val client by lazy {
        HttpClient(engineFactory = OkHttp) {
            expectSuccess = false

            install(HttpTimeout) { requestTimeoutMillis = TIMEOUT_MILLIS }
        }
    }

    override suspend fun fetch(id: String): String? = withContext(context = dispatchers.io) {
        try {
            val response = client
                .get(urlString = OEMBED) {
                    parameter("url", YouTubeLinks.pageOf(id = id))
                    parameter("format", "json")
                }

            if (!response.status.isSuccess()) return@withContext null
            val json = response.bodyAsText()

            OEmbedJson
                .stringOf(
                    json = json,
                    name = "thumbnail_url"
                )
                ?.takeIf { isYouTubePicture(url = it) }
                ?.let {
                    keepThumbnail(
                        id = id,
                        url = it
                    )
                }

            OEmbedJson.stringOf(
                json = json,
                name = "title"
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            logger.w { "YouTube details failed: ${exception::class.simpleName}" }
            null
        }
    }

    override suspend fun thumbnail(id: String): ByteArray? = withContext(context = dispatchers.io) {
        val file = fileOf(id = id) ?: return@withContext null

        try {
            if (Files.isRegularFile(file)) Files.readAllBytes(file) else null
        } catch (_: IOException) {
            null
        }
    }

    private suspend fun keepThumbnail(
        id: String,
        url: String
    ) {
        val file = fileOf(id = id) ?: return
        val response: HttpResponse = client.get(urlString = url)
        if (!response.status.isSuccess()) return

        val bytes = response.bodyAsBytes()
        if (bytes.size > MAX_PICTURE_BYTES) return

        Files.createDirectories(folder)
        Files.write(file, bytes)
    }

    // The id is YouTube's eleven letters, digits, "-" and "_", so it never names a file outside the folder.
    private fun fileOf(id: String): Path? = id
        .takeIf { ID.matches(input = it) }
        ?.let { folder.resolve("$it$PICTURE_EXTENSION") }

    private fun isYouTubePicture(url: String): Boolean = PICTURE_HOST.containsMatchIn(input = url)

    private companion object {
        const val LOG_TAG = "KtorYouTubeDetails"
        const val OEMBED = "https://www.youtube.com/oembed"
        const val TIMEOUT_MILLIS = 15_000L
        const val MAX_PICTURE_BYTES = 2_000_000
        const val PICTURE_EXTENSION = ".jpg"
        val ID = Regex(pattern = "[A-Za-z0-9_-]{11}")
        val PICTURE_HOST = Regex(pattern = "^https://i\\d?\\.ytimg\\.com/")
    }
}
