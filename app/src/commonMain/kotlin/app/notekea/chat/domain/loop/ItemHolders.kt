package app.notekea.chat.domain.loop

/**
 * The notes that hold the item a change names, when no note is named for it (`docs/agent-frame.md`): those with an
 * entry that writes the most of the task's words for its thing. "Mark the why-us supplement as second draft" was
 * settled on one application of three that each list a supplement (the E4B's blind pool, seed 10, 2026-11-27).
 */
internal object ItemHolders {
    fun of(
        words: String,
        candidates: List<FrameCandidates.Candidate>
    ): List<FrameCandidates.Candidate> {
        val thing = FrameWords.of(text = words)
        if (thing.isEmpty()) return emptyList()

        val written = candidates.associateWith { candidate ->
            candidate
                .entries
                .maxOfOrNull { entry ->
                    writtenOf(
                        thing = thing,
                        entry = entry
                    )
                }
                ?: 0
        }

        val most = written.values.maxOrNull() ?: 0
        if (most == 0) return emptyList()

        return written
            .filterValues {
                it == most
            }
            .keys
            .toList()
    }

    // How many of the thing's words an entry writes.
    private fun writtenOf(
        thing: Set<String>,
        entry: String
    ): Int {
        val words = FrameWords.of(text = entry)

        return thing.count { word ->
            words.any {
                FrameWords.isSame(
                    one = it,
                    other = word
                )
            }
        }
    }
}
