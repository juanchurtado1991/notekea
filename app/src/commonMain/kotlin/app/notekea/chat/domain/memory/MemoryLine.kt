package app.notekea.chat.domain.memory

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
        get() {
            if (section != MemorySection.PEOPLE) return null

            val said = QUOTED
                .find(input = text)
                ?.groupValues
                ?.get(1) ?: return null

            val note = LINK
                .find(input = text)
                ?.groupValues
                ?.get(1)
                ?.trim() ?: return null

            return Named(
                said = said,
                note = note
            )
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
