package app.notekea.chat.domain.plan

import app.notekea.chat.domain.prompt.WritePrompt
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * The body of a note written from other notes (3.9), and from the documents they attach: read whole when they fit,
 * else part by part into digests first, so a long contract is read to its end. With no text to read it writes
 * nothing ([ActionProblem.NothingToRead]), and a body of placeholders is written again, then refused.
 */
internal class NotesWriter(
    private val llmEngine: LlmEngine,
    noteTextCache: NoteTextCache
) {
    private val attachedTexts = AttachedTexts(noteTextCache = noteTextCache)

    /** The body for [step] and how many notes it was written from. */
    suspend fun bodyOf(
        step: ActionStep,
        request: String,
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): String {
        val known = step.notes.filter { path ->
            notes.any {
                it.file.path == path
            }
        }

        val read = known.map { path ->
            path.nameWithoutExtension to attachedTexts.of(
                vaultPath = vaultPath,
                note = notes.first {
                    it.file.path == path
                },
                notes = notes
            )
        }

        val noNotes = ActionProblem.Missing(step.kind, ActionField.NOTES)
        if (read.isEmpty()) throw ComposeProblem(problem = noNotes)

        // A document named beside the note that attaches it is read once, with that note.
        val withNotes = read.flatMapTo(destination = HashSet()) { it.second.attached }

        val sources = known
            .zip(other = read)
            .filter { (path, _) ->
                path.name !in withNotes
            }
            .map { (_, named) ->
                named.first to named.second.text
            }
            .filter { it.second.isNotBlank() }

        if (sources.isEmpty()) {
            val names = read.flatMap { (name, note) ->
                note.unread.ifEmpty { listOf(name) }
            }

            throw ComposeProblem(problem = ActionProblem.NothingToRead(names = names))
        }

        val chars = sources.sumOf { it.second.length }

        val given = if (chars > READ_AT_ONCE_CHARS) {
            digested(
                request = request,
                sources = sources
            )
        } else {
            sources
        }

        val words = WritePrompt.wordsFor(
            notes = sources.size,
            chars = chars
        )

        val messages = WritePrompt.fromNotes(
            request = request,
            what = step.text.orEmpty(),
            sources = given,
            words = words
        )

        // The answer's room is what the context has left after what it reads: asked for more, the model fails.
        val givenTokens = given.sumOf { it.second.length } / CHARS_PER_TOKEN

        val room = (CONTEXT_TOKENS - PROMPT_TOKENS - givenTokens)
            .coerceAtLeast(minimumValue = MIN_OUTPUT_TOKENS)

        val maxTokens = (words * TOKENS_PER_WORD).coerceAtMost(maximumValue = room)

        val first = written(
            messages = messages,
            maxTokens = maxTokens
        )

        // Its last step, as a person rereads their work: no placeholders, and no figure the notes do not hold.
        val wrong = problemsOf(
            text = first,
            sources = sources
        ) ?: return whole(text = first)

        val again = written(
            messages = messages + WritePrompt.again(problem = wrong),
            maxTokens = maxTokens
        )

        if (Placeholders.isIn(text = again)) throw ComposeProblem(problem = ActionProblem.NotBacked)

        return whole(text = again)
    }

    // What is wrong with a body, as the model is told before it writes it again; null when nothing is.
    private fun problemsOf(
        text: String,
        sources: List<Pair<String, String>>
    ): String? {
        val figures = BackedFigures.missingIn(
            text = text,
            sources = sources.map { it.second }
        )

        return when {
            Placeholders.isIn(text = text) -> WritePrompt.PLACEHOLDERS
            figures.isNotEmpty() -> WritePrompt.FIGURES + figures.joinToString()
            else -> null
        }
    }

    // A table's last row cut where the model ran out of room is left out: half a row breaks the table.
    private fun whole(text: String): String {
        val last = text.substringAfterLast(delimiter = '\n').trim()
        val isCutRow = last.startsWith(prefix = "|") && !last.endsWith(suffix = "|")

        return if (isCutRow) text.substringBeforeLast(delimiter = '\n').trimEnd() else text
    }

    /** Each source read part by part into what matters for the request, when together they are too long to read. */
    private suspend fun digested(
        request: String,
        sources: List<Pair<String, String>>
    ): List<Pair<String, String>> = coroutineScope {
        val parts = (MAX_PARTS / sources.size).coerceAtLeast(minimumValue = 1)

        // The digests are read together afterwards: each gets its share of that room, so 30 notes still fit.
        val count = sources.sumOf { (_, text) ->
            ((text.length + DIGEST_CHARS - 1) / DIGEST_CHARS).coerceIn(1, parts)
        }

        val digestTokens = (DIGESTS_TOKENS / count).coerceIn(MIN_DIGEST_TOKENS, DIGEST_TOKENS)

        sources.map { (name, text) ->
            async {
                val digests = text
                    .chunked(size = DIGEST_CHARS)
                    .take(n = parts)
                    .map { part ->
                        written(
                            messages = WritePrompt.digest(
                                request = request,
                                name = name,
                                text = part
                            ),
                            maxTokens = digestTokens
                        )
                    }

                name to digests.joinToString(separator = "\n")
            }
        }.awaitAll()
    }

    private suspend fun written(
        messages: List<ChatMessage>,
        maxTokens: Int
    ): String {
        val text = StringBuilder()

        llmEngine
            .generate(
                messages = messages,
                options = GenerationOptions(
                    maxTokens = maxTokens,
                    temperature = TEMPERATURE
                )
            )
            .collect { text.append(it) }

        return text.toString().trim()
    }

    private companion object {
        const val TEMPERATURE = 0.3f
        const val TOKENS_PER_WORD = 3
        const val DIGEST_TOKENS = 220
        const val MIN_DIGEST_TOKENS = 60

        // What the digests may take together, about what is read at once.
        const val DIGESTS_TOKENS = 2_200

        // The model's context (`ModelSetup.CONTEXT_TOKENS`), the instructions' share of it, and Spanish's and
        // English's characters a token, low on purpose so the room is not overestimated.
        const val CONTEXT_TOKENS = 4_096
        const val PROMPT_TOKENS = 500
        const val CHARS_PER_TOKEN = 3
        const val MIN_OUTPUT_TOKENS = 300

        // About 2,500 tokens of notes, which leaves room for the instructions and the answer in a 4,096 context.
        const val READ_AT_ONCE_CHARS = 9_000
        const val DIGEST_CHARS = 6_000

        // The digests together still fit in what is read at once: ten parts are 60,000 characters of notes.
        const val MAX_PARTS = 10
    }
}
