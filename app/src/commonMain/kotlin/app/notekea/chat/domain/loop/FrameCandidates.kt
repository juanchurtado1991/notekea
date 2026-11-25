package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The notes a task may be about, with the entries in them its words touch (`docs/agent-frame.md`): found by the code
 * from the memory, the open note, names, the search, and every namesake; the model only says which it means.
 */
internal object FrameCandidates {
    /**
     * A note shown to the frame's model: its path, its first line, the entries the task's words touch, the [lines]
     * that write one of them, and what tells it from a note of the same name: the [words] it writes and its [title].
     */
    data class Candidate(
        val path: RelativePath,
        val gist: String,
        val entries: List<String>,
        val lines: List<String> = emptyList(),
        val words: Set<String> = emptySet(),
        val title: String = ""
    )

    fun of(
        task: String,
        paths: List<RelativePath>,
        texts: Map<RelativePath, String>
    ): List<Candidate> = paths.map { path ->
        val text = texts[path].orEmpty()

        Candidate(
            path = path,
            gist = gistOf(text = text),
            entries = entriesOf(
                task = task,
                text = text
            ),
            lines = linesOf(
                task = task,
                text = text
            ),
            words = FrameWords.of(text = text),
            title = text
                .lineSequence()
                .firstOrNull { it.startsWith(prefix = "# ") }
                .orEmpty()
        )
    }

    // The lines of [text] that write a word of [task], headings left out: what tells a note apart from its namesake.
    private fun linesOf(
        task: String,
        text: String
    ): List<String> {
        val words = FrameWords.of(text = task)

        return text
            .lineSequence()
            .map { it.trim() }
            .filter {
                it.isNotEmpty() &&
                    !it.startsWith(prefix = "#") &&
                    touches(line = it, words = words)
            }
            .map { it.take(n = ENTRY) }
            .take(n = MOST_LINES)
            .toList()
    }

    /** The paths shown, at most [MOST]: [first] (the memory's, the open note), named, found, then their namesakes. */
    fun pathsOf(
        task: String,
        notes: Collection<NoteEntry>,
        found: List<RelativePath>,
        first: List<RelativePath>
    ): List<RelativePath> {
        val words = FrameWords.of(text = task)
        val markdown = notes.map { it.file.path }.filter { it.isMarkdown }

        val named = markdown.filter { path ->
            val title = path.bare()

            FrameWords.of(text = title).any { word ->
                words.any {
                    FrameWords.isSame(
                        one = it,
                        other = word
                    )
                }
            }
        }

        val picked = (first + named + found)
            .filter {
                it in markdown
            }
            .distinct()
            .take(n = MOST)

        val namesakes = picked.flatMap { path ->
            markdown.filter {
                val isNamesake = it.nameWithoutExtension.equals(
                    other = path.nameWithoutExtension,
                    ignoreCase = true
                )

                it != path && isNamesake
            }
        }

        // A namesake goes right after its note: the choice between them is the one the frame must see.
        return (picked + namesakes)
            .distinct()
            .take(n = MOST + NAMESAKES)
    }

    private fun gistOf(text: String): String = text
        .lineSequence()
        .map { it.trim() }
        .firstOrNull {
            it.isNotEmpty() &&
                !it.startsWith(prefix = "#") &&
                !it.startsWith(prefix = "---")
        }
        .orEmpty()
        .take(n = GIST)

    /**
     * The lines of a list, a task list or a table in [text] that the task's words touch: by a word of their own, or
     * by the heading of the section they are in ("## Appointments" holds the appointments whatever they say); every
     * entry when fewer than two are touched.
     */
    fun entriesOf(
        task: String,
        text: String
    ): List<String> {
        val words = FrameWords.of(text = task)
        if (words.isEmpty()) return emptyList()

        val entries = NoteEntries.of(text = text)

        val touched = entries.filter {
            touches(it.line, words) || touches(it.heading, words)
        }

        // Fewer than two touched: every entry, for the model to mark. "My physio appointment" touched the one
        // entry that says physio, and the other sessions of the note were never asked about (the frame alone,
        // 2026-11-18).
        return touched
            .takeIf {
                it.size > 1
            }
            .orEmpty()
            .ifEmpty { entries }
            .map { it.shown }
            .distinct()
            .take(n = MOST_ENTRIES)
    }

    /** The entries of [text] that write one of [words]. */
    fun touchedOf(
        words: Set<String>,
        text: String
    ): List<String> = NoteEntries
        .of(text = text)
        .filter { touches(it.line, words) }
        .map { it.shown }
        .distinct()
        .take(n = MOST_ENTRIES)

    private fun touches(
        line: String,
        words: Set<String>
    ): Boolean = FrameWords.of(text = line).any { word ->
        words.any {
            FrameWords.isSame(
                one = it,
                other = word
            )
        }
    }

    private const val MOST = 10
    private const val NAMESAKES = 4
    private const val MOST_ENTRIES = 12
    private const val MOST_LINES = 3
    private const val GIST = 120
    private const val ENTRY = 140
}
