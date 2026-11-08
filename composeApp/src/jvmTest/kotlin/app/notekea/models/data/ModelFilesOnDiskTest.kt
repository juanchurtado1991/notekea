package app.notekea.models.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.models.domain.DownloadStep
import app.notekea.models.domain.ModelError
import app.notekea.models.domain.ModelFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * `ROADMAP.md` 3.1 on real files and a real HTTP server: a model downloads with Ktor, resumes with a `Range`, survives
 * a server that ignores it or a dropped connection, and is installed only when its SHA-256 matches.
 */
@OptIn(ExperimentalPathApi::class)
class ModelFilesOnDiskTest {
    private val bytes = Random(seed = 7).nextBytes(size = 300_000)
    private val server = RangeFileServer(body = bytes)
    private val root = Files.createTempDirectory(TEMP_PREFIX)
    private val folder = ModelFolder(root = root.resolve(MODELS))
    private val dispatchers = JvmAppDispatchers()
    private val client = HttpClient(engineFactory = OkHttp) { expectSuccess = false }
    private val downloader = KtorModelDownloader(client = client, folder = folder, dispatchers = dispatchers)
    private val storage = NioModelStorage(modelFolder = folder, dispatchers = dispatchers)
    private val file = ModelFile(
        fileName = FILE_NAME,
        url = server.url,
        sizeBytes = bytes.size.toLong(),
        sha256 = sha256(bytes = bytes)
    )

    @AfterTest
    fun tearDown() {
        client.close()
        server.close()
        root.deleteRecursively()
    }

    @Test
    fun `a whole download is checked and installed under its own name`() = runBlocking {
        val finished = downloader.download(file = file, fromByte = 0).last()
        val installed = storage.verifyAndInstall(file = file)

        assertEquals(expected = DownloadStep.Finished(outcome = Outcome.Success(value = Unit)), actual = finished)
        assertEquals(expected = Outcome.Success(value = Unit), actual = installed)
        assertContentEquals(expected = bytes, actual = Files.readAllBytes(folder.installed(file = file)))
        assertEquals(
            expected = true to 0L,
            actual =
            storage.isInstalled(file = file) to storage.partialBytes(file = file)
        )
    }

    @Test
    fun `a dropped connection keeps its bytes, and the next try asks only for the rest`() = runBlocking {
        server.cutAfter = 100_000
        val dropped = downloader.download(file = file, fromByte = 0).last()
        val kept = storage.partialBytes(file = file)
        server.cutAfter = null
        downloader.download(file = file, fromByte = kept).last()

        assertEquals(
            expected = DownloadStep.Finished(outcome = Outcome.Failure(error = ModelError.Network)),
            actual = dropped
        )
        assertEquals(expected = 100_000L, actual = kept)
        assertEquals(expected = listOf(null, "bytes=100000-"), actual = server.ranges)
        assertEquals(expected = Outcome.Success(value = Unit), actual = storage.verifyAndInstall(file = file))
    }

    @Test
    fun `a server that ignores the range sends the whole file, which starts over`() = runBlocking {
        Files.createDirectories(folder.root)
        Files.write(folder.partial(file = file), bytes.copyOfRange(fromIndex = 0, toIndex = 1_000))
        server.ignoresRange = true

        downloader.download(file = file, fromByte = 1_000).last()

        assertContentEquals(expected = bytes, actual = Files.readAllBytes(folder.partial(file = file)))
    }

    @Test
    fun `a server error is a network failure that writes nothing`() = runBlocking {
        server.status = 404

        val finished = downloader.download(file = file, fromByte = 0).last()

        assertEquals(
            expected = DownloadStep.Finished(outcome = Outcome.Failure(error = ModelError.Network)),
            actual = finished
        )
        assertEquals(expected = 0L, actual = storage.partialBytes(file = file))
    }

    @Test
    fun `a file that does not match its checksum is deleted, never installed`() = runBlocking {
        val wrong = file.copy(sha256 = sha256(bytes = byteArrayOf(1)))
        downloader.download(file = wrong, fromByte = 0).last()

        val installed = storage.verifyAndInstall(file = wrong)

        assertEquals(expected = Outcome.Failure(error = ModelError.ChecksumMismatch), actual = installed)
        assertEquals(
            expected = false to 0L,
            actual =
            storage.isInstalled(file = wrong) to storage.partialBytes(file = wrong)
        )
    }

    @Test
    fun `deleting removes the installed file, and the disk reports free space`() = runBlocking {
        downloader.download(file = file, fromByte = 0).last()
        storage.verifyAndInstall(file = file)

        storage.delete(file = file)

        assertEquals(expected = false, actual = storage.isInstalled(file = file))
        assertEquals(expected = true, actual = storage.freeBytes() > 0)
    }

    private companion object {
        const val FILE_NAME = "model.gguf"
        const val MODELS = "models"
        const val TEMP_PREFIX = "notekea-models"

        fun sha256(
            bytes: ByteArray
        ): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(separator = "") {
            "%02x".format(it)
        }
    }
}
