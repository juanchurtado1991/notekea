package app.notekea.chat.domain

import app.notekea.core.llm.domain.LlmError

/** Why a question got no answer. */
sealed interface ChatError {
    /** The chat model could not answer ([error]). */
    data class Model(val error: LlmError) : ChatError

    /** The notes could not be searched: the index or its embedding model failed. */
    data object Search : ChatError
}
