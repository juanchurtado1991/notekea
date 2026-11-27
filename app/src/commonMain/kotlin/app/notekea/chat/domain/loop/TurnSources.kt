package app.notekea.chat.domain.loop

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.core.notes.domain.RelativePath

/**
 * The sources an answer of the loop is shown with: the passages its searches gave and the notes it read whole, only
 * those of the notes the answer names, numbered in the order it names them. A note read whole has no passage: its
 * source opens the note.
 */
internal object TurnSources {
    fun of(
        answer: String,
        passages: List<AnswerSource>,
        read: List<RelativePath>
    ): List<AnswerSource> {
        if (answer.isBlank()) return emptyList()

        val passed = passages.map { it.note }.toSet()

        val whole = read
            .filterNot {
                it in passed
            }
            .map {
                AnswerSource(
                    number = 0,
                    note = it,
                    headingPath = emptyList(),
                    range = IntRange.EMPTY,
                    text = ""
                )
            }

        val said = answer.lowercase()

        return (passages + whole)
            .distinctBy { it.note }
            .mapNotNull { source ->
                said
                    .indexOf(
                        string = source
                            .note
                            .nameWithoutExtension
                            .lowercase()
                    )
                    .takeIf {
                        it >= 0
                    }
                    ?.let {
                        it to source
                    }
            }
            .sortedBy { it.first }
            .mapIndexed { index, (_, source) -> source.copy(number = index + 1) }
    }
}
