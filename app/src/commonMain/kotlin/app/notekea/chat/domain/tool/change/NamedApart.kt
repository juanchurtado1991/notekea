package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.FrameWords
import app.notekea.core.notes.domain.NoteEntry

/**
 * Of the notes that share a name, the one the task tells from the others by a word only it is called by: a folder,
 * its title, an alias. "In the lab budget" says Lab/Budget, not Travel/Budget (the E4B asked which, 2026-11-25).
 */
internal object NamedApart {
    /** The one of [notes] that [task] tells apart; null when it tells none, or more than one. */
    fun of(
        task: String,
        notes: List<NoteEntry>
    ): NoteEntry? {
        val said = FrameWords.of(text = task) + FrameWords.shortOf(text = task)

        val called = notes.associateWith { wordsOf(note = it) }

        return notes
            .filter { note ->
                val others = (notes - note).flatMap { called.getValue(key = it) }.toSet()

                called
                    .getValue(key = note)
                    .filterNot {
                        it in others
                    }
                    .any { word ->
                        said.any {
                            FrameWords.isSame(
                                one = it,
                                other = word
                            )
                        }
                    }
            }
            .singleOrNull()
    }

    // What a note is called by beside its name: its folders, its title and its aliases, less its name's words. A word
    // of three letters only of a folder ("Lab"): in a title it is "and" or "the".
    private fun wordsOf(note: NoteEntry): Set<String> {
        val name = FrameWords.of(text = note.file.path.nameWithoutExtension)

        val folders = note.folders.flatMap {
            FrameWords.of(text = it) + FrameWords.shortOf(text = it)
        }

        val called = (listOfNotNull(note.metadata.title) + note.metadata.aliases).flatMap {
            FrameWords.of(text = it)
        }

        return (folders + called)
            .filterNot {
                it in name
            }
            .toSet()
    }
}
