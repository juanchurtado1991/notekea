package app.notekea.chat.domain.loop

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.chat.domain.tool.ModelJson
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText

/**
 * The notes of a change to many, each marked by the model as one the task asks to change or not, by what it holds,
 * and those it does not ask taken out before the card. Asked to name the ones to drop, the Gemma E4B named the ones
 * it meant to keep and emptied its own proposal (dev, 2026-11-24): it marks each one, as the frame's questions do.
 */
internal class ProposalReview(
    private val llmEngine: LlmEngine,
    private val modelJson: ModelJson
) {
    /**
     * Takes out of the turn's changes the notes the task does not ask for; none when the marks say nothing. Whether
     * any was taken out.
     */
    suspend fun review(
        state: TurnState,
        found: List<String>
    ): Boolean {
        val notes = notesOf(state = state)
        if (notes.size !in MANY..MOST) return false

        val marks = marksOf(
            notes = notes,
            state = state,
            found = found
        ) ?: return false

        val unasked = notes.filterIndexed { at, _ ->
            marks["$ID${at + 1}"] == FramePrompts.NO
        }

        // Every note marked no says nothing of any: the changes go on the card, where the person sees them.
        val saysNothing = unasked.isEmpty() || unasked.size == notes.size
        if (saysNothing) return false

        val dropped = unasked.map { state.turn.noteNamed(name = it) }.toSet()

        state
            .turn
            .work
            .drop {
                state.turn.noteNamed(name = it) in dropped
            }

        return true
    }

    private suspend fun marksOf(
        notes: List<String>,
        state: TurnState,
        found: List<String>
    ): Map<String, String>? {
        val listed = notes
            .mapIndexed { at, name -> "$ID${at + 1} $name: ${heldBy(name = name, state = state)}" }
            .joinToString(separator = "\n")

        val ids = notes.indices.map { "\"$ID${it + 1}\"" }
        val mark = "{\"enum\":[\"${FramePrompts.YES}\",\"${FramePrompts.NO}\"]}"

        val json = try {
            ReplyText.of(
                llmEngine = llmEngine,
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.SYSTEM,
                        text = INSTRUCTIONS
                    ),
                    ChatMessage(
                        role = ChatRole.USER,
                        text = "What was found:\n${found.joinToString(separator = "\n\n")}\n\n" +
                            "Request: ${state.task}\n\nNotes:\n$listed"
                    )
                ),
                options = GenerationOptions(
                    maxTokens = TOKENS + MARK_TOKENS * notes.size,
                    temperature = 0f,
                    jsonSchema = "{\"type\":\"object\",\"properties\":{" +
                        ids.joinToString(separator = ",") { "$it:$mark" } +
                        "},\"required\":[${ids.joinToString(separator = ",")}]}"
                )
            )
        } catch (_: LlmException) {
            return null
        }

        return modelJson.fields(json = json)
    }

    // The notes the changes act on, each once, as they were written: a new note's sources are what it is written
    // from, and dropping them emptied the note (the loop's fifth run, eight notes on the 4B never written).
    private fun notesOf(state: TurnState): List<String> = state
        .turn
        .work
        .changes
        .filter {
            it.kind != ActionKind.WRITE_FROM_NOTES
        }
        .flatMap {
            it.notes + listOfNotNull(it[ActionField.NOTE])
        }
        .distinctBy {
            state.turn.noteNamed(name = it) ?: it
        }

    // Its properties, tags and open tasks, then what it says: what a condition of the task is checked on.
    private fun heldBy(
        name: String,
        state: TurnState
    ): String {
        val path = state.turn.noteNamed(name = name)

        val note = state
            .turn
            .notes
            .firstOrNull {
                it.file.path == path
            } ?: return "not in the vault"

        val properties = NoteProperties.of(note = note)

        val open = note
            .metadata
            .tasks
            .count {
                !it.isDone
            }

        val tasks = if (open > 0) listOf("$open open tasks") else emptyList()

        return (properties + tasks)
            .joinToString(separator = "; ")
            .ifEmpty { "no properties" }
    }

    private companion object {
        const val ID = "N"
        const val MANY = 2
        const val MOST = 40
        const val TOKENS = 40
        const val MARK_TOKENS = 10

        const val INSTRUCTIONS =
            """A person's request changes notes of their app. Each note below (N1, N2…) is one a change was prepared
for, with what it holds. Mark "yes" when the request asks to change that note, "no" when it does not: it does not meet
the request's condition, or it only lists or links the others (a reading list, a menu, a home page). Judge each note
alone. Reply with JSON, every note marked: {"N1":"yes","N2":"no"}"""
    }
}
