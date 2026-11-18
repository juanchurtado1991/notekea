package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.memory.domain.MemoryLine

/**
 * The frame's questions to the model (`docs/agent-frame.md`), each a small choice under a grammar: how many things
 * a task means; whether it says which of a set of namesakes; which note; which entry; which several. Asked to judge
 * all at once, the 4B said "several" for "Laura Castro's email"; asked to pick one, it never said "unclear".
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
                "\"$WORDS\":{\"type\":\"string\"}},\"required\":[\"$COUNT\",\"$WORDS\"]}"
        ) ?: return null

        val count = FrameCount.entries.firstOrNull {
            it.written == fields[COUNT]
        } ?: return null

        return count to fields[WORDS].orEmpty()
    }

    /** The one of [group] that [task] or the memory says it means; null when nothing says which. */
    suspend fun saidOf(
        task: String,
        group: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): FrameCandidates.Candidate? = picked(
        system = FramePrompts.SAID,
        task = task,
        memory = memory,
        shown = FrameShown(
            notes = group,
            extra = listOf(FramePrompts.NOT_SAID)
        )
    )?.let { (shown, fields) -> shown.noteOf(fields = fields) }

    /** The shown note [task] is about; null when none is. */
    suspend fun pickOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): FrameCandidates.Candidate? = picked(
        system = FramePrompts.PICK,
        task = task,
        memory = memory,
        shown = FrameShown(
            notes = candidates,
            extra = listOf(FramePrompts.NOT_SAID)
        )
    )?.let { (shown, fields) -> shown.noteOf(fields = fields) }

    /** The entries of [note] [task] changes: the one it says, all when it does not say which, none for the note. */
    suspend fun entriesOf(
        task: String,
        note: FrameCandidates.Candidate,
        memory: List<MemoryLine>
    ): List<String> {
        val shown = FrameShown(
            notes = listOf(note),
            extra = listOf(FramePrompts.NOT_SAID, FramePrompts.WHOLE),
            entries = note.entries
        )

        val (_, fields) = picked(FramePrompts.ENTRY, task, memory, shown) ?: return emptyList()

        return when (shown.extraOf(fields = fields)) {
            FramePrompts.NOT_SAID -> note.entries
            FramePrompts.WHOLE -> emptyList()
            else -> listOfNotNull(shown.entryOf(fields = fields))
        }
    }

    /** The notes [task] names, each of them; null when the reply cannot be read. */
    suspend fun severalOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): List<FrameCandidates.Candidate>? {
        val shown = FrameShown(
            notes = candidates,
            extra = emptyList()
        )

        val fields = asked(
            system = FramePrompts.SEVERAL,
            user = "${remembered(memory = memory)}${shown.text}\n\nRequest: $task",
            schema = shown.listSchema()
        ) ?: return null

        return shown.notesOf(fields = fields)
    }

    private suspend fun picked(
        system: String,
        task: String,
        memory: List<MemoryLine>,
        shown: FrameShown
    ): Pair<FrameShown, Map<String, String>>? = asked(
        system = system,
        user = "${remembered(memory = memory)}${shown.text}\n\nRequest: $task",
        schema = shown.schema()
    )?.let {
        shown to it
    }

    private suspend fun asked(
        system: String,
        user: String,
        schema: String
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
                        maxTokens = TOKENS,
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
        const val TOKENS = 100
    }
}
