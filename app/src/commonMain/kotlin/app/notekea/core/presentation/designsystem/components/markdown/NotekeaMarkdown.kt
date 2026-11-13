package app.notekea.core.presentation.designsystem.components.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.markdown.ReadingMarkdown
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalNotekeaCodeTextStyle
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.markdownInlineContent
import com.mikepenz.markdown.model.rememberMarkdownState

private const val DARK_LUMINANCE = 0.5f
private const val PREVIEW_MARKDOWN =
    "# Arepas\n\nMade with **corn flour**, see [the recipe](https://example.com).\n\n" +
        "> Rest the dough for `5 minutes`.\n\n- [x] Buy flour\n- [ ] Buy cheese\n\n```kotlin\nval servings = 6\n```\n"
private const val PREVIEW_WIDTH_DP = 520

/**
 * Markdown in the Notekea theme: callouts as cards, links opened by the system unless [onLinkClick] takes them,
 * [onTaskClick] task boxes (which of how many), [marks] behind its texts. A link to `inline:<id>` is drawn as
 * [inlineContent]'s `<id>` ([InlineLinks]); [isCompact] is the interface's type scale, for a panel and not a note.
 */
@Composable
fun NotekeaMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    onLinkClick: ((String) -> Unit)? = null,
    marks: TextMarks? = null,
    isSelectable: Boolean = true,
    imagePainter: (String) -> Painter? = { null },
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    isCompact: Boolean = false,
    onTaskClick: ((index: Int, count: Int) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < DARK_LUMINANCE
    val systemUriHandler = LocalUriHandler.current

    val uriHandler = remember(onLinkClick, systemUriHandler) {
        onLinkClick?.let { onClick ->
            // Every clicked link goes to onLinkClick, which decides what opens (a note, or the browser).
            object : UriHandler {
                override fun openUri(uri: String) = onClick(uri)
            }
        } ?: systemUriHandler
    }

    val readingMarkdown = remember { ReadingMarkdown() }

    val prepared = remember(content) { readingMarkdown.prepare(markdown = content) }

    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        if (isSelectable) {
            SelectionContainer(modifier = modifier) {
                ThemedMarkdown(
                    content = prepared,
                    isDark = isDark,
                    imagePainter = imagePainter,
                    onTaskClick = onTaskClick,
                    marks = marks,
                    inlineContent = inlineContent,
                    isCompact = isCompact
                )
            }
        } else {
            Box(modifier = modifier) {
                ThemedMarkdown(
                    content = prepared,
                    isDark = isDark,
                    imagePainter = imagePainter,
                    onTaskClick = onTaskClick,
                    marks = marks,
                    inlineContent = inlineContent,
                    isCompact = isCompact
                )
            }
        }
    }
}

@Composable
private fun ThemedMarkdown(
    content: String,
    isDark: Boolean,
    imagePainter: (String) -> Painter?,
    inlineContent: Map<String, InlineTextContent>,
    isCompact: Boolean,
    onTaskClick: ((index: Int, count: Int) -> Unit)?,
    marks: TextMarks?
) {
    val colors = MaterialTheme.colorScheme
    val currentOnTaskClick by rememberUpdatedState(newValue = onTaskClick)
    val hasTaskClicks = onTaskClick != null

    val readingMarkdown = remember { ReadingMarkdown() }

    val images = remember(imagePainter) { NotekeaImageTransformer(painterOf = imagePainter) }

    Markdown(
        imageTransformer = images,
        markdownState = rememberMarkdownState(
            content = content,
            immediate = true
        ),
        colors = markdownColor(
            text = colors.onSurface,
            codeBackground = colors.surfaceVariant,
            inlineCodeBackground = colors.surfaceVariant,
            dividerColor = colors.outline
        ),
        typography = notekeaMarkdownTypography(isCompact = isCompact),
        modifier = Modifier.fillMaxWidth(),
        annotator = remember { markdownAnnotator(annotate = InlineLinks::annotate) },
        inlineContent = markdownInlineContent(content = inlineContent),
        extendedSpans = markdownExtendedSpans(
            extendedSpans = remember(marks) { marks?.spans() }
        ),
        components = remember(isDark, hasTaskClicks) {
            val taskClicks = { index: Int, count: Int ->
                currentOnTaskClick?.invoke(index, count) ?: Unit
            }

            notekeaMarkdownComponents(
                readingMarkdown = readingMarkdown,
                isDark = isDark,
                onTaskClick = taskClicks.takeIf { hasTaskClicks }
            )
        }
    )
}

/**
 * The editor's type scale (28 / 20 / 16 sp headings, 16 sp text) so a note reads the same in both modes; when
 * [isCompact] the interface's own (a chat answer), its headings only bolder.
 */
@Composable
private fun notekeaMarkdownTypography(isCompact: Boolean): MarkdownTypography {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val text = if (isCompact) typography.bodyMedium else typography.bodyLarge
    val heading = text.copy(fontWeight = FontWeight.SemiBold)
    // The theme's style, loaded once: building it again would look up its font files for every segment.
    val code = LocalNotekeaCodeTextStyle.current

    val first = if (isCompact) typography.titleMedium else typography.displayMedium
    val second = if (isCompact) typography.titleMedium else typography.displaySmall
    val block = if (isCompact) code.copy(fontSize = text.fontSize) else code

    return markdownTypography(
        h1 = first,
        h2 = second,
        h3 = heading,
        h4 = heading,
        h5 = heading,
        h6 = heading,
        text = text,
        code = block,
        inlineCode = text.copy(fontFamily = code.fontFamily),
        quote = text.copy(
            fontStyle = FontStyle.Italic,
            color = colors.onSurfaceVariant
        ),
        paragraph = text,
        ordered = text,
        bullet = text,
        list = text,
        textLink = TextLinkStyles(style = SpanStyle(color = colors.primary))
    )
}

@Preview
@Composable
private fun NotekeaMarkdownPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaMarkdown(content = PREVIEW_MARKDOWN)
        }
    }
}
