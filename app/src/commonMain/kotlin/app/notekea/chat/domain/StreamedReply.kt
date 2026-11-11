package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** A reply the model writes from [messages], shown as it comes, citing nothing: help, the vault's overview. */
internal fun LlmEngine.streamedReply(
    messages: List<ChatMessage>,
    options: GenerationOptions
): Flow<AnswerStep> = flow {
    val written = StringBuilder()

    try {
        generate(
            messages = messages,
            options = options
        ).collect { piece ->
            written.append(piece)

            emit(
                value = AnswerStep.Writing(
                    text = written.toString().trim(),
                    read = emptyList()
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
                sources = emptyList(),
                read = emptyList()
            )
        )
    )
}
