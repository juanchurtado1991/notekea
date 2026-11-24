package app.notekea.chat.eval

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReplyCacheTest {
    private val folder = Files.createTempDirectory("notekea-replies")
    private val engine = FakeLlmEngine()
    private val cache = cacheOf(model = MODEL)

    init {
        engine.reply = { "reply ${engine.generated.size}" }
    }

    @Test
    fun `a call made again is read from disk, in another round too`() = runTest {
        val first = written(cache = cache)
        val again = written(cache = cache)
        val later = written(cache = cacheOf(model = MODEL))

        assertEquals(
            expected = "reply 1",
            actual = first
        )

        assertEquals(
            expected = listOf(first, first),
            actual = listOf(again, later)
        )

        assertEquals(
            expected = 1,
            actual = engine.generated.size
        )

        assertEquals(
            expected = "replies kept 1, asked 1",
            actual = cache.counts
        )
    }

    @Test
    fun `whatever decides a reply asks the model again`() = runTest {
        val options = listOf(
            GenerationOptions(),
            GenerationOptions(maxTokens = 9),
            GenerationOptions(temperature = 0f),
            GenerationOptions(seed = 7),
            GenerationOptions(jsonSchema = "{}")
        )

        options.forEach {
            written(
                cache = cache,
                options = it
            )
        }

        written(
            cache = cache,
            task = "another task"
        )

        written(
            cache = cache,
            system = "another system"
        )

        written(cache = cacheOf(model = "another model"))

        assertEquals(
            expected = options.size + 3,
            actual = engine.generated.size
        )
    }

    @Test
    fun `two calls cut at another place are not one`() = runTest {
        written(
            cache = cache,
            system = "ab",
            task = "c"
        )

        written(
            cache = cache,
            system = "a",
            task = "bc"
        )

        assertEquals(
            expected = 2,
            actual = engine.generated.size
        )
    }

    @Test
    fun `a reply that failed is not kept`() = runTest {
        engine.failure = LlmError.EngineFailed

        assertFailsWith<LlmException> { written(cache = cache) }

        engine.failure = null

        assertEquals(
            expected = "reply 2",
            actual = written(cache = cache)
        )
    }

    @Test
    fun `each round's counts are written a line each`() = runTest {
        written(cache = cache)
        cache.note()
        written(cache = cache)
        cache.note()

        assertEquals(
            expected = listOf("replies kept 0, asked 1", "replies kept 1, asked 1"),
            actual = Files.readAllLines(folder.resolve(ReplyCache.ROUNDS))
        )
    }

    private fun cacheOf(model: String) = ReplyCache(
        engine = engine,
        folder = folder,
        model = model
    )

    private suspend fun written(
        cache: ReplyCache,
        system: String = "system",
        task: String = "task",
        options: GenerationOptions = GenerationOptions()
    ): String {
        val messages = listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = system
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = task
            )
        )

        return cache
            .generate(
                messages = messages,
                options = options
            )
            .toList()
            .joinToString(separator = "")
    }

    private companion object {
        const val MODEL = "a model"
    }
}
