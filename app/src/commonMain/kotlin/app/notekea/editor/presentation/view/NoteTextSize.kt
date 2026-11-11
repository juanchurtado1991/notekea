package app.notekea.editor.presentation.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.times
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalAppPreferences

/**
 * [content], the note's page, with its text at Settings → Editor's size: every style of the theme scaled by it, so
 * headings, code and the body keep their proportions. At the default size nothing changes.
 */
@Composable
internal fun NoteTextSize(content: @Composable () -> Unit) {
    val size = LocalAppPreferences.current.textSize
    val typography = MaterialTheme.typography

    if (size == AppPreferences.DEFAULT_TEXT_SIZE) return content()

    val scaled = remember(typography, size) {
        typography.scaledBy(factor = size.toFloat() / AppPreferences.DEFAULT_TEXT_SIZE)
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography = scaled,
        content = content
    )
}

private fun Typography.scaledBy(factor: Float): Typography = Typography(
    displayLarge = displayLarge.scaledBy(factor = factor),
    displayMedium = displayMedium.scaledBy(factor = factor),
    displaySmall = displaySmall.scaledBy(factor = factor),
    headlineLarge = headlineLarge.scaledBy(factor = factor),
    headlineMedium = headlineMedium.scaledBy(factor = factor),
    headlineSmall = headlineSmall.scaledBy(factor = factor),
    titleLarge = titleLarge.scaledBy(factor = factor),
    titleMedium = titleMedium.scaledBy(factor = factor),
    titleSmall = titleSmall.scaledBy(factor = factor),
    bodyLarge = bodyLarge.scaledBy(factor = factor),
    bodyMedium = bodyMedium.scaledBy(factor = factor),
    bodySmall = bodySmall.scaledBy(factor = factor),
    labelLarge = labelLarge.scaledBy(factor = factor),
    labelMedium = labelMedium.scaledBy(factor = factor),
    labelSmall = labelSmall.scaledBy(factor = factor)
)

private fun TextStyle.scaledBy(factor: Float): TextStyle = copy(
    fontSize = fontSize.scaledBy(factor = factor),
    lineHeight = lineHeight.scaledBy(factor = factor)
)

private fun TextUnit.scaledBy(factor: Float): TextUnit = if (isSpecified) factor * this else this

@Preview
@Composable
private fun NoteTextSizePreview() {
    NotekeaPreviewThemes {
        NoteTextSize {
            Text(
                text = PREVIEW_TEXT,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private const val PREVIEW_TEXT = "Mix the water and salt, then add the flour slowly."
