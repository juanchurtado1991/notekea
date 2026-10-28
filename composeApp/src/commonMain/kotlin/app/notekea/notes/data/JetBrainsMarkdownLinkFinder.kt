package app.notekea.notes.data

import app.notekea.notes.domain.LinkFinder
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.NoteLink
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

/**
 * [LinkFinder] that uses the JetBrains Markdown parser only to know where code is (fenced and indented blocks, inline
 * code), since it does not know Obsidian's `[[wiki links]]`, and finds the links themselves with patterns. Markdown
 * links count only when they point to a `.md` file and have no URL scheme, so web pages are never rewritten.
 */
class JetBrainsMarkdownLinkFinder : LinkFinder {
    private val parser = MarkdownParser(
        flavour = GFMFlavourDescriptor(),
        assertionsEnabled = false,
        cancellationToken = CancellationToken.NonCancellable
    )

    override fun find(
        markdown: String
    ): List<NoteLink> {
        val links = matches(markdown = markdown, pattern = WIKI_LINK, kind = LinkKind.WIKI) +
            matches(markdown = markdown, pattern = MARKDOWN_ANGLE_LINK, kind = LinkKind.MARKDOWN_ANGLE) +
            matches(markdown = markdown, pattern = MARKDOWN_LINK, kind = LinkKind.MARKDOWN_ENCODED).filterNot {
                SCHEME_SEPARATOR in
                    it.target
            }
        // The Markdown tree, which finds the code the links must not be in, is built only when there are links.
        if (links.isEmpty()) return emptyList()
        val code = codeRanges(node = parser.buildMarkdownTreeFromString(text = markdown as CharSequence))
        return links.filterNot { link -> code.any { link.targetStart in it } }.sortedBy { it.targetStart }
    }

    private fun codeRanges(
        node: ASTNode
    ): List<IntRange> = if (node.type in CODE_TYPES) {
        listOf(node.startOffset until node.endOffset)
    } else {
        node.children.flatMap { codeRanges(node = it) }
    }

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
        }.toList()

    companion object {
        private const val SCHEME_SEPARATOR = "://"
        private const val TARGET_GROUP = 1
        private val CODE_TYPES =
            setOf(MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_SPAN)

        // [[target]], [[target#heading]], [[target^block]], [[target|alias]], with or without a leading "!" (embeds).
        private val WIKI_LINK = Regex(pattern = """\[\[([^\[\]|#^\n]+?)(?:[#^][^\[\]|\n]*)?(?:\|[^\[\]\n]*)?]]""")

        // [text](<folder/Note name.md#heading>)
        private val MARKDOWN_ANGLE_LINK =
            Regex(pattern = """\[[^\]\n]*]\(<([^>\n]+?\.md)(?:#[^>\n]*)?>""", option = RegexOption.IGNORE_CASE)

        // [text](folder/Note%20name.md#heading)
        private val MARKDOWN_LINK =
            Regex(pattern = """\[[^\]\n]*]\(([^)<>\s#]+?\.md)(?:#[^)\s]*)?\)""", option = RegexOption.IGNORE_CASE)
    }
}
