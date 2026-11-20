package app.notekea.chat.domain.loop

/**
 * The entries of one note the task's words for its thing fit alike, told apart by the code as namesake notes are
 * ([FrameGroups.toldOf]): the entries writing every one of those words, open when no other word of the task is
 * written by one of them alone. The 4B marked every one "no", or one of two "yes" (the frame alone, 2026-11-20).
 */
internal object FrameAlike {
    /** The entries left open, or none when fewer than two fit or a word tells them apart (the model marks them). */
    fun of(
        task: String,
        thing: String,
        entries: List<String>
    ): List<String> {
        // A number is a word of an entry too: "the 2026-11-18 booking", "chapter 1".
        val written = entries.map {
            it to FrameWords.of(text = it) + FrameWords.numbersOf(text = it)
        }

        // The thing's words that any entry writes: "the overdue invoice" is the invoice when no entry says overdue.
        val words = FrameWords
            .of(text = thing)
            .filter { word ->
                written.any { (_, own) -> holds(own, word) }
            }

        if (words.isEmpty()) return emptyList()

        val alike = written.filter { (_, own) ->
            words.all { holds(own, it) }
        }

        // Words every entry shown writes name the list, not one of it: "add vitamin D to grandpa's medicines".
        if (alike.size !in 2..<written.size) return emptyList()

        // What the task quotes is what it writes, not which entry it means.
        val unquoted = task.replace(
            regex = FrameGroups.QUOTED,
            replacement = " "
        )

        val said = FrameWords.of(text = unquoted) + FrameWords.numbersOf(text = unquoted)

        val isTold = alike.any { (entry, own) ->
            val others = alike.filter {
                it.first != entry
            }

            said.any { word ->
                holds(own, word) && others.none { (_, other) -> holds(other, word) }
            }
        }

        return if (isTold) emptyList() else alike.map { (entry, _) -> entry }
    }

    private fun holds(
        own: Set<String>,
        word: String
    ): Boolean = own.any {
        FrameWords.isSame(
            one = it,
            other = word
        )
    }
}
