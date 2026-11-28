package app.notekea.chat.domain.loop

/**
 * The entries of a note's text: its list items, tasks and table rows, each with the section it is in. A table's
 * heading row, the one above its rule, names the columns: it is no entry.
 */
internal object NoteEntries {
    /** A [line] as written, trimmed, with its [kind] and its section: the [heading] above it and which one it is. */
    data class Entry(
        val line: String,
        val kind: NoteEntryKind,
        val heading: String,
        val section: Int
    ) {
        /** The line as it is shown to the model and offered to the person. */
        val shown: String = line.take(n = SHOWN)
    }

    fun of(text: String): List<Entry> {
        val lines = text
            .lineSequence()
            .map { it.trim() }
            .toList()

        val sections = lines
            .runningFold(initial = Section()) { section, line ->
                if (line.startsWith(prefix = HEADING)) {
                    Section(
                        heading = line,
                        number = section.number + 1
                    )
                } else {
                    section
                }
            }
            .drop(n = 1)

        // The Gemma E4B marked "| Bill | Amount | Due | Paid |" as the water bill to mark paid (the frame alone,
        // 2026-11-24).
        return lines
            .zip(other = sections)
            .filterIndexed { index, (line, _) ->
                isEntry(line = line) && !isRule(line = lines.getOrNull(index = index + 1).orEmpty())
            }
            .map { (line, section) ->
                Entry(
                    line = line,
                    kind = kindOf(line = line),
                    heading = section.heading,
                    section = section.number
                )
            }
    }

    // A list item, a task, a numbered item or a table row that is not the table's rule.
    private fun isEntry(line: String): Boolean {
        val isRow = line.startsWith(prefix = ROW) && !isRule(line = line)

        return isRow || ITEM.containsMatchIn(input = line)
    }

    private fun isRule(line: String): Boolean =
        line.startsWith(prefix = ROW) && line.all { it in RULE }

    private fun kindOf(line: String): NoteEntryKind = when {
        line.startsWith(prefix = ROW) -> NoteEntryKind.ROW
        TASK.containsMatchIn(input = line) -> NoteEntryKind.TASK
        else -> NoteEntryKind.ITEM
    }

    private data class Section(
        val heading: String = "",
        val number: Int = 0
    )

    private val TASK = Regex(pattern = "^[-*+]\\s\\[.]\\s")
    private val ITEM = Regex(pattern = "^([-*+]\\s|\\d+[.)]\\s)")
    private const val HEADING = "#"
    private const val ROW = "|"
    private const val RULE = "|-: "
    private const val SHOWN = 140
}
