package app.notekea.core.memory.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * `Notekea/Memory.md` read into its lines (`docs/agent-memory.md`): a bullet under a `##` heading is a line, its
 * trailing HTML comment its origin. A line that reads as a permission is left out: the memory holds meanings and
 * preferences, never leave to act (layer 6).
 */
object MemoryNote {
    /** Where the memory is kept: beside the vault's instructions, in Notekea's own folder (2026-11-15). */
    val PATH = RelativePath(value = "Notekea/Memory.md")

    fun linesOf(text: String): List<MemoryLine> = text
        .lines()
        .map { it.trim() }
        .fold(
            initial = Read(
                section = MemorySection.OTHER,
                lines = emptyList()
            )
        ) { read, line ->
            val heading = MemorySection.ofHeadingLine(line = line)

            if (heading != null) {
                read.copy(section = heading)
            } else {
                val kept = BULLET
                    .matchEntire(input = line)
                    ?.groupValues
                    ?.get(1)
                    ?.let {
                        lineOf(
                            section = read.section,
                            written = it
                        )
                    }

                read.copy(lines = read.lines + listOfNotNull(kept))
            }
        }
        .lines

    // The section a line falls under, and the lines read so far.
    private data class Read(
        val section: MemorySection,
        val lines: List<MemoryLine>
    )

    /** Whether [text] asks for leave to act rather than saying what something means or how it is liked. */
    fun isPermission(text: String): Boolean = PERMISSION.containsMatchIn(input = text)

    private fun lineOf(
        section: MemorySection,
        written: String
    ): MemoryLine? {
        val origin = ORIGIN.find(input = written)

        val text = written
            .replace(
                regex = ORIGIN,
                replacement = ""
            )
            .trim()

        val isRead = text.isNotEmpty() && !isPermission(text = text)
        if (!isRead) return null

        return MemoryLine(
            section = section,
            text = text,
            origin = origin
                ?.groupValues
                ?.get(1)
                ?.trim()
        )
    }

    private val BULLET = Regex(pattern = "^[-*]\\s+(.+)$")
    private val ORIGIN = Regex(pattern = "<!--(.*?)-->")

    // Leave to skip the card, a question or a search: what only Settings may grant.
    private val PERMISSION = Regex(
        pattern = "without (asking|telling|confirm)|don'?t (ask|confirm|tell)|never ask|no (me )?preguntes|" +
            "sin (preguntar|avisar|confirmar)|nunca preguntes|skip the card|sin la tarjeta|automatically|" +
            "autom[aá]ticamente",
        option = RegexOption.IGNORE_CASE
    )
}
