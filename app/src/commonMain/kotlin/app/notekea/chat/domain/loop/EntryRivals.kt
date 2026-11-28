package app.notekea.chat.domain.loop

/**
 * The entries of a note the task's own words cannot tell from the one marked: each writes every task word that
 * entry writes. The 12B's blind pool, seed 10, 2026-11-28: "move my meeting with Ms. Lee" was settled on the one
 * meeting marked, of several, on 12 of its 23 changes proposed where a question was due.
 */
internal object EntryRivals {
    /** [marked] with the rivals [entries] hold; [marked] alone when it writes a task word none of them does. */
    fun of(
        task: String,
        marked: String,
        entries: List<String>
    ): List<String> {
        val words = FrameWords.of(text = task)

        val own = writtenOf(
            words = words,
            entry = marked
        )

        if (own.isEmpty()) return listOf(marked)

        val rivals = entries.filter { entry ->
            entry != marked && writtenOf(words = words, entry = entry).containsAll(elements = own)
        }

        return listOf(marked) + rivals
    }

    // The task words [entry] writes.
    private fun writtenOf(
        words: Set<String>,
        entry: String
    ): Set<String> {
        val written = FrameWords.of(text = entry)

        return words
            .filterTo(destination = mutableSetOf()) { word ->
                written.any {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }
            }
    }
}
