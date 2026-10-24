package app.notekea.core.presentation.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaTheme

/**
 * Wraps a `@Preview` so the content renders in the light theme above the dark one (stacked, because Android Studio
 * limits the preview width but not its height). It sets the theme explicitly instead of relying on
 * `@PreviewLightDark`, whose system dark mode does not reach desktop previews.
 */
@Composable
fun NotekeaPreviewThemes(
    content: @Composable () -> Unit
) {
    Column {
        listOf(false, true).forEach { darkTheme ->
            NotekeaTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingL)) { content() }
                }
            }
        }
    }
}
