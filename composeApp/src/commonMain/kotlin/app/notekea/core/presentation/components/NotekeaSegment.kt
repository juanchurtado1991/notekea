package app.notekea.core.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** One choice of a [NotekeaSegmentedControl]: its [icon] and [label]. */
@Immutable
data class NotekeaSegment(
    val icon: ImageVector,
    val label: String
)
