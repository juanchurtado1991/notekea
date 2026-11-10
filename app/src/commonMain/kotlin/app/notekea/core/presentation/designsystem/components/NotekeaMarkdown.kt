package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.rememberMarkdownState

private const val DARK_LUMINANCE = 0.5f
private const val PREVIEW_MARKDOWN =
    "# Arepas\n\nMade with **corn flour**, see [the recipe](https://example.com).\n\n" +
        "> Rest the dough for `5 minutes`.\n\n- [x] Buy flour\n- [ ] Buy cheese\n\n```kotlin\nval servings = 6\n```\n"
private const val PREVIEW_WIDTH_DP = 520

/**
 * Markdown rendered with the Notekea theme: callouts as cards ([MarkdownQuoteOrCallout]), selectable text unless
 * [isSelectable] is off, links opened by the system unless [onLinkClick] takes them, [onTaskClick] task boxes (which
 * one of how many), [marks] behind its texts but tables and code. Parsed at once: a lazy list needs its height first.
 */
@Composable
fun NotekeaMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    onLinkClick: ((String) -> Unit)? = null,
    marks: TextMarks? = null,
    isSelectable: Boolean = true,
    imagePainter: (String) -> Painter? = { null },
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
                    marks = marks
                )
            }
        } else {
            Box(modifier = modifier) {
                ThemedMarkdown(
                    content = prepared,
                    isDark = isDark,
                    imagePainter = imagePainter,
                    onTaskClick = onTaskClick,
                    marks = marks
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
        typography = notekeaMarkdownTypography(),
        modifier = Modifier.fillMaxWidth(),
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

/** The editor's type scale (28 / 20 / 16 sp headings, 16 sp text) so a note reads the same in both modes. */
@Composable
private fun notekeaMarkdownTypography(): MarkdownTypography {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val heading = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    // The theme's style, loaded once: building it again would look up its font files for every segment.
    val code = LocalNotekeaCodeTextStyle.current

    return markdownTypography(
        h1 = typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        h2 = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        h3 = heading,
        h4 = heading,
        h5 = heading,
        h6 = heading,
        text = typography.bodyLarge,
        code = code,
        inlineCode = typography.bodyLarge.copy(fontFamily = code.fontFamily),
        quote = typography.bodyLarge.copy(
            fontStyle = FontStyle.Italic,
            color = colors.onSurfaceVariant
        ),
        paragraph = typography.bodyLarge,
        ordered = typography.bodyLarge,
        bullet = typography.bodyLarge,
        list = typography.bodyLarge,
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
