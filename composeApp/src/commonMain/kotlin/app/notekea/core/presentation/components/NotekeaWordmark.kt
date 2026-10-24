package app.notekea.core.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val LETTER_SPACING_SP = -0.5f
private const val WORDMARK_HEAD = "Note"
private const val WORDMARK_SIZE_SP = 40
private const val WORDMARK_TAIL = "kea"

/**
 * The "Notekea" logotype: "Note" in cobalt and "kea" in tangerine, the kea's underwing. A brand logotype, so its
 * tangerine half is exempt from the WCAG text contrast minimum; it is never used for regular text.
 */
@Composable
fun NotekeaWordmark(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = colors.primary)) { append(WORDMARK_HEAD) }
            withStyle(style = SpanStyle(color = colors.tertiary)) { append(WORDMARK_TAIL) }
        },
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = WORDMARK_SIZE_SP.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LETTER_SPACING_SP.sp
        )
    )
}

@Preview
@Composable
private fun NotekeaWordmarkPreview() {
    NotekeaPreviewThemes {
        NotekeaWordmark()
    }
}
