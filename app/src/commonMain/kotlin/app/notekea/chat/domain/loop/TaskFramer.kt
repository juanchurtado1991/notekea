package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.memory.domain.MemoryLine

/**
 * The first step of a turn on notes (`docs/agent-frame.md`): shown the candidates and their entries, the model says
 * under a grammar, by number, which the task could mean and how many. Null with nothing to show or no reply read.
 */
class TaskFramer(private val llmEngine: LlmEngine) {
    internal suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): TaskFrame? {
        if (candidates.isEmpty()) return null

        val entries = candidates.flatMap { candidate ->
            candidate.entries.map {
                candidate to it
            }
        }

        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = messagesOf(task, candidates, entries, memory),
                    options = GenerationOptions(
                        maxTokens = FRAME_TOKENS,
                        temperature = 0f,
                        jsonSchema = schemaOf(
                            notes = candidates.size,
                            entries = entries.size
                        )
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        val fields = ToolCallJson.fields(json = json.toString()) ?: return null

        val notes = idsOf(
            prefix = NOTE_ID,
            written = fields[NOTES]
        ).mapNotNull { candidates.getOrNull(index = it) }

        val picked = idsOf(
            prefix = ENTRY_ID,
            written = fields[ENTRIES]
        ).mapNotNull { entries.getOrNull(index = it) }

        // An entry picked in a note not picked names that note too: the entry is what the task is about.
        val targets = (notes + picked.map { it.first })
            .distinct()
            .map { candidate ->
                FrameTarget(
                    note = candidate.path,
                    entries = picked.filter {
                        it.first == candidate
                    }.map { it.second }
                )
            }

        return TaskFrame(
            targets = targets,
            count = FrameCount.entries.firstOrNull {
                it.written == fields[COUNT]
            } ?: return null,
            candidates = candidates.map { it.path },
            words = fields[WORDS]
        )
    }

    private fun idsOf(
        prefix: String,
        written: String?
    ): List<Int> = written
        .orEmpty()
        .lines()
        .mapNotNull {
            it
                .trim()
                .removePrefix(prefix = prefix)
                .toIntOrNull()
                ?.minus(other = 1)
        }
        .distinct()

    private fun messagesOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        entries: List<Pair<FrameCandidates.Candidate, String>>,
        memory: List<MemoryLine>
    ): List<ChatMessage> {
        val shown = candidates.mapIndexed { at, candidate ->
            val inside = entries
                .withIndex()
                .filter {
                    it.value.first == candidate
                }
                .joinToString(separator = "") { "\n    $ENTRY_ID${it.index + 1} ${it.value.second}" }

            val gist = candidate
                .gist
                .takeIf { it.isNotEmpty() }
                ?.let { " — $it" }
                .orEmpty()

            "$NOTE_ID${at + 1} ${candidate.path.value}$gist$inside"
        }

        val remembered = memory
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "What the person's memory says:\n",
                postfix = "\n\n"
            ) { "- ${it.text}" }
            .orEmpty()

        return listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = RULES
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "${remembered}Notes:\n${shown.joinToString(separator = "\n")}\n\nRequest: $task"
            )
        )
    }

    private fun schemaOf(
        notes: Int,
        entries: Int
    ): String {
        val counts = FrameCount.entries.joinToString(separator = ",") { "\"${it.written}\"" }
        val noteIds = (1..notes).joinToString(separator = ",") { "\"$NOTE_ID$it\"" }
        val entryIds = (1..entries).joinToString(separator = ",") { "\"$ENTRY_ID$it\"" }

        // An enum of nothing is no grammar: with no entries shown, the field is not asked for.
        val entryField = if (entries == 0) {
            ""
        } else {
            ",\"$ENTRIES\":{\"type\":\"array\",\"items\":{\"enum\":[$entryIds]}}"
        }

        val entryRequired = if (entries == 0) "" else ",\"$ENTRIES\""

        return "{\"type\":\"object\",\"properties\":{" +
            "\"$NOTES\":{\"type\":\"array\",\"items\":{\"enum\":[$noteIds]}}$entryField," +
            "\"$COUNT\":{\"enum\":[$counts]},\"$WORDS\":{\"type\":\"string\"}}," +
            "\"required\":[\"$NOTES\"$entryRequired,\"$COUNT\",\"$WORDS\"]}"
    }

    private companion object {
        const val NOTES = "notes"
        const val ENTRIES = "entries"
        const val COUNT = "count"
        const val WORDS = "words"
        const val NOTE_ID = "N"
        const val ENTRY_ID = "E"
        const val FRAME_TOKENS = 160

        const val RULES =
            """You read a request made to a notes app and say what in the notes it is about, before any work is
done. You are shown the notes it may be about (N1, N2…) and, inside them, entries it may be about (E1, E2…: an
appointment, an order, a session, a row). Reply with JSON:
- notes: EVERY shown note the request could mean, not only the likeliest. Notes that share a name, people who
  share a first name, or several notes of the kind it names all fit, unless its words rule some out: a surname,
  a folder, a date, a word only one of them has. When the person's memory says which note the words mean, that
  one alone. Empty when it is about no shown note (a note to write from nothing, the app, the web).
- entries: when it is about an entry inside a note and not the whole note, EVERY shown entry it could mean, the
  same way: "the appointment" in a note of three appointments fits all three, "the 14:00 appointment" one.
  Empty when it is about whole notes.
- count: "one" when it means one thing (the appointment, Ana's phone, my budget, this note); "several" when it
  names more than one (both, my two budgets, Ana and Luis); "every" when it means every note or entry a condition
  picks out (all clients with an unpaid invoice, my recipes, the notes tagged x); "none" when it is about no
  particular note.
- words: the request's own words that name what it is about, copied as written ("the appointment", "Ana").
Never guess which one: list all that fit and the app asks the person."""
    }
}
