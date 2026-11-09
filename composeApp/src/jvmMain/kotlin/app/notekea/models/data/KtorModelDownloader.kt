package app.notekea.models.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.models.domain.DownloadStep
import app.notekea.models.domain.ModelDownloader
import app.notekea.models.domain.ModelError
import app.notekea.models.domain.ModelFile
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * [ModelDownloader] on Ktor: asks for the bytes after those already in the partial file (`Range`) and appends them;
 * a server that ignores the range sends the whole file, which then starts over.
 */
class KtorModelDownloader(
    private val client: HttpClient,
    private val folder: ModelFolder,
    private val dispatchers: AppDispatchers
) : ModelDownloader {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override fun download(
        file: ModelFile,
        fromByte: Long
    ): Flow<DownloadStep> = flow {
        val outcome = try {
            client.prepareGet(urlString = file.url) {
                if (fromByte > 0) header(key = HttpHeaders.Range, value = "bytes=$fromByte-")
            }.execute { response -> save(response = response, file = file, fromByte = fromByte) }
        } catch (exception: DiskWriteFailure) {
            logger.w(throwable = exception.cause) { "Download of ${file.fileName} could not be written" }
            Outcome.Failure(error = ModelError.Storage)
        } catch (exception: IOException) {
            logger.w(throwable = exception) { "Download of ${file.fileName} stopped" }
            Outcome.Failure(error = ModelError.Network)
        }
        emit(value = DownloadStep.Finished(outcome = outcome))
    }.flowOn(context = dispatchers.io)

    /** Writes the body of [response] after the [fromByte] already there, or from the start when it is the whole file. */
    private suspend fun FlowCollector<DownloadStep>.save(
        response: HttpResponse,
        file: ModelFile,
        fromByte: Long
    ): Outcome<Unit, ModelError> {
        val resumes = response.status == HttpStatusCode.PartialContent
        if (!resumes && response.status != HttpStatusCode.OK) {
            logger.w { "Download of ${file.fileName} answered ${response.status.value}" }
            return Outcome.Failure(error = ModelError.Network)
        }
        val options = if (resumes) APPEND_OPTIONS else REPLACE_OPTIONS
        var onDisk = if (resumes) fromByte else 0L
        var reported = onDisk
        val channel = response.bodyAsChannel()
        val buffer = ByteArray(BUFFER_BYTES)
        writing(block = { Files.createDirectories(folder.root) })
        writing(block = { Files.newOutputStream(folder.partial(file = file), *options) }).use { output ->
            while (true) {
                val read = channel.readAvailable(buffer, 0, buffer.size)
                if (read < 0) break
                writing(block = { output.write(buffer, 0, read) })
                onDisk += read
                if (onDisk - reported >= REPORT_EVERY_BYTES) {
                    reported = onDisk
                    emit(value = DownloadStep.Received(bytesOnDisk = onDisk))
                }
            }
        }
        emit(value = DownloadStep.Received(bytesOnDisk = onDisk))
        if (onDisk == file.sizeBytes) return Outcome.Success(value = Unit)
        logger.w { "Download of ${file.fileName} ended at $onDisk of ${file.sizeBytes} bytes" }
        return Outcome.Failure(error = ModelError.Network)
    }

    /** Runs [block], a write to the disk, telling its failures apart from the network's. */
    private inline fun <T> writing(
        block: () -> T
    ): T = try {
        block()
    } catch (exception: IOException) {
        throw DiskWriteFailure(cause = exception)
    }

    /** A write to the models folder failed (a full disk, a folder gone), not the connection. */
    private class DiskWriteFailure(
        cause: IOException
    ) : IOException(cause)

    private companion object {
        val APPEND_OPTIONS = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        val REPLACE_OPTIONS = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        const val BUFFER_BYTES = 1 shl 16
        const val LOG_TAG = "KtorModelDownloader"
        const val REPORT_EVERY_BYTES = 4L shl 20
    }
}
