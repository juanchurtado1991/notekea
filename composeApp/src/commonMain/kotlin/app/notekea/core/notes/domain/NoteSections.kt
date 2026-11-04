package app.notekea.core.notes.domain

/**
 * Splits a note's body into its [NoteSection]s by its headings, as the outline finds them ([OutlineFinder]: one to six
 * `#`, never inside a code block). A heading's path holds the open headings of a lower level above it, as the outline
 * nests them; a body with nothing but blank text before its first heading has no section there.
 */
class NoteSections(
    private val outlineFinder: OutlineFinder
) {
    fun of(
        body: String
    ): List<NoteSection> {
        val headings = outlineFinder.headings(text = body)
        val open = ArrayDeque<Pair<Int, String>>()
        val paths = headings.map { heading ->
            while (open.lastOrNull()?.let { it.first >= heading.level } == true) open.removeLast()
            open.addLast(heading.level to heading.text)
            open.map { it.second }
        }
        val starts = listOf(0) + headings.map { it.offset }
        val ends = headings.map { it.offset } + body.length
        return starts.indices.map { index ->
            NoteSection(
                headingPath = if (index == 0) emptyList() else paths[index - 1],
                range = starts[index] until ends[index]
            )
        }.filter { section -> body.substring(range = section.range).isNotBlank() }
    }
}
