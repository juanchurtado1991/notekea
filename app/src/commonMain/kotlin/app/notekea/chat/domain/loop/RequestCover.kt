package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.RelativePath

/**
 * Which entries a request could be about beside the line a change is made to, by what they write: those of its own
 * section, and those of another note that write more of the request's words. One lacking a name or a number the line
 * shares with the request is another thing. Counted by the code, from the notes' own words, in any language.
 */
internal object RequestCover {
    /** An [entry] of [note], with the words it, its heading and its note's name write. */
    data class Written(
        val note: RelativePath,
        val entry: NoteEntries.Entry,
        val words: Set<String>
    ) {
        /** The entry as the person is offered it, with its note's name when it is not in [target]. */
        fun optionFor(target: RelativePath): String =
            if (note == target) entry.shown else "${note.bare()}: ${entry.shown}"
    }

    /** What [task] could be about: the [entries], and whether they cover more of it than the line chosen. */
    data class Cover(
        val entries: List<Written>,
        val isBetter: Boolean
    )

    /**
     * The entries [task] could be about, with [line] of [target] or instead of it, those of [alike] among them; null
     * when [line] is no entry of it.
     */
    fun of(
        task: String,
        line: String,
        target: RelativePath,
        texts: Map<RelativePath, String>,
        alike: List<RelativePath>
    ): Cover? {
        val asked = FrameWords.of(text = task) + FrameWords.codesOf(text = task)

        val all = texts.flatMap { (note, text) ->
            writtenOf(
                note = note,
                text = text
            )
        }

        val chosen = chosenOf(
            line = line.trim(),
            entries = all.filter {
                it.note == target
            }
        ) ?: return null

        val names = FrameWords
            .namesOf(text = chosen.entry.line)
            .filter {
                isIn(
                    word = it,
                    words = asked
                )
            }

        val covers = all
            .filter { written ->
                written == chosen || names.all {
                    isIn(
                        word = it,
                        words = written.words
                    )
                }
            }
            .distinctBy {
                it.note to it.entry.line
            }
            .associateWith { written ->
                asked.count {
                    isIn(
                        word = it,
                        words = written.words
                    )
                }
            }

        val cover = covers.getValue(key = chosen)
        val best = covers.values.max()

        // Others cover more than the line chosen: they are what the request is about, and the line is not.
        if (best > cover) {
            return Cover(
                entries = covers
                    .filterValues {
                        it == best
                    }
                    .keys
                    .take(n = MOST),
                isBetter = true
            )
        }

        // As much in a note the request's words name alike is another one; elsewhere, the same thing written twice.
        val others = covers
            .filter { (written, count) ->
                written.note != target &&
                    written.note in alike &&
                    count == cover
            }
            .keys
            .take(n = MORE)

        val beside = besideOf(
            chosen = chosen,
            entries = covers.keys,
            most = MOST - others.size
        )

        return Cover(
            entries = beside + others,
            isBetter = false
        )
    }

    // The entry [line] is, else the first it is a part of: "Call" is the task Call, not "Call Ana" above it.
    private fun chosenOf(
        line: String,
        entries: List<Written>
    ): Written? {
        val whole = entries.firstOrNull {
            it.entry.line == line
        }

        return whole ?: entries.firstOrNull {
            line in it.entry.line
        }
    }

    // Those of its section and its kind, the nearest ones when they are more than [most]: [chosen] among them.
    private fun besideOf(
        chosen: Written,
        entries: Collection<Written>,
        most: Int
    ): List<Written> {
        val own = entries.filter {
            it.note == chosen.note &&
                it.entry.section == chosen.entry.section &&
                it.entry.kind == chosen.entry.kind
        }

        val first = (own.indexOf(element = chosen) - most / 2)
            .coerceAtMost(maximumValue = own.size - most)
            .coerceAtLeast(minimumValue = 0)

        return own
            .drop(n = first)
            .take(n = most)
    }

    private fun writtenOf(
        note: RelativePath,
        text: String
    ): List<Written> = NoteEntries
        .of(text = text)
        .map { entry ->
            val whole = "${entry.line} ${entry.heading} ${note.nameWithoutExtension}"

            Written(
                note = note,
                entry = entry,
                words = FrameWords.of(text = whole) + FrameWords.codesOf(text = whole)
            )
        }

    private fun isIn(
        word: String,
        words: Set<String>
    ): Boolean = words.any {
        FrameWords.isSame(
            one = it,
            other = word
        )
    }

    private const val MOST = 12
    private const val MORE = 4
}
