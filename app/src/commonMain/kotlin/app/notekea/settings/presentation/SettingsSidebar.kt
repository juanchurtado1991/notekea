package app.notekea.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaColors
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.settings_title
import org.jetbrains.compose.resources.stringResource

private const val SIDEBAR_WIDTH_DP = 200
private const val ICON_BOX_DP = 22
private const val ICON_DP = 14
private const val TINT_ALPHA = 0.4f

/**
 * Settings' sidebar, as macOS's System Settings (the developer's choice, 2026-11-11): "Settings", then each section
 * with its icon on a small square of its own color; the chosen one is highlighted.
 */
@Composable
internal fun SettingsSidebar(
    section: SettingsSection,
    onSectionChoose: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(width = SIDEBAR_WIDTH_DP.dp)
            .fillMaxHeight()
            .background(
                color = MaterialTheme
                    .colorScheme
                    .surfaceVariant
                    .copy(alpha = TINT_ALPHA)
            )
            .padding(all = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Text(
            text = stringResource(resource = Res.string.settings_title),
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
            style = MaterialTheme
                .typography
                .titleSmall
                .copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        SettingsSection.entries.forEach {
            SectionRow(
                section = it,
                isChosen = it == section,
                onClick = { onSectionChoose(it) }
            )
        }
    }
}

@Composable
private fun SectionRow(
    section: SettingsSection,
    isChosen: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = NotekeaDimensions.ControlRadius)
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .focusBorder(
                color = colors.primary,
                shape = shape
            )
            .background(color = if (isChosen) colors.primaryContainer else Color.Transparent)
            .clickable(
                role = Role.Tab,
                onClick = onClick
            )
            .padding(
                horizontal = NotekeaDimensions.SpacingS,
                vertical = NotekeaDimensions.SpacingXs
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size = ICON_BOX_DP.dp)
                .clip(shape = shape)
                .background(color = colorOf(section = section)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                modifier = Modifier.size(size = ICON_DP.dp),
                tint = colors.surface
            )
        }

        Text(
            text = stringResource(resource = section.label),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isChosen) colors.onPrimaryContainer else colors.onSurface
        )
    }
}

// Each section's color, from the theme's own, as macOS gives each pane of its settings a color of its own.
@Composable
private fun colorOf(section: SettingsSection): Color = when (section) {
    SettingsSection.GENERAL,
    SettingsSection.SHORTCUTS,
    SettingsSection.ABOUT -> MaterialTheme.colorScheme.outline

    SettingsSection.APPEARANCE -> MaterialTheme.colorScheme.primary

    SettingsSection.EDITOR -> NotekeaColors.success

    SettingsSection.FILES -> NotekeaColors.warning

    SettingsSection.AI_MODEL -> NotekeaColors.fileMedia

    SettingsSection.SYNC -> NotekeaColors.fileImage
}

@Preview
@Composable
private fun SettingsSidebarPreview() {
    NotekeaPreviewThemes {
        SettingsSidebar(
            section = SettingsSection.GENERAL,
            onSectionChoose = {}
        )
    }
}
