package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaBrand

private const val DEFAULT_SIZE_DP = 64

/**
 * Notekea's logo at [size], in the color theme's colors ([NotekeaBrand.logoOf]): the note in the primary container
 * color, the mark in the primary color and the folded corner in the accent; decorative, so screen readers skip it
 * beside the app's name.
 */
@Composable
fun NotekeaLogo(
    modifier: Modifier = Modifier,
    size: Dp = DEFAULT_SIZE_DP.dp
) {
    val colors = MaterialTheme.colorScheme
    val logo = remember(colors.primaryContainer, colors.primary, colors.tertiary) {
        NotekeaBrand.logoOf(note = colors.primaryContainer, mark = colors.primary, fold = colors.tertiary)
    }
    Image(
        imageVector = logo,
        contentDescription = null,
        modifier = modifier.size(size = size)
    )
}

@Preview
@Composable
private fun NotekeaLogoPreview() {
    NotekeaPreviewThemes { NotekeaLogo() }
}
