package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import app.notekea.core.domain.markdown.ReadingMarkdown
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.editor_task
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCode
import com.mikepenz.markdown.m3.elements.MarkdownCheckBox
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.jetbrains.compose.resources.stringResource

private const val DONE_STATE = 'x'

/**
 * How Notekea draws Markdown: code colored by language without the `>` marks of the quotes around it, checkboxes,
 * callouts as cards, and tables with their columns lined up ([NotekeaMarkdownTable]).
 */
internal fun notekeaMarkdownComponents(
    readingMarkdown: ReadingMarkdown,
    isDark: Boolean,
    onTaskClick: ((index: Int, count: Int) -> Unit)?
) = markdownComponents(
    codeBlock = {
        val depth = quoteDepth(node = it.node)

        MarkdownCodeBlock(it.content, it.node, it.typography.code) { code, language, style ->
            val unquoted = readingMarkdown.unquoted(
                code = code,
                depth = depth
            )

            HighlightedCode(
                code = unquoted,
                style = style,
                isDark = isDark,
                language = language
            )
        }
    },
    codeFence = {
        val depth = quoteDepth(node = it.node)

        MarkdownCodeFence(it.content, it.node, it.typography.code) { code, language, style ->
            val unquoted = readingMarkdown.unquoted(
                code = code,
                depth = depth
            )

            HighlightedCode(
                code = unquoted,
                style = style,
                isDark = isDark,
                language = language
            )
        }
    },
    checkbox = {
        if (onTaskClick == null) {
            MarkdownCheckBox(it.content, it.node, it.typography.text)
        } else {
            TaskBox(
                content = it.content,
                node = it.node,
                onClick = onTaskClick
            )
        }
    },
    blockQuote = {
        MarkdownQuoteOrCallout(
            content = it.content,
            node = it.node
        )
    },
    alert = {
        MarkdownQuoteOrCallout(
            content = it.content,
            node = it.node
        )
    },
    table = {
        NotekeaMarkdownTable(
            content = it.content,
            node = it.node,
            style = it.typography.table
        )
    }
)

/** A task's checkbox that takes clicks: [onClick] gets which checkbox of the content it is, and how many there are. */
@Composable
private fun TaskBox(
    content: String,
    node: ASTNode,
    onClick: (index: Int, count: Int) -> Unit
) {
    val boxes = remember(node) { checkBoxesOf(node = node) }

    val isDone = node
        .getTextInNode(allFileText = content)
        .contains(
            char = DONE_STATE,
            ignoreCase = true
        )

    NotekeaCheckbox(
        checked = isDone,
        onClick = { onClick(boxes.indexOf(element = node), boxes.size) },
        label = stringResource(resource = Res.string.editor_task),
        modifier = Modifier
            .padding(end = NotekeaDimensions.SpacingXs)
            .pointerHoverIcon(icon = PointerIcon.Hand)
    )
}

/** Every checkbox of the document [node] is in, in the text's order. */
private fun checkBoxesOf(node: ASTNode): List<ASTNode> {
    val root = generateSequence(seed = node) { it.parent }.last()
    val boxes = mutableListOf<ASTNode>()
    val pending = ArrayDeque(elements = listOf(root))

    while (pending.isNotEmpty()) {
        val next = pending.removeFirst()

        if (next.type == GFMTokenTypes.CHECK_BOX) boxes += next

        pending.addAll(
            index = 0,
            elements = next.children
        )
    }

    return boxes
}

/**
 * One code block colored by language. The renderer colors every block in the background at the same time, and a
 * highlights builder is mutable: shared by two blocks (or kept while a block's text changes), it mixes their texts and
 * returns color ranges past the end of the code, which crashed the app. So each block and text gets its own.
 */
@Composable
private fun HighlightedCode(
    code: String,
    style: TextStyle,
    isDark: Boolean,
    language: String?
) {
    val highlights = remember(code, isDark) {
        Highlights.Builder().theme(SyntaxThemes.default(darkMode = isDark))
    }

    MarkdownHighlightedCode(
        code = code,
        language = language,
        style = style,
        highlightsBuilder = highlights
    )
}

/** How many quotes or callouts hold [node]: the renderer leaves that many `>` marks in a code block's lines. */
private fun quoteDepth(node: ASTNode): Int = generateSequence(seed = node.parent) {
    it.parent
}.count {
    it.type == MarkdownElementTypes.BLOCK_QUOTE || it.type == GFMElementTypes.ALERT
}
