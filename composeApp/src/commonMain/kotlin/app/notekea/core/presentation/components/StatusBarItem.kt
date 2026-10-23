package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import app.notekea.core.presentation.theme.NotekeaDimensions

/** Clickable status bar entry: a colored state dot and a short label ("Indexed 1,204 notes"). */
@Composable
fun StatusBarItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indicator: StatusIndicator = StatusIndicator.IDLE
) {
    Row(
        modifier = modifier
            .height(NotekeaDimensions.StatusBarHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(NotekeaDimensions.SpacingS)
                .background(color = indicator.color(colorScheme = MaterialTheme.colorScheme), shape = CircleShape)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
