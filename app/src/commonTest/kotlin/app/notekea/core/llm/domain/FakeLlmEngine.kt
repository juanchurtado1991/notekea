package app.notekea.core.llm.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The chat model in memory: [reply] gives each reply, written in [pieces] of the text; [scoreOf] scores a text for
 * the reranker. Every call is kept, and [failure] makes the next ones fail.
 */
class FakeLlmEngine : LlmEngine {
    val generated = mutableListOf<List<ChatMessage>>()
    val options = mutableListOf<GenerationOptions>()
    var reply: (List<ChatMessage>) -> String = { "" }
    var scoreOf: (query: String, text: String) -> Float = { _, _ -> 0f }
    var pieces = 4
    var failure: LlmError? = null

    override fun generate(
        messages: List<ChatMessage>,
        options: GenerationOptions
    ): Flow<String> = flow {
        generated += messages
        this@FakeLlmEngine.options += options

        failure?.let {
            throw LlmException(error = it)
        }

        val text = reply(messages)
        val size = (text.length / pieces).coerceAtLeast(minimumValue = 1)

        text.chunked(size = size).forEach { emit(value = it) }
    }

    override suspend fun rerank(
        query: String,
        texts: List<String>
    ): Outcome<List<Float>, LlmError> {
        failure?.let {
            return Outcome.Failure(error = it)
        }

        return Outcome.Success(value = texts.map { scoreOf(query, it) })
    }
}
