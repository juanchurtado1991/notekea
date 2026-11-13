package app.notekea.core.presentation.designsystem.components.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalReduceMotion
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import kotlinx.coroutines.delay

private const val BLINKS = 3
private const val BLINK_HALF_MILLIS = 400
private const val FILL_ALPHA = 0.16f
private const val PREVIEW_TEXT = "Access token"
private const val PREVIEW_SERIAL = 1

/**
 * A part of a screen the person is shown (a setting the chat names): scrolled into view, then outlined and tinted in
 * the theme's color, blinking three times (steady for as long where motion is reduced). Each new [shownAt] shows it
 * again; null shows nothing.
 */
@Composable
fun NotekeaSpotlight(
    shownAt: Int?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val requester = remember { BringIntoViewRequester() }
    val glow = remember { Animatable(initialValue = 0f) }
    val isMotionReduced = LocalReduceMotion.current
    val color = MaterialTheme.colorScheme.primary

    LaunchedEffect(shownAt) {
        if (shownAt == null) return@LaunchedEffect
        requester.bringIntoView()

        if (isMotionReduced) {
            glow.snapTo(targetValue = 1f)
            delay(timeMillis = BLINKS * 2L * BLINK_HALF_MILLIS)
        } else {
            repeat(times = BLINKS) {
                glow.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = BLINK_HALF_MILLIS)
                )

                glow.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = BLINK_HALF_MILLIS)
                )
            }
        }

        glow.snapTo(targetValue = 0f)
    }

    Box(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester = requester)
            .drawWithContent {
                drawContent()
                val amount = glow.value
                if (amount <= 0f) return@drawWithContent

                // Around the setting, a little outside it, so its own border stays visible.
                val outside = NotekeaDimensions.SpacingXs.toPx()
                val corner = CornerRadius(x = NotekeaDimensions.ControlRadius.toPx())

                val topLeft = Offset(
                    x = -outside,
                    y = -outside
                )

                val area = Size(
                    width = size.width + 2 * outside,
                    height = size.height + 2 * outside
                )

                drawRoundRect(
                    color = color.copy(alpha = FILL_ALPHA * amount),
                    topLeft = topLeft,
                    size = area,
                    cornerRadius = corner
                )

                drawRoundRect(
                    color = color.copy(alpha = amount),
                    topLeft = topLeft,
                    size = area,
                    cornerRadius = corner,
                    style = Stroke(width = NotekeaDimensions.BorderWidth.toPx() * 2)
                )
            }
    ) { content() }
}

@Preview
@Composable
private fun NotekeaSpotlightPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.padding(all = NotekeaDimensions.SpacingL)) {
            NotekeaSpotlight(shownAt = PREVIEW_SERIAL) { Text(text = PREVIEW_TEXT) }
        }
    }
}
