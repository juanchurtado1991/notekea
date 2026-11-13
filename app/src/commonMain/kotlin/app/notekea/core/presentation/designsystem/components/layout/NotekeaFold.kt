package app.notekea.core.presentation.designsystem.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.motionTween

private const val PREVIEW_CONTENT = "What the section holds"

/**
 * What a section holds under its header (`NotekeaSectionHeader`, the properties' title): shown while [isOpen], and
 * sliding open or shut with a fade when it changes, so the eye follows what moved.
 */
@Composable
fun NotekeaFold(
    isOpen: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val fade = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
    val fold = motionTween<IntSize>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)

    AnimatedVisibility(
        visible = isOpen,
        modifier = modifier,
        enter = expandVertically(animationSpec = fold) + fadeIn(animationSpec = fade),
        exit = shrinkVertically(animationSpec = fold) + fadeOut(animationSpec = fade)
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
