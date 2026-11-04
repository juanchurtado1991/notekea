import com.sun.net.httpserver.HttpServer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Timeout
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadVerifiedFileTaskTest {
    private val folder = Files.createTempDirectory("download-verified").toFile()
    private val target = File(folder, "models/model.gguf")
    private val partial = File(folder, "models/model.gguf.part")

    @AfterTest
    fun tearDown() {
        folder.deleteRecursively()
    }

    @Test
    fun `a download with the right checksum becomes the file`() {
        val source = File(folder, "source").apply { writeText("the model") }
        task(url = source.toURI().toString(), sha256 = sha256Of(text = "the model")).download()
        assertEquals("the model", target.readText())
        assertFalse(partial.exists())
    }

    @Test
    fun `a checksum mismatch fails and leaves nothing behind`() {
        val source = File(folder, "source").apply { writeText("a tampered model") }
        val failure = assertFailsWith<IllegalStateException> {
            task(url = source.toURI().toString(), sha256 = sha256Of(text = "the model")).download()
        }
        assertTrue(failure.message.orEmpty().contains("SHA-256"))
        assertFalse(target.exists())
        assertFalse(partial.exists())
    }

    @Test
    fun `a download that breaks halfway leaves no partial file`() {
        val server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
        server.createContext("/") { exchange ->
            exchange.sendResponseHeaders(200, PROMISED_BYTES)
            exchange.responseBody.write("only the start".toByteArray())
            exchange.responseBody.flush()
            exchange.close()
        }
        server.start()
        try {
            val url = "http://127.0.0.1:${server.address.port}/model"
            assertFailsWith<java.io.IOException> { task(url = url, sha256 = "any").download() }
            assertFalse(partial.exists())
        } finally {
            server.stop(0)
        }
    }

    @Test
    @Timeout(value = 30, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    fun `a server that never answers fails instead of hanging the build`() {
        ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { silent ->
            val task = task(url = "http://127.0.0.1:${silent.localPort}/model", sha256 = "any")
            task.timeoutMillis.set(SHORT_TIMEOUT_MILLIS)
            assertFailsWith<java.net.SocketTimeoutException> { task.download() }
            assertFalse(partial.exists())
        }
    }

    private fun task(
        url: String,
        sha256: String
    ): DownloadVerifiedFileTask = ProjectBuilder.builder().build().tasks
        .register("download", DownloadVerifiedFileTask::class.java).get().also {
            it.url.set(url)
            it.sha256.set(sha256)
            it.file.set(target)
        }

    private fun sha256Of(text: String): String = MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray()).joinToString(separator = "") { "%02x".format(it) }

    private companion object {
        const val PROMISED_BYTES = 1_000L
        const val SHORT_TIMEOUT_MILLIS = 200
    }
}
