package app.notekea.core.embedding.data

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.ladenthin.llama.LlamaModel
import net.ladenthin.llama.args.LogFormat
import net.ladenthin.llama.value.LogLevel
import org.junit.Assume.assumeTrue
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.BiConsumer
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The bundled multilingual model through java-llama.cpp, the one integration test on the real model
 * (`ARCHITECTURE.md`: unit tests use fakes): unit vectors of its size, closeness in meaning across Spanish and English,
 * a text over its context refused, a missing file named, and the model let go when it is idle.
 */
class LlamaTextEmbedderRealModelTest {
    private val modelPath = System.getProperty(MODEL_PROPERTY)
    private val natives = Files.createTempDirectory("notekea-natives").toString()

    @Test
    fun `texts become unit vectors close in meaning across Spanish and English`() = runBlocking {
        val embedder = embedder()

        val texts = listOf(
            "¿Cuánta agua lleva la masa de las arepas?",
            "Arepas: two cups of pre-cooked corn flour and two and a half cups of warm water.",
            "Quartz counters, six weeks. The quote goes into the budget."
        )

        val (question, recipe, vendor) = (embedder.embed(texts = texts) as Outcome.Success).value

        assertEquals(
            expected = listOf(384, 384, 384),
            actual = listOf(question.size, recipe.size, vendor.size)
        )

        assertTrue(
            actual = listOf(question, recipe, vendor).all {
                abs(dot(it, it) - 1f) < TOLERANCE
            }
        )

        assertTrue(
            actual = dot(question, recipe) > dot(question, vendor) + MARGIN,
            message = "${dot(question, recipe)}"
        )
    }

    @Test
    fun `a text longer than the model reads is refused with its place and size, never cut`() =
        runBlocking {
            val embedder = embedder()

            val outcome = embedder.embed(
                texts = listOf(
                    "short",
                    List(size = 600) { "word" }.joinToString(separator = " ")
                )
            )

            val error = (outcome as Outcome.Failure).error

            assertTrue(
                actual = error is EmbeddingError.TextTooLong &&
                    error.index == 1 &&
                    error.tokens > embedder.maxTokens
            )
        }

    // Quitting while indexing cancels the call: it stops as cancelled and logs nothing, as the model has not failed.
    @Test
    fun `a cancelled call stops as cancelled, never logged as a failing model`() = runBlocking {
        val embedder = embedder()
        val errors = mutableListOf<String>()

        Logger.setLogWriters(
            object : LogWriter() {
                override fun log(
                    severity: Severity,
                    message: String,
                    tag: String,
                    throwable: Throwable?
                ) {
                    if (severity >= Severity.Error) errors += message
                }
            }
        )

        try {
            val call = launch(context = Dispatchers.Default) {
                embedder.embed(texts = List(size = 300) { "Note $it" })
            }

            delay(timeMillis = CANCEL_AFTER_MILLIS)
            call.cancelAndJoin()
        } finally {
            Logger.setLogWriters(platformLogWriter())
        }

        assertEquals(
            expected = emptyList(),
            actual = errors
        )
    }

    @Test
    fun `a missing model file is named`() = runBlocking {
        val embedder = embedder(modelPath = "/no/model.gguf")

        assertEquals(
            expected = Outcome.Failure(
                error = EmbeddingError.ModelMissing(path = "/no/model.gguf")
            ),
            actual = embedder.embed(texts = listOf("anything"))
        )
    }

    @Test
    fun `the model is let go once it has been idle`() = runBlocking {
        val embedder = embedder(idleMillis = IDLE_MILLIS)

        embedder.embed(texts = listOf("Tomato soup"))
        val loadedAfterUse = embedder.isLoaded

        withTimeout(timeMillis = WAIT_MILLIS) {
            while (embedder.isLoaded) delay(timeMillis = POLL_MILLIS)
        }

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(loadedAfterUse, embedder.isLoaded)
        )
    }

    // Audit: the idle timer was started only by a call that returned, so a cancelled one kept the model loaded for
    // good.
    @Test
    fun `the model is let go after a cancelled call too`() = runBlocking {
        val embedder = embedder(idleMillis = IDLE_MILLIS)

        val call = launch(context = Dispatchers.Default) {
            embedder.embed(texts = List(size = 300) { "Note $it" })
        }

        withTimeout(timeMillis = WAIT_MILLIS) {
            while (!embedder.isLoaded) delay(timeMillis = POLL_MILLIS)
        }

        call.cancelAndJoin()

        withTimeout(timeMillis = WAIT_MILLIS) {
            while (embedder.isLoaded) delay(timeMillis = POLL_MILLIS)
        }

        assertEquals(
            expected = false,
            actual = embedder.isLoaded
        )
    }

    @Test
    fun `closing lets the model go at once`() = runBlocking {
        val embedder = embedder()
        embedder.embed(texts = listOf("Tomato soup"))
        val loadedAfterUse = embedder.isLoaded

        embedder.close()

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(loadedAfterUse, embedder.isLoaded)
        )
    }

    // 2.9: the model was never freed in java-llama.cpp 4.2.0: every load and let go kept about 275 MB for good.
    @Test
    fun `loading the model again and again keeps no memory`() = runBlocking {
        assumeTrue(Files.exists(STATUS_FILE))
        val embedder = embedder(idleMillis = IDLE_MILLIS)

        val resident = List(size = CYCLES) {
            embedder.embed(texts = listOf("Tomato soup"))

            withTimeout(timeMillis = WAIT_MILLIS) {
                while (embedder.isLoaded) delay(timeMillis = POLL_MILLIS)
            }

            residentMegabytes()
        }

        val growth = resident.last() - resident.first()

        assertTrue(
            actual = growth < MAX_GROWTH_MB,
            message = "$resident"
        )
    }

    // 2.9: llama.cpp's default log level writes every embedded text to stderr, and with it the words of the notes.
    @Test
    fun `the words of a note stay out of the engine's logs`() = runBlocking {
        val lines = CopyOnWriteArrayList<String>()

        val capture = object : BiConsumer<LogLevel, String> {
            override fun accept(level: LogLevel, message: String) {
                lines += message
            }
        }

        LlamaModel.setLogger(LogFormat.TEXT, capture)

        embedder().embed(texts = listOf(PRIVATE_WORD))

        assertEquals(
            expected = emptyList(),
            actual = lines.filter {
                PRIVATE_WORD in it
            }
        )
    }

    private fun residentMegabytes(): Long = Files
        .readAllLines(STATUS_FILE)
        .first { it.startsWith(prefix = "VmRSS") }
        .filter(Char::isDigit)
        .toLong() / KILOBYTES

    private fun embedder(
        modelPath: String = this.modelPath,
        idleMillis: Long = DEFAULT_IDLE_MILLIS
    ) = LlamaTextEmbedder(
        modelPath = modelPath,
        nativeFolder = natives,
        idleMillis = idleMillis
    )

    private fun dot(
        first: FloatArray,
        second: FloatArray
    ): Float = first
        .indices
        .sumOf { (first[it] * second[it]).toDouble() }
        .toFloat()

    private companion object {
        const val MODEL_PROPERTY = "notekea.embedding.model"
        const val TOLERANCE = 0.001f
        const val MARGIN = 0.1f
        const val IDLE_MILLIS = 200L
        const val DEFAULT_IDLE_MILLIS = 60_000L
        const val WAIT_MILLIS = 10_000L
        const val POLL_MILLIS = 50L
        const val CANCEL_AFTER_MILLIS = 300L
        const val CYCLES = 8
        const val MAX_GROWTH_MB = 100L
        const val KILOBYTES = 1024
        const val PRIVATE_WORD = "zanahoriamorada"
        val STATUS_FILE: Path = Path.of("/proc/self/status")
    }
}
