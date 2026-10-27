package app.notekea.core.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_CONTENT = "What the section holds"

/**
 * What a section holds under its header ([NotekeaSectionHeader], the properties' title): shown while [isOpen], and
 * sliding open or shut with a fade when it changes, so the eye follows what moved.
 */
@Composable
fun NotekeaFold(
    isOpen: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val spec = { tween<Float>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS) }
    AnimatedVisibility(
        visible = isOpen,
        modifier = modifier,
        enter = expandVertically(animationSpec = tween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)) +
            fadeIn(animationSpec = spec()),
        exit = shrinkVertically(animationSpec = tween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)) +
            fadeOut(animationSpec = spec())
    ) {
        Column { content() }
    }
}

@Preview
@Composable
private fun NotekeaFoldPreview() {
    NotekeaPreviewThemes {
        NotekeaFold(isOpen = true) { Text(text = PREVIEW_CONTENT) }
    }
}
