package app.notekea.core.llm.domain

/** Which of the catalog's three chat models is in use, as the chat names it under its question field. */
enum class ModelTier {
    SMALL,
    MEDIUM,
    LARGE
}
