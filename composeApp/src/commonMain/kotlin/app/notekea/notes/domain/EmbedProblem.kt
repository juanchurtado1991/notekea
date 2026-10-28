package app.notekea.notes.domain

/**
 * Why an embedded note cannot show its text (`ROADMAP.md` 1.15): the note does not exist ([MISSING_NOTE]), it has no
 * such heading or block ([MISSING_SECTION]), it is already shown above it in the same chain of embeds, the note itself
 * included ([CIRCULAR]), or it is deeper than [MAX_EMBED_DEPTH] notes ([TOO_DEEP]).
 */
enum class EmbedProblem {
    MISSING_NOTE,
    MISSING_SECTION,
    CIRCULAR,
    TOO_DEEP;

    companion object {
        /** How many notes deep embeds are shown: a note, the notes it embeds, and theirs. */
        const val MAX_EMBED_DEPTH = 3
    }
}
