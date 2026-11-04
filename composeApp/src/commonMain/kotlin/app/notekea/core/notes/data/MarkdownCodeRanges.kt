package app.notekea.core.notes.data

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

/**
 * Where Markdown text has code (fenced and indented blocks, inline code), read by the JetBrains Markdown parser, for
 * the finders that must leave code alone ([JetBrainsMarkdownLinkFinder], [JetBrainsMarkdownMentionFinder]).
 */
class MarkdownCodeRanges {
    private val parser = MarkdownParser(
        flavour = GFMFlavourDescriptor(),
        assertionsEnabled = false,
        cancellationToken = CancellationToken.NonCancellable
    )

    fun of(
        markdown: String
    ): List<IntRange> = codeRanges(node = parser.buildMarkdownTreeFromString(text = markdown as CharSequence))

    private fun codeRanges(
        node: ASTNode
    ): List<IntRange> = if (node.type in CODE_TYPES) {
        listOf(node.startOffset until node.endOffset)
    } else {
        node.children.flatMap { codeRanges(node = it) }
    }

    private companion object {
        val CODE_TYPES =
            setOf(MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_SPAN)
    }
}
