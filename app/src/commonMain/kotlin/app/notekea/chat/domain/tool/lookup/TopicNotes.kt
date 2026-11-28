package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.loop.FrameWords
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath

/**
 * The notes on the topic of a note read (`docs/agent-figures.md`): those it links or that share a tag with it, when
 * they write a word of the task, as papá's allergy sat in a second note of the same tag. No word list decides it.
 */
internal object TopicNotes {
    suspend fun of(
        note: NoteEntry,
        text: String,
        context: ToolContext,
        cache: NoteTextCache
    ): List<RelativePath> {
        val words = FrameWords.of(text = context.task)
        if (words.isEmpty()) return emptyList()

        val read = context
            .reads
            .read
            .toSet() + note.file.path

        val tags = note
            .allTags
            .map { it.lowercase() }
            .toSet()

        val linked = LINK
            .findAll(input = text)
            .mapNotNull { context.noteNamed(name = it.groupValues[1].trim()) }
            .toSet()

        val near = context.looked.filter { other ->
            val path = other.file.path
            val isNear = path in linked || other.allTags.any { it.lowercase() in tags }

            path.isMarkdown &&
                path !in read &&
                isNear
        }

        if (near.isEmpty() || near.size > MOST_NEAR) return emptyList()

        return cache
            .textsOf(
                vaultPath = context.vaultPath,
                notes = near
            )
            .filter { (_, near) ->
                val written = FrameWords.of(text = near.body)

                words.any { word ->
                    written.any { FrameWords.isSame(word, it) }
                }
            }
            .keys
            .take(n = MOST)
    }

    private val LINK = Regex(pattern = "\\[\\[([^\\]|#^]+)")
    private const val MOST = 3

    // A tag on most of the vault ("#journal" on every day) says nothing of the topic.
    private const val MOST_NEAR = 25
}
