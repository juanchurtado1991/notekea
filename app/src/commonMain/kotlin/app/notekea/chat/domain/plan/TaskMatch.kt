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
            val words = plain(text = wanted)
            if (words.isEmpty()) return None

            val open = tasks.filterNot {
                it.isDone || plain(text = it.text).isEmpty()
            }

            val same = open.filter {
                plain(text = it.text) == words
            }

            val holding = open.filter {
                words in plain(text = it.text)
            }

            val held = open.filter {
                plain(text = it.text) in words
            }

            val longest = held.maxOfOrNull { plain(text = it.text).length }

            val tier = same
                .ifEmpty { holding }
                .ifEmpty {
                    held.filter {
                        plain(text = it.text).length == longest
                    }
                }

            val texts = tier
                .map { it.text.trim() }
                .distinctBy { plain(text = it) }

            return when {
                tier.isEmpty() -> None
                texts.size > 1 -> Several(texts = texts)
                else -> One(task = tier.first())
            }
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
