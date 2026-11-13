package app.notekea.chat.presentation.model

/** Whether the chat can answer now, as its panel shows it. */
enum class ModelStatus {
    CHECKING,
    READY,

    /** No chat model is downloaded and chosen: the panel offers Settings → AI model. */
    NO_MODEL,

    /** The model chosen needs more memory than this computer has. */
    NOT_ENOUGH_MEMORY,

    /** The model or its native library failed to load. */
    FAILED
}
