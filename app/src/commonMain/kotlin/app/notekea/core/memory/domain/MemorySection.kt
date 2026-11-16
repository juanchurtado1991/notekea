package app.notekea.core.memory.domain

/** The sections of the memory, by what their lines are for; a heading the app does not know is [OTHER]. */
enum class MemorySection(val heading: String) {
    PEOPLE(heading = "People and names"),
    PLACES(heading = "Where things go"),
    AREAS(heading = "Areas"),
    ANSWERS(heading = "How I like answers"),
    ABOUT(heading = "About me"),
    SOURCES(heading = "Sources"),
    OTHER(heading = "");

    companion object {
        fun of(heading: String): MemorySection = entries.firstOrNull {
            it != OTHER && it.heading.equals(
                other = heading.trim(),
                ignoreCase = true
            )
        } ?: OTHER
    }
}
