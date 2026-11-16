package app.notekea.core.memory.domain

/**
 * The simple fields of Settings → Chat memory (`docs/agent-memory.md`, "How the person sets it up"): each one line of
 * the memory under its [section], starting with its [prefix], so the chat reads it as it reads every other line.
 */
enum class MemoryField(
    val section: MemorySection,
    val prefix: String
) {
    COUNTRY(
        section = MemorySection.ABOUT,
        prefix = "I live in "
    ),
    CURRENCY(
        section = MemorySection.ANSWERS,
        prefix = "Money in "
    ),
    LANGUAGE(
        section = MemorySection.ANSWERS,
        prefix = "Answer in "
    ),
    NEW_NOTES(
        section = MemorySection.PLACES,
        prefix = "New notes go in the folder "
    )
}
