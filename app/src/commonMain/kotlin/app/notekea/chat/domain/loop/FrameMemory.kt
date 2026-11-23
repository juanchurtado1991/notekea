package app.notekea.chat.domain.loop

import app.notekea.core.memory.domain.MemoryLine

/**
 * The note of a set the person's memory says the task's words mean (`docs/agent-memory.md`): "Laura" is
 * [[Laura Castro]] settles the two Lauras by the code, before the model is asked. Null when no line names one of it.
 */
internal object FrameMemory {
    fun settledOf(
        task: String,
        group: List<FrameCandidates.Candidate>,
        memory: List<MemoryLine>
    ): FrameCandidates.Candidate? {
        val said = task.lowercase()

        val meant = memory
            .flatMap { it.names }
            .filter { named -> wordsOf(named.said).containsMatchIn(input = said) }
            .flatMap { named ->
                group.filter {
                    isNamed(
                        candidate = it,
                        note = named.note
                    )
                }
            }
            .distinct()

        return meant.singleOrNull()
    }

    // The memory's words as whole words of the task, accented letters too: "José" in "José's phone", not "Joséfina".
    private fun wordsOf(said: String): Regex {
        val words = Regex.escape(literal = said.lowercase())

        return Regex(pattern = "(?<!\\p{L})$words(?!\\p{L})")
    }

    // A wikilink names a note by its title or by its path: [[Laura Castro]], [[Work/Budget 2026]].
    private fun isNamed(
        candidate: FrameCandidates.Candidate,
        note: String
    ): Boolean {
        val isTitle = candidate
            .path
            .nameWithoutExtension
            .equals(
                other = note,
                ignoreCase = true
            )

        val isPath = candidate
            .path
            .bare()
            .equals(
                other = note.removeSuffix(suffix = ".md"),
                ignoreCase = true
            )

        return isTitle || isPath
    }
}
