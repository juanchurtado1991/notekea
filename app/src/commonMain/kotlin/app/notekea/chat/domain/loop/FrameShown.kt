package app.notekea.chat.domain.loop

/**
 * What the frame's model is shown for one question, by number (N1, N2… for notes, E1, E2… for the entries of one),
 * the grammar that lets it name only those or one of [extra], or mark each yes or no, and its reply read back.
 */
internal class FrameShown(
    private val notes: List<FrameCandidates.Candidate>,
    private val extra: List<String>,
    private val entries: List<String> = emptyList()
) {
    /** The notes with their first line and the lines that write the task's words, or the one note's entries. */
    val text: String = if (entries.isEmpty()) notesText() else entriesText()

    fun schema(): String {
        val ids = if (entries.isEmpty()) {
            idsOf(
                prefix = NOTE_ID,
                size = notes.size
            )
        } else {
            idsOf(
                prefix = ENTRY_ID,
                size = entries.size
            )
        }

        val all = (ids + extra.map { "\"$it\"" }).joinToString(separator = ",")

        return "{\"type\":\"object\",\"properties\":{\"$PICK\":{\"enum\":[$all]}},\"required\":[\"$PICK\"]}"
    }

    /** One "yes" or "no" for every id shown, each required (`docs/loop-goal-plan.md`, step 2.2). */
    fun marksSchema(): String {
        val ids = if (entries.isEmpty()) {
            idsOf(
                prefix = NOTE_ID,
                size = notes.size
            )
        } else {
            idsOf(
                prefix = ENTRY_ID,
                size = entries.size
            )
        }

        val mark = "{\"enum\":[\"${FramePrompts.YES}\",\"${FramePrompts.NO}\"]}"
        val properties = ids.joinToString(separator = ",") { "$it:$mark" }

        return "{\"type\":\"object\",\"properties\":{$properties},\"required\":[${ids.joinToString(separator = ",")}]}"
    }

    /** The notes marked "yes"; null when a note shown has no mark, as a reply cut short has none. */
    fun markedNotesOf(fields: Map<String, String>): List<FrameCandidates.Candidate>? = markedOf(
        fields = fields,
        prefix = NOTE_ID,
        items = notes
    )

    /** The entries marked "yes"; null when an entry shown has no mark. */
    fun markedEntriesOf(fields: Map<String, String>): List<String>? = markedOf(
        fields = fields,
        prefix = ENTRY_ID,
        items = entries
    )

    /** Several notes, as a list: every one the task names. */
    fun listSchema(): String {
        val ids = idsOf(
            prefix = NOTE_ID,
            size = notes.size
        ).joinToString(separator = ",")

        return "{\"type\":\"object\",\"properties\":{\"$PICKS\":{\"type\":\"array\",\"items\":{\"enum\":[$ids]}}}," +
            "\"required\":[\"$PICKS\"]}"
    }

    /** The note picked; null for one of [extra] or a reply that names none. */
    fun noteOf(fields: Map<String, String>): FrameCandidates.Candidate? = indexOf(
        id = fields[PICK].orEmpty(),
        prefix = NOTE_ID
    )?.let { notes.getOrNull(index = it) }

    fun notesOf(fields: Map<String, String>): List<FrameCandidates.Candidate> = fields[PICKS]
        .orEmpty()
        .lines()
        .mapNotNull { id ->
            indexOf(
                id = id.trim(),
                prefix = NOTE_ID
            )?.let { notes.getOrNull(index = it) }
        }
        .distinct()

    private fun <T> markedOf(
        fields: Map<String, String>,
        prefix: String,
        items: List<T>
    ): List<T>? {
        val marks = items.indices.map {
            fields["$prefix${it + 1}"]
        }

        if (marks.any { it != FramePrompts.YES && it != FramePrompts.NO }) return null

        return items.filterIndexed { at, _ ->
            marks[at] == FramePrompts.YES
        }
    }

    private fun notesText(): String = "Notes:\n" + notes
        .mapIndexed { at, note ->
            val gist = note
                .gist
                .takeIf { it.isNotEmpty() }
                ?.let { " — $it" }
                .orEmpty()

            val lines = note.lines.joinToString(separator = "") { "\n    | $it" }

            "$NOTE_ID${at + 1} ${note.path.value}$gist$lines"
        }
        .joinToString(separator = "\n")

    private fun entriesText(): String = "Note: ${notes.single().path.value}\nEntries:\n" + entries
        .mapIndexed { at, entry -> "$ENTRY_ID${at + 1} $entry" }
        .joinToString(separator = "\n")

    private fun indexOf(
        id: String,
        prefix: String
    ): Int? = id
        .takeIf { it.startsWith(prefix = prefix) }
        ?.removePrefix(prefix = prefix)
        ?.toIntOrNull()
        ?.minus(other = 1)

    private fun idsOf(
        prefix: String,
        size: Int
    ): List<String> = (1..size).map { "\"$prefix$it\"" }

    private companion object {
        const val PICK = "pick"
        const val PICKS = "picks"
        const val NOTE_ID = "N"
        const val ENTRY_ID = "E"
    }
}
