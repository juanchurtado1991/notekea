package app.notekea.chat.eval

import app.notekea.core.llm.domain.ChatMessage
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.ladenthin.llama.LlamaModel
import net.ladenthin.llama.parameters.ModelParameters
import java.nio.file.Files
import java.nio.file.Path

/**
 * The Medium chat model as llama.cpp gives it, for what step 0 of `docs/agent-loop.md` measures and the app's engine
 * does not offer yet: one context shared by the slots ([isShared]), the model's own tool calls, a grammar of our
 * own. [contextTokens] is the whole context, split among [slots] unless shared.
 */
class RawChatModel(
    slots: Int = 1,
    contextTokens: Int = CONTEXT_TOKENS,
    isShared: Boolean = false
) : AutoCloseable {
    private val json = ObjectMapper()

    private val model = LlamaModel(
        ModelParameters()
            .setModel(FILE.toString())
            .setParallel(slots)
            .setCtxSize(contextTokens)
            .setKvUnified(isShared)
            .setGpuLayers(ALL_LAYERS)
            .setLogVerbosity(ERRORS_ONLY)
    )

    /**
     * The model's reply to [messages], of [maxTokens] at most and without thinking; [request] adds what a call
     * needs to the body llama.cpp reads (`tools`, `json_schema`, `grammar`).
     */
    fun reply(
        messages: List<ChatMessage>,
        maxTokens: Int,
        request: ObjectNode.() -> Unit = {}
    ): RawReply {
        val body = json.createObjectNode()
        val written = body.putArray("messages")

        messages.forEach {
            written
                .addObject()
                .put(
                    "role",
                    it
                        .role
                        .name
                        .lowercase()
                )
                .put("content", it.text)
        }

        body
            .put("n_predict", maxTokens)
            .put("temperature", 0)
            .put("seed", SEED)
            .put("reasoning_budget_tokens", 0)
            .putObject("chat_template_kwargs")
            .put("enable_thinking", false)

        body.request()

        return try {
            read(reply = json.readTree(model.handleChatCompletions(body.toString())))
        } catch (exception: RuntimeException) {
            RawReply(
                text = "FAILED: ${exception.message.orEmpty().take(n = FAILURE_SHOWN)}",
                isFailed = true
            )
        }
    }

    /** [text] as the tree it is, for a `tools` or `json_schema` written as a string. */
    fun tree(text: String): JsonNode = json.readTree(text)

    private fun read(reply: JsonNode): RawReply {
        val message = reply.at("/choices/0/message")
        val call = message.at("/tool_calls/0/function")

        return RawReply(
            text = message
                .path("content")
                .asText()
                .trim(),
            tool = call
                .path("name")
                .asText()
                .ifEmpty { null },
            arguments = call
                .path("arguments")
                .asText()
                .ifEmpty { null }
                ?.let { json.readTree(it) },
            calls = message.path("tool_calls").size(),
            promptTokens = reply.at("/usage/prompt_tokens").asInt(),
            cachedTokens = reply.at("/timings/cache_n").asInt(),
            writtenTokens = reply.at("/usage/completion_tokens").asInt(),
            millis = TIMES.sumOf { reply.at(it).asDouble() }.toLong(),
            isFailed = message.isMissingNode
        )
    }

    override fun close() {
        model.close()
    }

    companion object {
        /** The context the app gives the model today, all slots together. */
        const val CONTEXT_TOKENS = 16_384

        private val FILE = Path
            .of(System.getProperty("notekea.chat.models").orEmpty())
            .resolve("Qwen3.5-4B-Q4_K_M.gguf")

        private val TIMES = listOf("/timings/prompt_ms", "/timings/predicted_ms")
        private const val ALL_LAYERS = -1
        private const val ERRORS_ONLY = 1
        private const val SEED = 42
        private const val FAILURE_SHOWN = 120

        /** Whether the model's file is there: these measurements are skipped without it. */
        val isInstalled: Boolean get() = Files.isRegularFile(FILE)
    }
}

/**
 * What the model replied: its [text], or the first [tool] it called with its [arguments] (of [calls] in all), the
 * tokens it read ([cachedTokens] of them kept from the call before) and wrote, and the time the model says it took
 * (the clock's time counts a computer left alone falling asleep).
 */
data class RawReply(
    val text: String,
    val tool: String? = null,
    val arguments: JsonNode? = null,
    val calls: Int = 0,
    val promptTokens: Int = 0,
    val cachedTokens: Int = 0,
    val writtenTokens: Int = 0,
    val millis: Long = 0,
    val isFailed: Boolean = false
)
