package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.AmbiguousTask

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
        pattern = "\\b(all|every|everyone|everybody|each|both|todas|todos|cada|ambos|ambas)\\b",
        option = RegexOption.IGNORE_CASE
    )

    // Several notes are meant, so "the garden" in "the journal notes that mention the garden" names what they say,
    // and "la etiqueta money" in "los proyectos con la etiqueta money" the tag: neither is the one note to change
    // (the loop's seventh run asked which note for both).
    private val SEVERAL = Regex(
        pattern = "\\b(notes|notas|ones|them|those|these|los|las|estos|estas|esos|esas)\\b|" +
            "\\p{L}{4,}(los|las)\\b",
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

    /** "it" when [task] points at one thing with no name ("tag it as urgent") and names no several. */
    fun pointedIn(task: String): String? {
        val isSeveral = isSeveral(task = task)

        return IT
            .find(input = task)
            ?.value
            .takeUnless { isSeveral }
    }

    private val IT = Regex(
        pattern = "\\b(it|this|esto|eso)\\b",
        option = RegexOption.IGNORE_CASE
    )

    /** "the project" when [task] names one thing so; null when it names several or none. */
    fun oneIn(task: String): String? {
        if (isSeveral(task = task)) return null

        return THE
            .findAll(input = task)
            .map { it.value.trim() }
            .firstOrNull { said ->
                val word = said
                    .split(" ")
                    .getOrNull(index = 1)
                    .orEmpty()
                    .lowercase()

                word !in NOT_THINGS && !AmbiguousTask.isPluralWord(word = word)
            }
    }

    // Words inside quoted text to add say nothing of how many notes: "add 'read two books' to my goals".
    private fun isSeveral(task: String): Boolean {
        val said = AmbiguousTask.unquoted(task = task)

        return PLURAL.containsMatchIn(input = said) || SEVERAL.containsMatchIn(input = said)
    }
}
