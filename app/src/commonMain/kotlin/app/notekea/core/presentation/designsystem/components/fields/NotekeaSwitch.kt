package app.notekea.core.presentation.designsystem.components.fields

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaColors
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.motionTween

private const val PREVIEW_LABEL = "Open the last vault at startup"
private const val THUMB_DP = 14
private const val THUMB_INSET_DP = 2
private const val THUMB_SHADOW_DP = 1
private const val TRACK_HEIGHT_DP = 18
private const val TRACK_WIDTH_DP = 32

/**
 * An on/off setting as macOS draws one beside its label (redesign 5A): a 32 × 18 track, the theme's color when on,
 * with a white thumb that slides unless motion is reduced. [label] is for screen readers.
 */
@Composable
fun NotekeaSwitch(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val travel = (TRACK_WIDTH_DP - THUMB_DP - THUMB_INSET_DP * 2).dp

    val thumbOffset by animateDpAsState(
        targetValue = if (isOn) travel else 0.dp,
        animationSpec = motionTween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
    )

    val track by animateColorAsState(
        targetValue = if (isOn) colors.primary else colors.outline,
        animationSpec = motionTween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
    )

    Box(
        modifier = modifier
            .size(
                width = TRACK_WIDTH_DP.dp,
                height = TRACK_HEIGHT_DP.dp
            )
            .background(
                color = track,
                shape = CircleShape
            )
            // Before what takes the focus, which it then sees: after it the ring never showed.
            .focusBorder(
                color = colors.primary,
                shape = CircleShape
            )
            .toggleable(
                value = isOn,
                role = Role.Switch,
                onValueChange = onToggle
            )
            .semantics { contentDescription = label }
            .padding(all = THUMB_INSET_DP.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(size = THUMB_DP.dp)
                .shadow(
                    elevation = THUMB_SHADOW_DP.dp,
                    shape = CircleShape
                )
                .background(
                    color = NotekeaColors.switchThumb,
                    shape = CircleShape
                )
        )
    }
}

@Preview
@Composable
private fun NotekeaSwitchPreview() {
    NotekeaPreviewThemes {
        NotekeaSwitch(
            isOn = true,
            onToggle = {},
            label = PREVIEW_LABEL
        )
    }
}
