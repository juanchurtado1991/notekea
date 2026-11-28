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
        note: String,
        marked: String,
        entries: List<String>
    ): List<String> = listOf(marked) + rivalsOf(task, note, marked, entries)

    /**
     * The entries of [entries], other than [marked], that write every task word [marked] writes besides the words of
     * [note]'s name; none when [marked] writes no other task word.
     */
    fun rivalsOf(
        task: String,
        note: String,
        marked: String,
        entries: List<String>
    ): List<String> {
        // The note's own name is what every entry is about: "el contrato de maíz" tells none of its entries apart
        // (the E4B's blind pool, seed 10, 2026-11-28).
        val named = FrameWords.of(text = note)

        val words = FrameWords
            .of(text = task)
            .filterNot { word ->
                named.any {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }
            }
            .toSet()

        val own = writtenOf(
            words = words,
            entry = marked
        )

        if (own.isEmpty()) return emptyList()

        return entries.filter { entry ->
            entry != marked && writtenOf(words = words, entry = entry).containsAll(elements = own)
        }
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
