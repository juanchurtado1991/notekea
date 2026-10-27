package app.notekea.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_FIND = "Find a note"
private const val PREVIEW_FIND_KEYS = "Ctrl P"
private const val PREVIEW_TODAY = "Today's note"
private const val PREVIEW_TODAY_KEYS = "Ctrl D"
private const val PREVIEW_WIDTH_DP = 360
private const val ROW_HEIGHT_DP = 36

/**
 * An action offered where there is nothing yet (an editor without tabs), as VS Code's start page lists them: its
 * [icon] and [label], and the [keys] of its shortcut as keycaps at the end, so the row also teaches it. A click runs
 * [onClick].
 */
@Composable
fun NotekeaActionRow(
    icon: ImageVector,
    label: String,
    keys: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_HEIGHT_DP.dp)
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.weight(weight = 1f))
        NotekeaKeycaps(keys = keys)
    }
}

@Preview
@Composable
private fun NotekeaActionRowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaActionRow(
                icon = NotekeaIcons.Find,
                label = PREVIEW_FIND,
                keys = PREVIEW_FIND_KEYS.split(" "),
                onClick = {}
            )
            NotekeaActionRow(
                icon = NotekeaIcons.Today,
                label = PREVIEW_TODAY,
                keys = PREVIEW_TODAY_KEYS.split(" "),
                onClick = {}
            )
        }
    }
}
