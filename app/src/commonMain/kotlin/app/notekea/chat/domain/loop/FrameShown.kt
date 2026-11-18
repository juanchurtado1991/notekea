package app.notekea.chat.domain.loop

/**
 * The candidates as the frame's model is shown them, by number (N1, N2…, and E1, E2… for the entries inside them),
 * the grammars that let it name only those, and its picks read back as targets (`docs/agent-frame.md`).
 */
internal class FrameShown(private val candidates: List<FrameCandidates.Candidate>) {
    private val entries = candidates.flatMap { candidate ->
        candidate.entries.map {
            candidate to it
        }
    }

    val text: String = "Notes:\n" + candidates
        .mapIndexed { at, candidate ->
            val inside = entries
                .withIndex()
                .filter {
                    it.value.first == candidate
                }
                .joinToString(separator = "") { "\n    $ENTRY_ID${it.index + 1} ${it.value.second}" }

            val gist = candidate
                .gist
                .takeIf { it.isNotEmpty() }
                ?.let { " — $it" }
                .orEmpty()

            "$NOTE_ID${at + 1} ${candidate.path.value}$gist$inside"
        }
        .joinToString(separator = "\n")

    fun oneSchema(): String {
        val notes = idsOf(
            prefix = NOTE_ID,
            size = candidates.size
        )

        val note =
            "\"$NOTE\":{\"enum\":[$notes,\"$UNCLEAR\"]},\"$FIT\":{\"type\":\"array\",\"items\":{\"enum\":[$notes]}}"

        // An enum of nothing is no grammar: with no entries shown, the entry fields are not asked for.
        if (entries.isEmpty()) return "{\"type\":\"object\",\"properties\":{$note},\"required\":[\"$NOTE\",\"$FIT\"]}"

        val ids = idsOf(
            prefix = ENTRY_ID,
            size = entries.size
        )

        val entry = ",\"$ENTRY\":{\"enum\":[$ids,\"$NONE\",\"$UNCLEAR\"]}," +
            "\"$FIT_ENTRIES\":{\"type\":\"array\",\"items\":{\"enum\":[$ids]}}"

        return "{\"type\":\"object\",\"properties\":{$note$entry}," +
            "\"required\":[\"$NOTE\",\"$FIT\",\"$ENTRY\",\"$FIT_ENTRIES\"]}"
    }

    fun severalSchema(): String {
        val notes = idsOf(
            prefix = NOTE_ID,
            size = candidates.size
        )

        return "{\"type\":\"object\",\"properties\":{\"$NOTES\":{\"type\":\"array\",\"items\":{\"enum\":[$notes]}}}," +
            "\"required\":[\"$NOTES\"]}"
    }

    /** The note picked, with its entry; or every note its words fit, when the model says it is unclear. */
    fun oneOf(fields: Map<String, String>): List<FrameTarget>? {
        val picked = fields[NOTE].orEmpty()

        if (picked == UNCLEAR) {
            val fit = notesOf(written = fields[FIT])
            if (fit.isEmpty()) return null

            return fit.map { FrameTarget(note = it.path) }
        }

        val note = noteOf(id = picked) ?: return null

        val inside = when (val entry = fields[ENTRY].orEmpty()) {
            UNCLEAR -> entriesOf(written = fields[FIT_ENTRIES]).filter {
                it.first == note
            }

            else -> listOfNotNull(entryOf(id = entry)).filter {
                it.first == note
            }
        }

        return listOf(
            FrameTarget(
                note = note.path,
                entries = inside.map { it.second }
            )
        )
    }

    fun severalOf(fields: Map<String, String>): List<FrameTarget> =
        notesOf(written = fields[NOTES]).map { FrameTarget(note = it.path) }

    private fun noteOf(id: String): FrameCandidates.Candidate? = indexOf(
        id = id,
        prefix = NOTE_ID
    )
        ?.let { candidates.getOrNull(index = it) }

    private fun entryOf(id: String): Pair<FrameCandidates.Candidate, String>? = indexOf(
        id = id,
        prefix = ENTRY_ID
    )
        ?.let { entries.getOrNull(index = it) }

    private fun notesOf(written: String?): List<FrameCandidates.Candidate> = written
        .orEmpty()
        .lines()
        .mapNotNull { noteOf(id = it.trim()) }
        .distinct()

    private fun entriesOf(written: String?): List<Pair<FrameCandidates.Candidate, String>> = written
        .orEmpty()
        .lines()
        .mapNotNull { entryOf(id = it.trim()) }
        .distinct()

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
    ): String = (1..size).joinToString(separator = ",") { "\"$prefix$it\"" }

    private companion object {
        const val NOTE = "note"
        const val NOTES = "notes"
        const val FIT = "fit"
        const val ENTRY = "entry"
        const val FIT_ENTRIES = "fit_entries"
        const val UNCLEAR = "unclear"
        const val NONE = "none"
        const val NOTE_ID = "N"
        const val ENTRY_ID = "E"
    }
}
