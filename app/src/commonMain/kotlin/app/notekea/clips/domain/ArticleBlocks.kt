package app.notekea.clips.domain

/**
 * An article's Markdown blocks without what a site writes around every page (the live probe of 2026-11-13: fifteen
 * sites): the trail of links above it ("Docs", "Get started"), and its buttons' words ("Back to top", "Edit").
 */
object ArticleBlocks {
    // A block that is only links: "[Docs](…)", or a list of them.
    private val LINK_LINE = Regex(pattern = "^(?:[-*] |\\d+\\. )?\\[[^\\]]+]\\([^)]+\\)$")

    private val BUTTON = Regex(
        pattern = "^(?:#+ )?(back to top|table of contents|in this article|on this page|contents|copy|copied!?|" +
            "edit|edit this page|share|feedback|was this (page|article) helpful\\??|skip to (main )?content|" +
            "view as markdown|tip|note|" +
            "volver arriba|tabla de contenidos?|en esta página|en este artículo|contenidos?|copiar|editar|compartir)$",
        option = RegexOption.IGNORE_CASE
    )

    // Said inside a heading of every page of Google's developer sites.
    private val INSIDE = Regex(
        pattern = "\\s*Stay organized with collections\\s+Save and categorize content based on your preferences\\.",
        option = RegexOption.IGNORE_CASE
    )

    private val BUTTON_LINK = Regex(
        pattern = "^\\[(view as markdown|edit|edit this page|edit on github|back to top|share)]\\([^)]+\\)$",
        option = RegexOption.IGNORE_CASE
    )

    fun cleaned(blocks: List<String>): List<String> {
        val kept = blocks
            .map { it.replace(INSIDE, replacement = "").trim() }
            .filterNot { block ->
                val isButton = BUTTON.matches(input = block) || BUTTON_LINK.matches(input = block)

                block.isEmpty() ||
                    block.trimStart('#', ' ').isEmpty() ||
                    isButton
            }

        // The links above the article's first words lead elsewhere in the site; a page that is all links is an
        // index, and keeps them.
        val trail = kept.takeWhile { isLinks(block = it) }

        return if (trail.size == kept.size) kept else kept.drop(n = trail.size)
    }

    private fun isLinks(block: String): Boolean = block
        .lines()
        .all { LINK_LINE.matches(input = it.trim()) }
}
