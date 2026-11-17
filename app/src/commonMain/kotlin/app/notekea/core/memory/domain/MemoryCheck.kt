package app.notekea.core.memory.domain

/**
 * What a line of the memory must be before it is proposed (`docs/agent-memory.md`, layers 1 and 2), checked with no
 * model call: from the person's own words, one short fact, nothing secret, guessed or permitting, and a note or a
 * folder it names that exists. [problemOf] says why a line is refused, written for the model to correct.
 */
object MemoryCheck {
    const val MOST_CHARS = 160

    fun problemOf(
        line: String,
        said: String,
        notes: Collection<String>,
        folders: Collection<String>
    ): String? {
        val text = line.trim()

        val missing = missingOf(
            text = text,
            notes = notes,
            folders = folders
        )

        return when {
            text.isEmpty() -> "a memory line cannot be empty"
            text.length > MOST_CHARS -> "a memory line is one short fact, $MOST_CHARS characters at most"
            MemoryNote.isPermission(text = text) -> "the memory never holds leave to act; Settings does"
            SECRET.containsMatchIn(input = text) -> "the memory never holds numbers of accounts, cards or IDs"
            PRIVATE.containsMatchIn(input = text) -> "the memory never holds health, money or secrets of the person"
            HEDGE.containsMatchIn(input = text) -> "a memory line says what the person said, never a guess"
            !isFromPerson(line = text, said = said) -> "remember only what the person said, in their own words"
            missing != null -> "no note or folder \"$missing\" is in the vault"
            else -> null
        }
    }

    /**
     * Whether the words of [line] are the person's: every word of it, but what it links or names in backticks, is in
     * what they [said] (layer 1). A line made of a page's or a note's text is not.
     */
    fun isFromPerson(
        line: String,
        said: String
    ): Boolean {
        val spoken = MemoryLines
            .wordsOf(text = said)
            .toSet()

        val own = MemoryLines.wordsOf(
            text = line
                .replace(
                    regex = NAMED,
                    replacement = " "
                )
        )

        return own
            .filter {
                it.length >= MIN_WORD && it !in GLUE
            }
            .all {
                it in spoken
            }
    }

    // The first link or folder of the line that the vault does not have.
    private fun missingOf(
        text: String,
        notes: Collection<String>,
        folders: Collection<String>
    ): String? {
        val titles = notes.map { it.substringAfterLast(delimiter = '/').lowercase() }

        val paths = notes.map { it.lowercase() }

        val note = LINK
            .findAll(input = text)
            .map {
                it
                    .groupValues[1]
                    .trim()
                    .removeSuffix(suffix = ".md")
            }
            .firstOrNull { link ->
                val wanted = link.lowercase()
                if ('/' in wanted) wanted !in paths else wanted !in titles
            }

        val known = folders.map { it.trim('/').lowercase() }

        val folder = FOLDER
            .findAll(input = text)
            .map {
                it
                    .groupValues[1]
                    .trim()
                    .trim('/')
            }
            .firstOrNull {
                it.lowercase() !in known
            }

        return note ?: folder
    }

    private const val MIN_WORD = 3

    private val LINK = Regex(pattern = "\\[\\[([^\\]|#]+)")
    private val FOLDER = Regex(pattern = "`([^`]+/)`")
    private val NAMED = Regex(pattern = "\\[\\[[^\\]]*]]|`[^`]*`")

    // A line's own glue, in the memory's headings' language and the person's: never a fact they must have said.
    private val GLUE = setOf(
        "the", "and", "are", "for", "goes", "into", "with", "means", "folder", "note", "notes", "trusted", "not",
        "los", "las", "van", "son", "una", "uno", "para", "con", "del", "que", "nota", "notas", "carpeta", "confio"
    )

    private val SECRET = Regex(pattern = "\\d[\\d -]{11,}\\d|\\b[A-Z]{2}\\d{2}[A-Z0-9]{10,}\\b")

    private val PRIVATE = Regex(
        pattern = "password|contrase[nñ]a|\\bpin\\b|diagnos|medicat|medicament|enfermedad|illness|salary|salario|" +
            "\\bdebt\\b|deuda|\\bsueldo\\b",
        option = RegexOption.IGNORE_CASE
    )

    private val HEDGE = Regex(
        pattern = "\\b(seems|probably|maybe|perhaps|likely|parece|probablemente|quiz[aá]s?|tal vez)(?![\\p{L}\\p{N}])",
        option = RegexOption.IGNORE_CASE
    )
}
