package app.notekea.core.presentation.designsystem.components.markdown

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

/**
 * A Markdown link to `inline:<id>` is not a link: it stands for the inline content `<id>` the caller draws there (a
 * chat answer's citation chips), its text what a copy of the answer keeps.
 */
object InlineLinks {
    const val PREFIX = "inline:"

    /** The Markdown that draws the inline content [id], reading [text] where it cannot be drawn. */
    fun of(
        id: String,
        text: String
    ): String = "[$text]($PREFIX$id)"

    /** Appends the inline content [child] stands for; false for any other node, which is then drawn as usual. */
    fun annotate(
        builder: AnnotatedString.Builder,
        content: String,
        child: ASTNode
    ): Boolean {
        if (child.type != MarkdownElementTypes.INLINE_LINK) return false

        val destination = child
            .findChildOfType(type = MarkdownElementTypes.LINK_DESTINATION)
            ?.getTextInNode(allFileText = content)
            ?.toString()
            .orEmpty()

        if (!destination.startsWith(prefix = PREFIX)) return false

        val text = child
            .findChildOfType(type = MarkdownElementTypes.LINK_TEXT)
            ?.getTextInNode(allFileText = content)
            ?.toString()
            .orEmpty()
            .removeSurrounding(
                prefix = "[",
                suffix = "]"
            )
            .replace(
                oldValue = "\\",
                newValue = ""
            )

        builder.appendInlineContent(
            id = destination.removePrefix(prefix = PREFIX),
            alternateText = text.ifEmpty { " " }
        )

        return true
    }
}
