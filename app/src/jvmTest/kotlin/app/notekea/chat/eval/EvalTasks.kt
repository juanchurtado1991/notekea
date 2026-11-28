package app.notekea.chat.eval

import java.nio.file.Files
import java.nio.file.Path

/**
 * The tasks a round is narrowed to (`docs/gemma-plan.md`, section 7): those holding one of [words], and those a file
 * lists, a task a line as written. With neither, every task.
 */
internal class EvalTasks(
    private val words: List<String>,
    private val listed: Set<String>?
) {
    fun holds(task: String): Boolean {
        val said = task.trim().lowercase()
        val isWorded = words.isEmpty() || words.any { it in said }
        val isListed = listed == null || said in listed

        return isWorded && isListed
    }

    companion object {
        /** `-PchatTasks="budget,reading challenge"`: only the tasks that hold one of those words. */
        const val WORDS = "notekea.chat.tasks"

        /** `-PchatTaskFile=<file>`: only the tasks the file lists, a line each (`scripts/loop-eval/fastset.py`). */
        const val FILE = "notekea.chat.taskFile"

        fun fromProperties(): EvalTasks {
            val words = System
                .getProperty(WORDS)
                .orEmpty()
                .split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }

            val listed = System
                .getProperty(FILE)
                .orEmpty()
                .takeIf { it.isNotEmpty() }
                ?.let { file ->
                    Files
                        .readAllLines(Path.of(file))
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .toSet()
                }

            return EvalTasks(
                words = words,
                listed = listed
            )
        }
    }
}
