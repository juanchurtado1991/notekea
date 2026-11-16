package app.notekea.chat.domain.memory

/**
 * What of the memory a turn is given (`docs/agent-memory.md`, "How it is read"), with no model call: *How I like
 * answers* and *About me* always, *Sources* in web work, and the other lines only when a word of the task matches
 * what they name. [MOST_CHARS] at most, so the memory never crowds the loop's prompt.
 */
object MemoryLines {
    const val MOST_CHARS = 1_500

    /** The step a turn shows for a line it was given because the task matched it: "From your memory: …". */
    const val MOVE = "memory"

    fun relevantTo(
        lines: List<MemoryLine>,
        task: String,
        isWeb: Boolean
    ): List<MemoryLine> {
        val said = wordsOf(text = task)
            .flatMap { formsOf(word = it) }
            .toSet()

        val picked = lines.filter { line ->
            when (line.section) {
                MemorySection.ANSWERS, MemorySection.ABOUT -> true

                MemorySection.SOURCES -> isWeb

                else -> keysOf(line = line).any {
                    it in said
                }
            }
        }

        val sizes = picked
            .runningFold(initial = 0) { total, line ->
                total + line.text.length + 1
            }

        return picked.filterIndexed { index, _ ->
            sizes[index + 1] <= MOST_CHARS
        }
    }

    /** The lines a task matched: those the turn shows it used, as the always-read ones would show at every turn. */
    fun matched(lines: List<MemoryLine>): List<MemoryLine> = lines.filter {
        it.section !in ALWAYS
    }

    /** Whether a line of [text]'s words says each word of [said], in order or not: "Laura" in "call Laura". */
    fun says(
        text: String,
        said: String
    ): Boolean {
        val words = wordsOf(text = text).toSet()
        val wanted = wordsOf(text = said)

        return wanted.isNotEmpty() && wanted.all { it in words }
    }

    // What a line names, word by word: "Laura", "Castro", "recipe", "cocina"; a plural is matched as its singular.
    private fun keysOf(line: MemoryLine): List<String> = wordsOf(text = line.text)
        .filter {
            it.length >= MIN_WORD && it !in COMMON
        }
        .flatMap { formsOf(word = it) }

    // A word as written and without a plural's ending: "recipes" is also "recipe", "reuniones" also "reunion".
    private fun formsOf(word: String): List<String> = listOfNotNull(
        word,
        word.removeSuffix(suffix = "s").takeIf {
            it.length >= MIN_WORD
        },
        word.removeSuffix(suffix = "es").takeIf {
            it.length >= MIN_WORD
        }
    ).distinct()

    fun wordsOf(text: String): List<String> = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")
        .split(NOT_WORD)
        .filter { it.isNotEmpty() }

    private const val MIN_WORD = 3

    private val ALWAYS = setOf(MemorySection.ANSWERS, MemorySection.ABOUT, MemorySection.SOURCES)

    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")

    // Words a line's text may hold that say nothing of what it names.
    private val COMMON = setOf(
        "the", "and", "are", "goes", "into", "with", "los", "las", "van", "son", "una", "para", "con", "del", "means",
        "tagged", "folder", "note", "notes", "nota", "notas", "carpeta"
    )

    private val ACCENTS = mapOf(
        'á' to 'a',
        'é' to 'e',
        'í' to 'i',
        'ó' to 'o',
        'ú' to 'u',
        'ü' to 'u',
        'ñ' to 'n'
    )
}
