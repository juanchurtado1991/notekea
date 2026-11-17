package app.notekea.core.notes.data

import app.notekea.core.notes.domain.links.LinkFinder
import app.notekea.core.notes.domain.links.LinkKind
import app.notekea.core.notes.domain.links.NoteLink

/**
 * [LinkFinder] that uses the JetBrains Markdown parser only to know where code is ([MarkdownCodeRanges]), since it
 * does not know Obsidian's `[[wiki links]]`, and finds the links themselves with patterns. Markdown links count only
 * when they have no URL scheme, so web pages are never rewritten, and lead to a `.md` file or a name with no extension.
 */
class JetBrainsMarkdownLinkFinder : LinkFinder {
    private val codeRanges = MarkdownCodeRanges()

    override fun find(markdown: String): List<NoteLink> = linksIn(
        markdown = markdown,
        isWanted = ::isNoteTarget
    )

    override fun findWithAttachments(markdown: String): List<NoteLink> = linksIn(
        markdown = markdown,
        isWanted = {
            !hasScheme(target = it)
        }
    )

    private fun linksIn(
        markdown: String,
        isWanted: (String) -> Boolean
    ): List<NoteLink> {
        val markdownLinks = matches(
            markdown = markdown,
            pattern = MARKDOWN_ANGLE_LINK,
            kind = LinkKind.MARKDOWN_ANGLE
        ) +
            matches(
                markdown = markdown,
                pattern = MARKDOWN_LINK,
                kind = LinkKind.MARKDOWN_ENCODED
            )

        // Every Markdown kind is filtered: a web page in angle brackets is no more a note than one without them.
        val links = matches(
            markdown = markdown,
            pattern = WIKI_LINK,
            kind = LinkKind.WIKI
        ) + markdownLinks.filter { isWanted(it.target) }

        // The Markdown tree, which finds the code the links must not be in, is built only when there are links.
        if (links.isEmpty()) return emptyList()
        val code = codeRanges.of(markdown = markdown)

        return links.filterNot { link ->
            code.any {
                link.targetStart in it
            }
        }.sortedBy { it.targetStart }
    }

    /** A link to a note: no URL scheme, and a `.md` file or a last segment with no extension (`[text](Note)`). */
    private fun isNoteTarget(target: String): Boolean {
        val name = target.substringAfterLast(delimiter = PATH_SEPARATOR)

        val isNote = target.endsWith(
            suffix = MARKDOWN_EXTENSION,
            ignoreCase = true
        ) ||
            (name.isNotEmpty() && EXTENSION_MARK !in name)

        return isNote && !hasScheme(target = target)
    }

    private fun hasScheme(target: String): Boolean = SCHEME.containsMatchIn(input = target)

    private fun matches(
        markdown: String,
        pattern: Regex,
        kind: LinkKind
    ): List<NoteLink> = pattern
        .findAll(input = markdown)
        .mapNotNull { match ->
            val target = match.groups[TARGET_GROUP] ?: return@mapNotNull null

            NoteLink(
                targetStart = target.range.first,
                targetEnd = target.range.last + 1,
                target = target.value,
                kind = kind
            )
        }
        .toList()

    companion object {
        private const val EXTENSION_MARK = '.'
        private const val MARKDOWN_EXTENSION = ".md"
        private const val PATH_SEPARATOR = "/"
        private const val TARGET_GROUP = 1

        // https://, mailto:, obsidian:// and the like: a link that leaves the vault.
        private val SCHEME = Regex(pattern = """^[A-Za-z][A-Za-z0-9+.-]*:""")

        // [[target]], [[target#heading]], [[target^block]], [[target|alias]], with or without a leading "!" (embeds);
        // inside a table the alias's bar is escaped ([[target\|alias]]), and that backslash is not part of the target.
        private val WIKI_LINK =
            Regex(pattern = """\[\[([^\[\]|#^\n]+?)(?:[#^][^\[\]|\n]*)?(?:\\?\|[^\[\]\n]*)?]]""")

        // [text](<folder/Note name.md#heading>), with or without a title after the target; the text may hold escaped
        // brackets ([C\[1\]](<C[1].md>)), as the app writes for names a wiki link cannot hold.
        private val MARKDOWN_ANGLE_LINK = Regex(
            pattern = """\[(?:\\.|[^\]\\\n])*]\(<([^>\n#][^>\n]*?)(?:#[^>\n]*)?>(?:\s[^)\n]*)?\)"""
        )

        // [text](folder/Note%20name.md#heading), with or without a title: [text](Note.md "Title")
        private val MARKDOWN_LINK = Regex(
            pattern = """\[(?:\\.|[^\]\\\n])*]\(([^)<>\s#]+)(?:#[^)\s]*)?(?:\s+(?:"[^"\n]*"|'[^'\n]*'))?\)"""
        )
    }
}
