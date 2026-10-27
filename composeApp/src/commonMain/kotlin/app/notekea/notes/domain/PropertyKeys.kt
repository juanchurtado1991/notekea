package app.notekea.notes.domain

/** Which names a property can have (`ROADMAP.md` 1.14): YAML keys the form writes without quotes. */
object PropertyKeys {
    private const val TAGS = "tags"
    private val VALID = Regex(pattern = "[\\p{L}\\p{N}_][\\p{L}\\p{N} _.-]*")

    /** Whether [key] can name a new property next to [existing] ones (a rename may keep its own name). */
    fun isValid(
        key: String,
        existing: Collection<String>,
        renaming: String? = null
    ): Boolean = VALID.matches(input = key) && key == key.trim() && (key == renaming || key !in existing)

    /** Whether [key] is the note's tags, which are shown as `#` tags like the tags written in its text. */
    fun isTags(
        key: String
    ): Boolean = key.lowercase() == TAGS
}
