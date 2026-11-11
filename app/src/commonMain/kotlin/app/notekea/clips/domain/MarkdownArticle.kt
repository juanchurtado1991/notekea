package app.notekea.clips.domain

/**
 * A page a site serves as Markdown for machines (a docs page's `.md`, an llms.txt entry) read as its [Article]: title
 * from its first heading, blocks as paragraphs with code kept whole; the site's note about its index left out.
 */
internal object MarkdownArticle {
    private const val MIN_WORDS = 15
    private const val TITLE_CHARS = 100

    // Node.js's docs keep their history in comments: "<!-- YAML added: v9.9.0 … -->".
    private val COMMENT = Regex(
        pattern = "<!--.*?-->",
        option = RegexOption.DOT_MATCHES_ALL
    )

    private val FRONTMATTER = Regex(
        pattern = "\\A---\\n.*?\\n---\\n",
        option = RegexOption.DOT_MATCHES_ALL
    )

    /** Whether [text] is Markdown or plain text rather than a web page. */
    fun isMarkdown(text: String): Boolean {
        val start = text.trimStart()

        return start.isNotEmpty() &&
            !start.startsWith(prefix = "<") &&
            !text.contains(other = "<html", ignoreCase = true)
    }

    fun of(text: String): Article? {
        val bare = text
            .replace(FRONTMATTER, replacement = "")
            .replace(COMMENT, replacement = "")
            .trim()

        val blocks = blocksOf(text = bare)
            .filterNot {
                it.startsWith(prefix = ">") && "llms.txt" in it
            }

        val heading = blocks.firstOrNull { it.startsWith(prefix = "# ") }

        val title = heading?.removePrefix(prefix = "# ")?.trim() ?: blocks
            .firstOrNull()
            ?.lines()
            ?.first()
            ?.take(n = TITLE_CHARS)

        val paragraphs = blocks.filterNot {
            it == heading
        }

        val words = paragraphs.sumOf { it.split(Regex(pattern = "\\s+")).size }

        if (title == null || words < MIN_WORDS) return null

        return Article(
            title = title,
            paragraphs = paragraphs
        )
    }

    // Blocks split at blank lines, a fenced code block kept as one.
    private fun blocksOf(text: String): List<String> {
        val blocks = mutableListOf<String>()
        val current = StringBuilder()
        var isFenced = false

        text.lines().forEach { line ->
            if (line.trimStart().startsWith(prefix = "```")) isFenced = !isFenced

            if (line.isBlank() && !isFenced) {
                if (current.isNotBlank()) blocks += current.toString().trim()
                current.clear()
            } else {
                current.append(line).append('\n')
            }
        }

        if (current.isNotBlank()) blocks += current.toString().trim()

        return blocks
    }
}
