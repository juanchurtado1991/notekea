package app.notekea.core.llm.domain

/** Why the chat model could not answer. */
sealed interface LlmError {
    /** No chat model is in use: none was downloaded, or the one chosen is gone (Settings → AI model). */
    data object NoModel : LlmError

    /** The model in use does not fit this machine's memory now. */
    data object NotEnoughMemory : LlmError

    /** The model or its native library failed to load or to run. */
    data object EngineFailed : LlmError
}
