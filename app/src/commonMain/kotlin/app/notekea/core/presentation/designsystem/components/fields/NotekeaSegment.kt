package app.notekea.core.presentation.designsystem.components.fields

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** One choice of a [NotekeaSegmentedControl]: its [label], and its [icon] when it has one. */
@Immutable
data class NotekeaSegment(
    val label: String,
    val icon: ImageVector? = null
)
