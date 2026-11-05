package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val INSET_DP = 2
private const val TRACK_RADIUS_DP = 7
private const val THUMB_RADIUS_DP = 5

internal val segmentShape = RoundedCornerShape(size = THUMB_RADIUS_DP.dp)

/** The muted track every selector of the app sits on, as macOS draws segmented controls. */
internal fun Modifier.segmentTrack(
    colors: ColorScheme
): Modifier = clip(shape = RoundedCornerShape(size = TRACK_RADIUS_DP.dp))
    .background(color = colors.surfaceVariant)
    .padding(all = INSET_DP.dp)

/** A selector's segment on its track: the selected one is raised on the surface. */
internal fun Modifier.segmentThumb(
    isSelected: Boolean,
    colors: ColorScheme
): Modifier = clip(shape = segmentShape)
    .background(color = if (isSelected) colors.surface else Color.Transparent)
    .border(
        width = NotekeaDimensions.BorderWidth,
        color = if (isSelected) colors.outlineVariant else Color.Transparent,
        shape = segmentShape
    )
