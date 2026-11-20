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
 * a task means; which notes of a set, or entries of a note, it could be about, each marked; which note; which
 * several. Asked to pick one, the 4B never said "unclear": so it marks each one, and the code counts.
 */
internal class FrameQuestions(private val llmEngine: LlmEngine) {
    /** How many things [task] means, its own words for them, and whether the thing is an item inside a note. */
    suspend fun countOf(
        task: String,
        memory: List<MemoryLine>
    ): FrameCountReply? {
        val counts = FrameCount.entries.joinToString(separator = ",") { "\"${it.written}\"" }
        val things = "\"${FramePrompts.NOTE}\",\"${FramePrompts.ITEM}\""

        val does =
            listOf(FramePrompts.CHANGES, FramePrompts.ADDS, FramePrompts.NOTE, FramePrompts.ASKS)
                .joinToString(separator = ",") { "\"$it\"" }

        val fields = asked(
            system = FramePrompts.COUNT,
            user = "${remembered(memory = memory)}Request: $task",
            schema = "{\"type\":\"object\",\"properties\":{\"$COUNT\":{\"enum\":[$counts]}," +
                "\"$WORDS\":{\"type\":\"string\"},\"$THING\":{\"enum\":[$things]},\"$DOES\":{\"enum\":[$does]}}," +
                "\"required\":[\"$COUNT\",\"$WORDS\",\"$THING\",\"$DOES\"]}"
        ) ?: return null

        val count = FrameCount.entries.firstOrNull {
            it.written == fields[COUNT]
        } ?: return null

        return FrameCountReply(
            count = count,
            words = fields[WORDS].orEmpty(),
            // The thing alone made adds, renames and "both" items (the frame alone, 2026-11-20): only a change is.
            isItem = fields[THING] == FramePrompts.ITEM && fields[DOES] == FramePrompts.CHANGES
        )
    }

    /**
     * The notes of [group] [task] could be about, each marked yes or no by the model and counted by the code
     * (`docs/loop-goal-plan.md`, step 2.2); null when the reply cannot be read.
     */
    suspend fun markedNotesOf(
        task: String,
        group: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): List<FrameCandidates.Candidate>? {
        val shown = FrameShown(
            notes = group.take(n = MOST_MARKED),
            extra = emptyList()
        )

        val fields = marked(FramePrompts.MARK_NOTES, task, memory, shown) ?: return null

        return shown.markedNotesOf(fields = fields)
    }

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

    /** The entries of [note] [task] could be about, each marked yes or no; null when the reply cannot be read. */
    suspend fun markedEntriesOf(
        task: String,
        note: FrameCandidates.Candidate,
        memory: List<MemoryLine>
    ): List<String>? {
        val shown = FrameShown(
            notes = listOf(note),
            extra = emptyList(),
            entries = note.entries.take(n = MOST_MARKED)
        )

        val fields = marked(FramePrompts.MARK_ENTRIES, task, memory, shown) ?: return null

        return shown.markedEntriesOf(fields = fields)
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

    // A mark is about eight tokens of JSON: room for every one shown, so the reply is never cut short.
    private suspend fun marked(
        system: String,
        task: String,
        memory: List<MemoryLine>,
        shown: FrameShown
    ): Map<String, String>? = asked(
        system = system,
        user = "${remembered(memory = memory)}${shown.text}\n\nRequest: $task",
        schema = shown.marksSchema(),
        tokens = TOKENS + MARK_TOKENS * MOST_MARKED
    )

    private suspend fun asked(
        system: String,
        user: String,
        schema: String,
        tokens: Int = TOKENS
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
        const val THING = "thing"
        const val DOES = "does"
        const val TOKENS = 100
        const val MARK_TOKENS = 10
        const val MOST_MARKED = 12
    }
}
