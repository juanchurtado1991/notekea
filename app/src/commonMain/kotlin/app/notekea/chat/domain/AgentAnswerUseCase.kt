package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * A second try at a question the first search did not answer (the light agent of 4.7, asked for in M3): in up to
 * [MAX_MOVES] moves the model searches with other words or reads a whole note, then answers from what it gathered with
 * 3.2's prompt and its citations, or says the notes do not hold it.
 */
class AgentAnswerUseCase(
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val noteTextCache: NoteTextCache,
    private val llmEngine: LlmEngine,
    private val planReader: ActionPlanReader
) {
    operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        question: String,
        language: String? = null
    ): Flow<AnswerStep> = flow {
        val gathered = mutableListOf<AnswerSource>()
        val tried = mutableSetOf<String>()

        val names = namesFor(
            question = question,
            notes = notes
        )

        try {
            for (turn in 1..MAX_MOVES) {
                val move = nextMove(
                    question = question,
                    names = names,
                    gathered = gathered
                ) ?: break

                val key = "${move.move}:${move.query ?: move.note}".lowercase()

                val isDone =
                    move.move == AgentPrompt.ANSWER ||
                        move.move == AgentPrompt.GIVE_UP ||
                        key in tried

                if (isDone) break
                tried += key

                when (move.move) {
                    AgentPrompt.SEARCH -> {
                        emit(value = AnswerStep.Searching)

                        gathered += searched(
                            vaultPath = vaultPath,
                            notes = notes,
                            query = move.query.orEmpty()
                        )
                    }

                    AgentPrompt.READ -> {
                        val note = notes.firstOrNull {
                            it
                                .file
                                .path
                                .nameWithoutExtension
                                .equals(move.note, ignoreCase = true)
                        }

                        if (note != null) {
                            emit(value = AnswerStep.Reading(notes = 1))

                            gathered += read(
                                vaultPath = vaultPath,
                                note = note
                            )
                        }
                    }
                }
            }
        } catch (exception: LlmException) {
            return@flow emit(AnswerStep.Failed(ChatError.Model(exception.error)))
        }

        val read = gathered
            .distinctBy {
                it.note to it.range
            }
            .take(n = MAX_EXCERPTS)
            .mapIndexed { index, source -> source.copy(number = index + 1) }

        if (read.isEmpty()) return@flow emit(value = AnswerStep.NotFound)

        emitAll(
            answered(
                question = question,
                read = read,
                language = language
            )
        )
    }

    /** The names that share the question's words first, so in a large vault the agent sees the ones it may read. */
    private fun namesFor(
        question: String,
        notes: Collection<NoteEntry>
    ): List<String> {
        val words = WORD
            .findAll(input = question.lowercase())
            .map { it.value }
            .filter {
                it.length > 2
            }
            .toSet()

        return notes
            .map { it.file.path }
            .sortedByDescending { path ->
                words.count {
                    it in path.value.lowercase()
                }
            }
            .map { it.nameWithoutExtension }
    }

    private suspend fun nextMove(
        question: String,
        names: List<String>,
        gathered: List<AnswerSource>
    ): AgentMove? {
        val found =
            gathered.map { "${it.note.nameWithoutExtension}: ${it.text.take(n = FOUND_CHARS)}" }

        val json = StringBuilder()

        llmEngine
            .generate(
                messages = AgentPrompt.messages(
                    question = question,
                    notes = names.take(n = MAX_NAMES),
                    found = found
                ),
                options = GenerationOptions(
                    maxTokens = MOVE_TOKENS,
                    temperature = 0f,
                    jsonSchema = AgentPrompt.SCHEMA
                )
            )
            .collect { json.append(it) }

        return planReader.readMove(json = json.toString())
    }

    private suspend fun searched(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        query: String
    ): List<AnswerSource> = when (val found = answerFromNotes.excerpts(vaultPath, notes, query)) {
        is Outcome.Success -> found.value.take(n = PER_SEARCH)
        is Outcome.Failure -> emptyList()
    }

    private suspend fun read(
        vaultPath: String,
        note: NoteEntry
    ): List<AnswerSource> {
        val body = noteTextCache.textsOf(
            vaultPath = vaultPath,
            notes = listOf(note)
        )[note.file.path]?.body
            ?: return emptyList()

        val shown = body.take(n = READ_CHARS)

        return listOf(
            AnswerSource(
                number = 0,
                note = note.file.path,
                headingPath = emptyList(),
                range = shown.indices,
                text = shown
            )
        )
    }

    /** The answer from what was gathered, with 3.2's prompt: cited, or not found. */
    private fun answered(
        question: String,
        read: List<AnswerSource>,
        language: String?
    ): Flow<AnswerStep> = flow {
        val written = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = AnswerPrompt
                        .messages(
                            question = question,
                            excerpts = read.map { it.text }
                        )
                        .inLanguage(
                            language = language,
                            kept = AnswerPrompt.NOT_FOUND
                        ),
                    options = GenerationOptions(maxTokens = ANSWER_TOKENS)
                )
                .collect { written.append(it) }
        } catch (exception: LlmException) {
            return@flow emit(AnswerStep.Failed(ChatError.Model(exception.error)))
        }

        val text = written.toString().trim()
        val isNotFound = AnswerPrompt.isNotFound(text = text)
        if (isNotFound) return@flow emit(value = AnswerStep.NotFound)

        emit(
            value = AnswerStep.Done(
                answer = AnswerCitations.of(
                    text = text,
                    read = read
                )
            )
        )
    }

    private companion object {
        const val MAX_MOVES = 3
        val WORD = Regex(pattern = "[\\p{L}\\p{N}]+")
        const val MAX_EXCERPTS = 6
        const val MAX_NAMES = 80
        const val PER_SEARCH = 3
        const val FOUND_CHARS = 160
        const val READ_CHARS = 3_000
        const val MOVE_TOKENS = 80
        const val ANSWER_TOKENS = 200
    }
}
