package app.notekea.core.memory.domain

/**
 * A line put into the memory's note or taken out of it (`docs/agent-memory.md`, "How a line is written"). A line
 * that says what a name already said means replaces it, so two lines never disagree.
 */
object MemoryEdit {
    const val TITLE = "# Memory"

    /** [note] with [line] under [section]'s heading, made when missing, its [origin] a hidden comment. */
    fun remembered(
        note: String,
        line: String,
        section: MemorySection,
        origin: String
    ): String {
        val written = "- ${line.trim()} <!-- $origin -->"

        val lines = note
            .ifBlank { "$TITLE\n" }
            .trimEnd()
            .lines()

        val clash = clashOf(
            lines = lines,
            new = line,
            section = section
        )

        if (clash != null) {
            return replacing(
                lines = lines,
                at = clash,
                written = written
            )
        }

        val heading = lines.indexOfFirst {
            it.trim().equals(
                other = "## ${section.heading}",
                ignoreCase = true
            )
        }

        if (heading < 0) return (lines + "" + "## ${section.heading}" + written).joined()

        val end = (heading + 1 until lines.size).firstOrNull {
            lines[it].trim().startsWith(prefix = "#")
        }
            ?: lines.size

        val last = (heading + 1 until end).lastOrNull { lines[it].isNotBlank() } ?: heading

        return (lines.take(n = last + 1) + written + lines.drop(n = last + 1)).joined()
    }

    /**
     * [note] with the line of [section] that starts with [prefix] made [value] (`"$prefix$value"`), or taken out when
     * [value] is blank: one of Settings' simple fields (the person's country, currency), written as a line of its own.
     */
    fun withField(
        note: String,
        section: MemorySection,
        prefix: String,
        value: String,
        origin: String
    ): String {
        val old = MemoryNote
            .linesOf(text = note)
            .firstOrNull {
                it.section == section && it.text.startsWith(prefix = prefix)
            }

        val cleared = old?.let {
            forgotten(
                note = note,
                line = it.text
            )
        } ?: note

        if (value.isBlank()) return cleared

        return remembered(
            note = cleared,
            line = prefix + value.trim(),
            section = section,
            origin = origin
        )
    }

    /** The value of the line of [section] that starts with [prefix] in [note]; empty when there is none. */
    fun fieldOf(
        note: String,
        section: MemorySection,
        prefix: String
    ): String = MemoryNote
        .linesOf(text = note)
        .firstOrNull {
            it.section == section && it.text.startsWith(prefix = prefix)
        }
        ?.text
        ?.removePrefix(prefix = prefix)
        .orEmpty()

    /** [note] without the line whose text is [line], its origin aside; [note] as it was when none is. */
    fun forgotten(
        note: String,
        line: String
    ): String {
        val lines = note.lines()

        val kept = lines.filterNot { written ->
            textOf(written = written)?.equals(
                other = line.trim(),
                ignoreCase = true
            ) == true
        }

        return if (kept.size == lines.size) note else kept.joined()
    }

    /**
     * The index of the line of [section] that [new] says again, or null. Only a line under the same `##` heading can
     * clash: a line of another section was overwritten when it happened to read like one of [section] (bug hunt S9).
     */
    private fun clashOf(
        lines: List<String>,
        new: String,
        section: MemorySection
    ): Int? {
        val sections = lines
            .runningFold(initial = MemorySection.OTHER) { current, written ->
                HEADING
                    .matchEntire(input = written.trim())
                    ?.let { MemorySection.of(heading = it.groupValues[1]) }
                    ?: current
            }
            .drop(n = 1)

        return lines.indices.firstOrNull {
            sections[it] == section && isClash(old = lines[it], new = new, section = section)
        }
    }

    // A name said again in People and names, whichever of a line's names it is, or the same site again in Sources.
    private fun isClash(
        old: String,
        new: String,
        section: MemorySection
    ): Boolean {
        val text = textOf(written = old) ?: return false

        return when (section) {
            MemorySection.PEOPLE -> {
                val before = namesOf(text = text)
                val after = namesOf(text = new)

                before.any {
                    it in after
                }
            }

            MemorySection.SOURCES -> {
                val before = DOMAIN.find(input = text)?.value
                val after = DOMAIN.find(input = new)?.value
                before != null && before.equals(other = after, ignoreCase = true)
            }

            else -> false
        }
    }

    private fun namesOf(text: String): Set<String> = MemoryLine(
        section = MemorySection.PEOPLE,
        text = text,
        origin = null
    )
        .names
        .map { it.said.lowercase() }
        .toSet()

    private fun textOf(written: String): String? = BULLET
        .matchEntire(input = written.trim())
        ?.groupValues
        ?.get(1)
        ?.replace(
            regex = ORIGIN,
            replacement = ""
        )
        ?.trim()

    private fun replacing(
        lines: List<String>,
        at: Int,
        written: String
    ): String = (lines.take(n = at) + written + lines.drop(n = at + 1)).joined()

    private fun List<String>.joined(): String = joinToString(separator = "\n") + "\n"

    private val BULLET = Regex(pattern = "^[-*]\\s+(.+)$")
    private val HEADING = Regex(pattern = "^##\\s+(.+)$")
    private val ORIGIN = Regex(pattern = "<!--(.*?)-->")
    private val DOMAIN = Regex(
        pattern = "[a-z0-9*][a-z0-9.-]*\\.[a-z]{2,}",
        option = RegexOption.IGNORE_CASE
    )
}
