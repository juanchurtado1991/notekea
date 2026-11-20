package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.tasks.NoteTask

/**
 * The open task of a note a step's words mean: the task of those words, else the ones that hold them, else the
 * longest they hold, Markdown and task dates left out. Tasks alike that differ ("call": "Call mom", "Call dentist")
 * are [Several], never the first of them; identical lines are one, as ticking either is the same.
 */
sealed interface TaskMatch {
    data class One(val task: NoteTask) : TaskMatch

    data class Several(val texts: List<String>) : TaskMatch

    data object None : TaskMatch

    companion object {
        private val DATE = Regex(pattern = "[📅⏳🛫✅➕]\\s*\\d{4}-\\d{2}-\\d{2}")
        private val BLOCK_ID = Regex(pattern = "\\s\\^[\\w-]+\\s*$")
        private val WIKI = Regex(pattern = "\\[\\[(?:[^\\]|]*\\|)?([^\\]]*)]]")
        private val LINK = Regex(pattern = "\\[([^\\]]*)]\\([^)]*\\)")
        private val MARKS = Regex(pattern = "\\*\\*|__|~~|==|[*`]")
        private val SPACES = Regex(pattern = "\\s+")

        fun of(
            tasks: List<NoteTask>,
            wanted: String
        ): TaskMatch {
            val open = tasks.filterNot { it.isDone }

            val tier = closest(
                items = open,
                wanted = wanted
            ) { it.text }

            val texts = tier
                .map { it.text.trim() }
                .distinctBy { plain(text = it) }

            return when {
                tier.isEmpty() -> None
                texts.size > 1 -> Several(texts = texts)
                else -> One(task = tier.first())
            }
        }

        /**
         * The [items] whose text ([textOf]) [wanted] means: those of its words, else those that hold them, else the
         * longest they hold; none when it has no words. Shared with [LineMatch].
         */
        fun <T> closest(
            items: List<T>,
            wanted: String,
            textOf: (T) -> String
        ): List<T> {
            val words = plain(text = wanted)
            if (words.isEmpty()) return emptyList()

            val plains = items
                .map {
                    it to plain(text = textOf(it))
                }
                .filter { (_, text) -> text.isNotEmpty() }

            val same = plains.filter { (_, text) ->
                text == words
            }

            val holding = plains.filter { (_, text) ->
                words in text
            }

            val held = plains.filter { (_, text) ->
                text in words
            }

            val longest = held.maxOfOrNull { (_, text) -> text.length }

            return same
                .ifEmpty { holding }
                .ifEmpty {
                    held.filter { (_, text) ->
                        text.length == longest
                    }
                }
                .map { (item, _) -> item }
        }

        /** [text] as its words alone: no Markdown marks, no link targets, no task dates, lower case. */
        fun plain(text: String): String = text
            .replace(
                regex = DATE,
                replacement = ""
            )
            .replace(
                regex = BLOCK_ID,
                replacement = ""
            )
            .replace(
                regex = WIKI,
                replacement = "\$1"
            )
            .replace(
                regex = LINK,
                replacement = "\$1"
            )
            .replace(
                regex = MARKS,
                replacement = ""
            )
            .replace(
                regex = SPACES,
                replacement = " "
            )
            .trim()
            .lowercase()
    }
}
