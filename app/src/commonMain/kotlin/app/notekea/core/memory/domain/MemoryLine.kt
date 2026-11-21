package app.notekea.core.memory.domain

/**
 * One fact of the chat's memory (`docs/agent-memory.md`): a line of `Notekea/Memory.md` under its [section], its
 * [text] as the model is given it, and the hidden comment that says when and how it was learnt ([origin]).
 */
data class MemoryLine(
    val section: MemorySection,
    val text: String,
    val origin: String?
) {
    /**
     * What a line of *People and names* settles: the words in quotes ("Laura") and the note they mean (the first
     * wikilink, [[Laura Castro]]); null for a line of another section or of another shape.
     */
    val named: Named?
        get() = names.firstOrNull()

    /**
     * Every name the line settles: each phrase in quotes before the link means its note, so `"the weekly review" and
     * "la revisión semanal" are [[Work/Weekly review]]` settles both (the loop's fifth run asked which for the second).
     */
    val names: List<Named>
        get() {
            if (section != MemorySection.PEOPLE) return emptyList()

            val note = LINK
                .find(input = text)
                ?.groupValues
                ?.get(1)
                ?.trim() ?: return emptyList()

            return QUOTED
                .findAll(input = text.substringBefore(delimiter = "[["))
                .map {
                    Named(
                        said = it.groupValues[1],
                        note = note
                    )
                }
                .toList()
        }

    /** "[said]" means the note [note], as a wikilink names it: a title or a path. */
    data class Named(
        val said: String,
        val note: String
    )

    private companion object {
        val QUOTED = Regex(pattern = "[\"“«]([^\"”»]+)[\"”»]")
        val LINK = Regex(pattern = "\\[\\[([^\\]|#]+)")
    }
}
