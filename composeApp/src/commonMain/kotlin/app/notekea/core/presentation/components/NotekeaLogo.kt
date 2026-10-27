package app.notekea.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaBrand

private const val DEFAULT_SIZE_DP = 64

/** Notekea's logo ([NotekeaBrand.Logo]) at [size]; decorative, so screen readers skip it beside the app's name. */
@Composable
fun NotekeaLogo(
    modifier: Modifier = Modifier,
    size: Dp = DEFAULT_SIZE_DP.dp
) {
    Image(imageVector = NotekeaBrand.Logo, contentDescription = null, modifier = modifier.size(size = size))
}

@Preview
@Composable
private fun NotekeaLogoPreview() {
    NotekeaPreviewThemes { NotekeaLogo() }
}
