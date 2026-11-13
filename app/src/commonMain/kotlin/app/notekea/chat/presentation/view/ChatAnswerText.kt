package app.notekea.chat.presentation.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.core.presentation.designsystem.components.NotekeaCitationChip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalReduceMotion

private const val CITATION_PREFIX = "citation:"
private const val CARET = "caret"
private const val CARET_BLINK_MILLIS = 530
private const val CARET_WIDTH_DP = 2
private val CITATION = Regex(pattern = """\[(\d+)]""")
private val WIKI_LINK = Regex(pattern = """\[\[(?:[^\]|]*\|)?([^\]]+)]]""")

/**
 * An answer's text with each `[n]` drawn as a citation chip of [sources] (`[n]` is the source numbered n), a wiki link
 * as its name, and while [isWriting] a blinking caret where the model writes next.
 */
@Composable
internal fun ChatAnswerText(
    text: String,
    sources: List<AnswerSource>,
    isWriting: Boolean,
    onSourceClick: (AnswerSource) -> Unit,
    modifier: Modifier = Modifier
) {
    val byNumber = remember(sources) {
        sources.associateBy { it.number }
    }

    val shown = remember(text, byNumber, isWriting) {
        annotated(
            text = text,
            numbers = byNumber.keys,
            isWriting = isWriting
        )
    }

    val inline = byNumber.map { (number, source) ->
        "$CITATION_PREFIX$number" to InlineTextContent(
            placeholder = Placeholder(
                width = 1.45.em,
                height = 1.3.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            NotekeaCitationChip(
                number = number,
                snippet = source.text,
                onClick = { onSourceClick(source) }
            )
        }
    }.toMap() + (CARET to caretContent())

    Text(
        text = shown,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        inlineContent = inline
    )
}

/** [text] with its citations of [numbers] as inline chips, its wiki links as their names, and the caret if writing. */
private fun annotated(
    text: String,
    numbers: Set<Int>,
    isWriting: Boolean
): AnnotatedString = buildAnnotatedString {
    val plain = WIKI_LINK
        .replace(input = text) {
            it.groupValues[1]
        }

    var at = 0

    CITATION.findAll(input = plain).forEach { match ->
        val number = match.groupValues[1].toInt()
        if (number !in numbers) return@forEach

        append(
            plain.substring(
                startIndex = at,
                endIndex = match.range.first
            )
        )

        appendInlineContent(
            id = "$CITATION_PREFIX$number",
            alternateText = match.value
        )

        at = match.range.last + 1
    }

    append(plain.substring(startIndex = at))

    if (isWriting) {
        appendInlineContent(
            id = CARET,
            alternateText = " "
        )
    }
}

/** The caret of an answer being written: a thin bar in the theme's color, blinking unless motion is reduced. */
@Composable
private fun caretContent(): InlineTextContent = InlineTextContent(
    placeholder = Placeholder(
        width = 0.5.em,
        height = 1.1.em,
        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
    )
) {
    val reduceMotion = LocalReduceMotion.current
    val blink = rememberInfiniteTransition()

    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = CARET_BLINK_MILLIS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .width(width = CARET_WIDTH_DP.dp)
            .fillMaxSize()
            .alpha(alpha = alpha)
            .background(color = MaterialTheme.colorScheme.primary)
    )
}

@Preview
@Composable
private fun ChatAnswerTextPreview() {
    NotekeaPreviewThemes {
        ChatAnswerText(
            text = ChatPreviewData.writing.answer,
            sources = ChatPreviewData.sources,
            isWriting = true,
            onSourceClick = {}
        )
    }
}
