package app.notekea.chat.domain.tool.change

/**
 * The one thing a task names with "the" (the project, the supplier meeting; el, la in Spanish), when it names one
 * and no more: a change that acts on several notes for it may have guessed which one was meant.
 */
internal object SingularTask {
    private val THE = Regex(
        pattern = "\\b(the|el|la)\\s+([\\p{L}-]+)(\\s+[\\p{L}-]+)?",
        option = RegexOption.IGNORE_CASE
    )
    private val PLURAL = Regex(
        pattern = "\\b(all|every|each|both|todas|todos|cada|ambos|ambas)\\b",
        option = RegexOption.IGNORE_CASE
    )
    private val NOT_THINGS =
        setOf("notes", "note", "nota", "notas", "vault", "folder", "carpeta", "tasks", "task") +
            setOf("two", "three", "four", "dos", "tres", "cuatro")

    private val POINTED = Regex(
        pattern = "\\b(it|this|that|esto|eso|esta|esa|este|ese)\\b",
        option = RegexOption.IGNORE_CASE
    )

    /** Whether [task] points at one thing it does not name: "the project", "tag it", "esta nota". */
    fun pointsAtOne(task: String): Boolean =
        oneIn(task = task) != null || POINTED.containsMatchIn(input = task)

    /** "the project" when [task] names one thing so; null when it names several or none. */
    fun oneIn(task: String): String? {
        if (PLURAL.containsMatchIn(input = task)) return null

        return THE
            .findAll(input = task)
            .map { it.value.trim() }
            .firstOrNull { said ->
                val word = said
                    .split(" ")
                    .getOrNull(index = 1)
                    .orEmpty()
                    .lowercase()

                word !in NOT_THINGS && !word.endsWith(suffix = "s")
            }
    }
}
