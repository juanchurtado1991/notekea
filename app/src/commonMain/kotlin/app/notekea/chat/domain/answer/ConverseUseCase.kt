package app.notekea.chat.domain.answer

import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.language.inLanguage
import app.notekea.chat.domain.plan.AttachedTexts
import app.notekea.chat.domain.prompt.ConversePrompt
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The chat talking, not answering from the notes: a greeting, thanks, an opinion or advice, with the chat so far
 * ([ConversePrompt]). About a [note], it reads the note first and shows it as the one source.
 */
class ConverseUseCase(
    private val llmEngine: LlmEngine,
    private val noteTextCache: NoteTextCache
) {
    private val attachedTexts = AttachedTexts(noteTextCache = noteTextCache)

    operator fun invoke(
        message: String,
        history: List<ChatExchange>,
        vaultPath: String,
        note: NoteEntry?,
        language: String? = null,
        today: String? = null,
        working: String? = null,
        notes: Collection<NoteEntry> = emptyList()
    ): Flow<AnswerStep> = flow {
        // With the documents it attaches: talk about a note that holds a pasted PDF is about the PDF's text.
        val body = note
            ?.let {
                attachedTexts.of(
                    vaultPath = vaultPath,
                    note = it,
                    notes = notes
                ).text
            }
            .orEmpty()

        val source = note?.let {
            AnswerSource(
                number = 1,
                note = it.file.path,
                headingPath = emptyList(),
                range = 0 until body.length.coerceAtMost(maximumValue = SNIPPET_CHARS),
                text = body.take(n = SNIPPET_CHARS)
            )
        }

        val read = listOfNotNull(source)
        val written = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = ConversePrompt
                        .messages(
                            message = message,
                            history = history,
                            note = note?.let {
                                it.file.path.nameWithoutExtension to body
                            },
                            today = today,
                            working = working
                        )
                        .inLanguage(language = language),
                    options = GenerationOptions(
                        maxTokens = MAX_TOKENS,
                        temperature = TEMPERATURE
                    )
                )
                .collect { piece ->
                    written.append(piece)

                    emit(
                        value = AnswerStep.Writing(
                            text = written.toString().trim(),
                            read = read
                        )
                    )
                }
        } catch (exception: LlmException) {
            return@flow emit(AnswerStep.Failed(ChatError.Model(exception.error)))
        }

        emit(
            value = AnswerStep.Done(
                answer = Answer(
                    text = written.toString().trim(),
                    sources = read,
                    read = read
                )
            )
        )
    }

    private companion object {
        const val MAX_TOKENS = 450
        const val TEMPERATURE = 0.6f
        const val SNIPPET_CHARS = 300
    }
}
