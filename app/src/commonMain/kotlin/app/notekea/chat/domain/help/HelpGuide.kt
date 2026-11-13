package app.notekea.chat.domain.help

/**
 * The app's guide (`files/guide/guide.md`) as [HelpTopic]s: one per `# ` heading, with its `words:` and `try:`
 * lines; the topics a question is about are those whose title and words share the most with it.
 */
class HelpGuide(val topics: List<HelpTopic>) {
    /** Up to [max] topics about [question], the closest first; none when nothing matches. */
    fun about(
        question: String,
        max: Int = MAX_TOPICS
    ): List<HelpTopic> {
        val asked = plain(text = question)

        return topics
            .map { topic ->
                topic to (topic.words + topic.title).sumOf { phrase ->
                    val words = plain(text = phrase)
                    if (words.isNotBlank() && words in asked) words.length else 0
                }
            }
            .filter { (_, score) ->
                score > 0
            }
            .sortedByDescending { (_, score) -> score }
            .take(n = max)
            .map { (topic, _) -> topic }
    }

    companion object {
        private const val MAX_TOPICS = 3
        private const val WORDS = "words:"
        private const val TRY = "try:"
        private val ACCENTS =
            mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ñ' to 'n')
        private val SHORTCUT = Regex(pattern = "\\{([a-zA-Z]+)\\}")
        private val COMMENT = Regex(
            pattern = "<!--.*?-->",
            option = RegexOption.DOT_MATCHES_ALL
        )

        /** The guide in [markdown]: everything before the first `# ` heading is left out. */
        fun of(markdown: String): HelpGuide = HelpGuide(
            topics = COMMENT
                .replace(
                    input = markdown,
                    replacement = ""
                )
                .split("\n# ")
                .map { it.removePrefix(prefix = "# ").trim() }
                .filter { it.isNotEmpty() }
                .map { topicOf(block = it) }
        )

        /** [text] with every `{name}` written as [shortcuts] has it; a name it lacks stays as it was. */
        fun filled(
            text: String,
            shortcuts: Map<String, String>
        ): String = SHORTCUT
            .replace(input = text) { match ->
                shortcuts[match.groupValues[1]] ?: match.value
            }

        /** The `{name}`s [text] uses, so a test can check every one is known. */
        fun shortcutsIn(text: String): Set<String> = SHORTCUT
            .findAll(input = text)
            .map {
                it.groupValues[1]
            }
            .toSet()

        private fun topicOf(block: String): HelpTopic {
            val lines = block.lines()
            val rest = lines.drop(n = 1)

            val words = rest
                .firstOrNull { it.startsWith(prefix = WORDS) }
                ?.removePrefix(prefix = WORDS)
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                .orEmpty()

            val tryRequest = rest
                .firstOrNull { it.startsWith(prefix = TRY) }
                ?.removePrefix(prefix = TRY)
                ?.trim()

            val text = rest
                .filterNot {
                    it.startsWith(prefix = WORDS) || it.startsWith(prefix = TRY)
                }
                .joinToString(separator = "\n")
                .trim()

            return HelpTopic(
                title = lines.first().trim(),
                words = words,
                tryRequest = tryRequest,
                text = text
            )
        }

        private fun plain(text: String): String = text
            .lowercase()
            .map {
                ACCENTS[it] ?: it
            }
            .joinToString(separator = "")
            .replace(Regex(pattern = "[¿?¡!.,:;\"']"), " ")
            .replace(Regex(pattern = "\\s+"), " ")
            .trim()
            .let { " $it " }
    }
}
