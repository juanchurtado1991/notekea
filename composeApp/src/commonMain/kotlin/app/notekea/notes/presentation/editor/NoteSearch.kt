package app.notekea.notes.presentation.editor

/** Where `query` appears in a note's body for Find in note: every match, ignoring letter case, without overlaps. */
class NoteSearch {
    fun matches(
        text: CharSequence,
        query: String
    ): List<IntRange> {
        if (query.isEmpty()) return emptyList()
        val found = mutableListOf<IntRange>()
        var start = text.indexOf(string = query, startIndex = 0, ignoreCase = true)
        while (start >= 0) {
            found += start until start + query.length
            start = text.indexOf(string = query, startIndex = start + query.length, ignoreCase = true)
        }
        return found
    }
}
