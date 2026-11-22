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

        /**
         * The section [line] is the `##` heading of, [OTHER] for one the app does not know; null when it is none.
         * The one reading of a heading: read with any space after `##` and written to with exactly one,
         * "##  Sources" got a second section (the code review of 2026-11-22).
         */
        fun ofHeadingLine(line: String): MemorySection? = HEADING
            .matchEntire(input = line.trim())
            ?.let { of(heading = it.groupValues[1]) }

        private val HEADING = Regex(pattern = "^##\\s+(.+)$")
    }
}
