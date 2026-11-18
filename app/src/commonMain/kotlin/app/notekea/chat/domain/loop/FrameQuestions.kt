package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.memory.domain.MemoryLine

/**
 * The frame's questions to the model (`docs/agent-frame.md`), each under a grammar that names only what it was
 * shown: how many things a task means, which one, which several. A 4B asked them all at once said "several" for
 * "Laura Castro's email" and listed every note it saw; asked one at a time, each is a choice it can make.
 */
internal class FrameQuestions(private val llmEngine: LlmEngine) {
    /** How many things [task] means, and its own words for them; from the task and the memory, no notes shown. */
    suspend fun countOf(
        task: String,
        memory: List<MemoryLine>
    ): Pair<FrameCount, String>? {
        val counts = FrameCount.entries.joinToString(separator = ",") { "\"${it.written}\"" }

        val fields = asked(
            system = FramePrompts.COUNT,
            user = "${remembered(memory = memory)}Request: $task",
            schema = "{\"type\":\"object\",\"properties\":{\"$COUNT\":{\"enum\":[$counts]}," +
                "\"$WORDS\":{\"type\":\"string\"}},\"required\":[\"$COUNT\",\"$WORDS\"]}",
            tokens = COUNT_TOKENS
        ) ?: return null

        val count = FrameCount.entries.firstOrNull {
            it.written == fields[COUNT]
        } ?: return null

        return count to fields[WORDS].orEmpty()
    }

    /**
     * The one note [task] means and the entry in it, or every note (or entry) its words fit equally when nothing
     * says which; null when the reply cannot be read or names no note.
     */
    suspend fun oneOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): List<FrameTarget>? {
        val shown = FrameShown(candidates = candidates)

        val fields = asked(
            system = FramePrompts.ONE,
            user = "${remembered(memory = memory)}${shown.text}\n\nRequest: $task",
            schema = shown.oneSchema(),
            tokens = PICK_TOKENS
        ) ?: return null

        return shown.oneOf(fields = fields)
    }

    /** The notes [task] names, each of them; null when the reply cannot be read. */
    suspend fun severalOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): List<FrameTarget>? {
        val shown = FrameShown(candidates = candidates)

        val fields = asked(
            system = FramePrompts.SEVERAL,
            user = "${remembered(memory = memory)}${shown.text}\n\nRequest: $task",
            schema = shown.severalSchema(),
            tokens = PICK_TOKENS
        ) ?: return null

        return shown.severalOf(fields = fields)
    }

    private suspend fun asked(
        system: String,
        user: String,
        schema: String,
        tokens: Int
    ): Map<String, String>? {
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.SYSTEM,
                            text = system
                        ),
                        ChatMessage(
                            role = ChatRole.USER,
                            text = user
                        )
                    ),
                    options = GenerationOptions(
                        maxTokens = tokens,
                        temperature = 0f,
                        jsonSchema = schema
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        return ToolCallJson.fields(json = json.toString())
    }

    private fun remembered(memory: List<MemoryLine>): String = memory
        .takeIf { it.isNotEmpty() }
        ?.joinToString(
            separator = "\n",
            prefix = "What the person's memory says:\n",
            postfix = "\n\n"
        ) { "- ${it.text}" }
        .orEmpty()

    private companion object {
        const val COUNT = "count"
        const val WORDS = "words"
        const val COUNT_TOKENS = 60
        const val PICK_TOKENS = 120
    }
}
