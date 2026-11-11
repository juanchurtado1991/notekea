package app.notekea.chat.domain

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
    operator fun invoke(
        message: String,
        history: List<ChatExchange>,
        vaultPath: String,
        note: NoteEntry?
    ): Flow<AnswerStep> = flow {
        val body = note
            ?.let {
                noteTextCache.textsOf(
                    vaultPath = vaultPath,
                    notes = listOf(it)
                )[it.file.path]?.body
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
                    messages = ConversePrompt.messages(
                        message = message,
                        history = history,
                        note = note?.let {
                            it.file.path.nameWithoutExtension to body
                        }
                    ),
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
