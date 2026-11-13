package app.notekea.core.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaTooltip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalReduceMotion
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.motionTween

private const val APPEAR_FROM = 0.4f
private const val HOVER_ALPHA = 0.28f
private const val REST_ALPHA = 0.14f
private const val MAX_SNIPPET = 180
private const val ELLIPSIS = "…"
private const val CHIP_RADIUS_DP = 9
private const val CHIP_HEIGHT_DP = 18
private const val PREVIEW_SNIPPET =
    "Two cups of pre-cooked corn flour and two and a half cups of warm water."

/**
 * A citation of an answer, `[number]` on a pill in the theme's color, which grows in as it appears. Hovering shows
 * the cited passage ([snippet]) and strengthens the pill; a click opens it ([onClick]).
 */
@Composable
fun NotekeaCitationChip(
    number: Int,
    snippet: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val interactions = remember { MutableInteractionSource() }
    val isHovered by interactions.collectIsHoveredAsState()
    val reduceMotion = LocalReduceMotion.current
    val grow = remember { Animatable(initialValue = if (reduceMotion) 1f else APPEAR_FROM) }
    val spec = motionTween<Float>(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)

    LaunchedEffect(Unit) {
        grow.animateTo(
            targetValue = 1f,
            animationSpec = spec
        )
    }

    val isLong = snippet.length > MAX_SNIPPET
    val shown = if (isLong) snippet.take(n = MAX_SNIPPET).trimEnd() + ELLIPSIS else snippet

    NotekeaTooltip(label = shown) {
        Box(
            modifier = modifier
                .scale(scale = grow.value)
                .defaultMinSize(
                    minWidth = CHIP_HEIGHT_DP.dp,
                    minHeight = CHIP_HEIGHT_DP.dp
                )
                .background(
                    color = colors.primary.copy(alpha = if (isHovered) HOVER_ALPHA else REST_ALPHA),
                    shape = RoundedCornerShape(size = CHIP_RADIUS_DP.dp)
                )
                .hoverable(interactionSource = interactions)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .clickable(
                    interactionSource = interactions,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(horizontal = NotekeaDimensions.SpacingXs),
            contentAlignment = Alignment.Center
        ) {
            // The line's room above and below the digits is trimmed, so they sit in the middle of the circle.
            Text(
                text = number.toString(),
                style = MaterialTheme
                    .typography
                    .labelSmall
                    .copy(
                        lineHeight = MaterialTheme.typography.labelSmall.fontSize,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                fontWeight = FontWeight.SemiBold,
                color = colors.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaCitationChipPreview() {
    NotekeaPreviewThemes {
        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
            (1..3).forEach {
                NotekeaCitationChip(
                    number = it,
                    snippet = PREVIEW_SNIPPET,
                    onClick = {}
                )
            }
        }
    }
}
