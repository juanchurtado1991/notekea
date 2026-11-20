package app.notekea.chat.domain.plan

/**
 * The line of a note a change's words mean (4.2), found as [TaskMatch] finds a task: the line of those words, else the
 * ones that hold them, else the longest they hold, each without its marks ([LineEdit]) and Markdown. Lines alike that
 * differ are [Several], never the first of them; identical lines are the first, as changing either reads the same.
 */
sealed interface LineMatch {
    /** The line at index [at] of the lines looked in, as the note writes it. */
    data class One(
        val at: Int,
        val line: String
    ) : LineMatch

    data class Several(val lines: List<String>) : LineMatch

    data object None : LineMatch

    companion object {
        // A code fence, a table's separator row or a thematic break: no line a change means.
        private val FRAME = Regex(pattern = "^\\s*(```|~~~|\\|?[\\s:|-]*-[\\s:|-]*\\|?\\s*$)")

        fun of(
            lines: List<String>,
            wanted: String
        ): LineMatch {
            val changeable = lines
                .withIndex()
                .filter { (_, line) ->
                    line.isNotBlank() && !FRAME.containsMatchIn(input = line)
                }

            val tier = TaskMatch.closest(
                items = changeable,
                wanted = LineEdit.textOf(line = wanted)
            ) { LineEdit.textOf(line = it.value) }

            val texts = tier
                .map { it.value.trim() }
                .distinctBy { TaskMatch.plain(text = LineEdit.textOf(line = it)) }

            val first = tier.firstOrNull()

            return when {
                first == null -> None

                texts.size > 1 -> Several(lines = texts)

                else -> One(
                    at = first.index,
                    line = first.value
                )
            }
        }
    }
}
