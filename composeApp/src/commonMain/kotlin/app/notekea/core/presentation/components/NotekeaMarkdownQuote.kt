package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.markdown.CalloutFold
import app.notekea.core.domain.markdown.CalloutKind
import app.notekea.core.domain.markdown.CalloutParser
import app.notekea.core.domain.markdown.ReadingMarkdown
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.MarkdownElement
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private const val PREVIEW_MARKDOWN = "> [!tip] Soak the beans\n>\n> Overnight, in plenty of water.\n\n" +
    "> [!warning]- Hot oil\n>\n> Never add water.\n\n> A plain quote.\n"
private const val PREVIEW_WIDTH_DP = 480
private const val QUOTE_BAR_DP = 3

/** Quotes inside a quote, drawn by [MarkdownQuoteOrCallout] again: plain ones and GitHub-style alerts. */
private val QUOTES = setOf(MarkdownElementTypes.BLOCK_QUOTE, GFMElementTypes.ALERT)

/** What a quote holds besides its blocks: line ends and the `>` marks. */
private val QUOTE_MARKS = setOf(MarkdownTokenTypes.EOL, MarkdownTokenTypes.BLOCK_QUOTE)

/**
 * A Markdown quote in [NotekeaMarkdown]: an Obsidian callout (`> [!type]`, `ROADMAP.md` 1.7) when its first paragraph
 * is a callout header, drawn as a [NotekeaCallout] with the type's icon and color and foldable with `-` / `+`;
 * otherwise a plain quote with a bar. Quotes and callouts inside it are drawn the same way, at any depth. The header
 * must be a paragraph of its own, as [ReadingMarkdown] leaves it; GitHub-style alerts (`> [!NOTE]`, which the renderer
 * reads apart) are drawn as callouts too.
 */
@Composable
internal fun MarkdownQuoteOrCallout(
    content: String,
    node: ASTNode
) {
    val parser = remember { CalloutParser() }
    // The header is the first paragraph, or the title token of a GitHub-style alert (`> [!NOTE]` with no title).
    val headerNode = node.children.firstOrNull {
        it.type == MarkdownElementTypes.PARAGRAPH || it.type == GFMTokenTypes.ALERT_TITLE
    }
    val header = headerNode?.let { parser.header(line = it.getTextInNode(content).toString()) }
    if (header == null) {
        val bar = MaterialTheme.colorScheme.outline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = bar,
                        start = Offset.Zero,
                        end = Offset(x = 0f, y = size.height),
                        strokeWidth = QUOTE_BAR_DP.dp.toPx()
                    )
                }
                .padding(start = NotekeaDimensions.SpacingM)
        ) {
            QuoteChildren(content = content, node = node, skipped = null)
        }
        return
    }
    var isExpanded by remember(node.startOffset) { mutableStateOf(value = header.fold != CalloutFold.COLLAPSED) }
    NotekeaCallout(
        title = header.title,
        icon = calloutIcon(kind = header.kind),
        color = calloutColor(kind = header.kind),
        isExpanded = isExpanded,
        onToggle = if (header.fold == CalloutFold.NONE) null else ({ isExpanded = !isExpanded })
    ) {
        QuoteChildren(content = content, node = node, skipped = headerNode)
    }
}

/** The blocks of a quote, without its `>` marks, empty lines and the [skipped] header; nested quotes recurse. */
@Composable
private fun QuoteChildren(
    content: String,
    node: ASTNode,
    skipped: ASTNode?
) {
    val components = LocalMarkdownComponents.current
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        node.children.forEach { child ->
            key(child.startOffset) {
                when {
                    child === skipped || child.type in QUOTE_MARKS -> Unit

                    child.type in QUOTES -> MarkdownQuoteOrCallout(content = content, node = child)

                    else -> MarkdownElement(
                        node = child,
                        components = components,
                        content = content,
                        includeSpacer = false
                    )
                }
            }
        }
    }
}

/** Callouts are colored by how much they ask for attention; the icon tells the type apart. */
@Composable
private fun calloutColor(
    kind: CalloutKind
): Color {
    val colors = MaterialTheme.colorScheme
    return when (kind) {
        CalloutKind.WARNING -> colors.tertiary
        CalloutKind.DANGER, CalloutKind.FAILURE, CalloutKind.BUG -> colors.error
        CalloutKind.QUOTE -> colors.onSurfaceVariant
        else -> colors.primary
    }
}

private fun calloutIcon(
    kind: CalloutKind
): ImageVector = when (kind) {
    CalloutKind.NOTE -> NotekeaIcons.CalloutNote
    CalloutKind.ABSTRACT -> NotekeaIcons.CalloutAbstract
    CalloutKind.INFO -> NotekeaIcons.CalloutInfo
    CalloutKind.TODO -> NotekeaIcons.CalloutTodo
    CalloutKind.TIP -> NotekeaIcons.CalloutTip
    CalloutKind.SUCCESS -> NotekeaIcons.CalloutSuccess
    CalloutKind.QUESTION -> NotekeaIcons.CalloutQuestion
    CalloutKind.WARNING -> NotekeaIcons.CalloutWarning
    CalloutKind.FAILURE -> NotekeaIcons.CalloutFailure
    CalloutKind.DANGER -> NotekeaIcons.CalloutDanger
    CalloutKind.BUG -> NotekeaIcons.CalloutBug
    CalloutKind.EXAMPLE -> NotekeaIcons.CalloutExample
    CalloutKind.QUOTE -> NotekeaIcons.CalloutQuote
}

@Preview
@Composable
private fun MarkdownQuoteOrCalloutPreview() {
    NotekeaPreviewThemes {
        NotekeaMarkdown(content = PREVIEW_MARKDOWN, modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp))
    }
}
