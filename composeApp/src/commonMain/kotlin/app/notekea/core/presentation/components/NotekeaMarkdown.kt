package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.notekeaCodeTextStyle
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.elements.MarkdownCheckBox
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes

private const val DARK_LUMINANCE = 0.5f
private const val PREVIEW_MARKDOWN =
    "# Arepas\n\nMade with **corn flour**, see [the recipe](https://example.com).\n\n" +
        "> Rest the dough for `5 minutes`.\n\n- [x] Buy flour\n- [ ] Buy cheese\n\n```kotlin\nval servings = 6\n```\n"
private const val PREVIEW_WIDTH_DP = 520

/**
 * Markdown rendered with the Notekea theme (the note's Read mode, later chat answers): headings on the type scale,
 * links in the primary color, code in the bundled monospaced face, and code blocks colored by language like GitHub's
 * preview, with the syntax theme following the app's light or dark theme. Text can be selected and copied. Clicked
 * links open with the system (a web page in the browser) unless [onLinkClick] takes them all.
 */
@Composable
fun NotekeaMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    onLinkClick: ((String) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < DARK_LUMINANCE
    val highlights = remember(isDark) { Highlights.Builder().theme(SyntaxThemes.default(darkMode = isDark)) }
    val systemUriHandler = LocalUriHandler.current
    val uriHandler = remember(onLinkClick, systemUriHandler) {
        onLinkClick?.let { onClick ->
            // Every clicked link goes to onLinkClick, which decides what opens (a note, or the browser).
            object : UriHandler {
                override fun openUri(
                    uri: String
                ) = onClick(uri)
            }
        } ?: systemUriHandler
    }
    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        SelectionContainer(modifier = modifier) { ThemedMarkdown(content = content, highlights = highlights) }
    }
}

@Composable
private fun ThemedMarkdown(
    content: String,
    highlights: Highlights.Builder
) {
    val colors = MaterialTheme.colorScheme
    Markdown(
        content = content,
        colors = markdownColor(
            text = colors.onSurface,
            codeBackground = colors.surfaceVariant,
            inlineCodeBackground = colors.surfaceVariant,
            dividerColor = colors.outline
        ),
        typography = notekeaMarkdownTypography(),
        modifier = Modifier.fillMaxWidth(),
        components = markdownComponents(
            codeBlock = { MarkdownHighlightedCodeBlock(it.content, it.node, it.typography.code, highlights) },
            codeFence = { MarkdownHighlightedCodeFence(it.content, it.node, it.typography.code, highlights) },
            checkbox = { MarkdownCheckBox(it.content, it.node, it.typography.text) }
        )
    )
}

/** The editor's type scale (28 / 20 / 16 sp headings, 16 sp text) so a note reads the same in both modes. */
@Composable
private fun notekeaMarkdownTypography(): MarkdownTypography {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val heading = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    val code = notekeaCodeTextStyle()
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
        quote = typography.bodyLarge.copy(fontStyle = FontStyle.Italic, color = colors.onSurfaceVariant),
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
