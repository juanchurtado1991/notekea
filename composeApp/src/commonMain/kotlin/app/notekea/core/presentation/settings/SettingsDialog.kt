package app.notekea.core.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaComingSoon
import app.notekea.core.presentation.components.NotekeaDialog
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.settings_close
import app.notekea.resources.settings_section_ai
import app.notekea.resources.settings_section_appearance
import app.notekea.resources.settings_section_daily_notes
import app.notekea.resources.settings_section_editor
import app.notekea.resources.settings_section_files
import app.notekea.resources.settings_section_general
import app.notekea.resources.settings_section_shortcuts
import app.notekea.resources.settings_section_sync
import app.notekea.resources.settings_section_templates
import app.notekea.resources.settings_soon_badge
import app.notekea.resources.settings_soon_message
import app.notekea.resources.settings_title
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_HEIGHT_DP = 500
private const val DIALOG_WIDTH_DP = 760
private const val NAV_TINT_ALPHA = 0.4f
private const val NAV_WIDTH_DP = 200
private const val SECTION_ALPHA = 0.55f
private val SECTIONS = listOf(
    Res.string.settings_section_general,
    Res.string.settings_section_editor,
    Res.string.settings_section_appearance,
    Res.string.settings_section_files,
    Res.string.settings_section_daily_notes,
    Res.string.settings_section_templates,
    Res.string.settings_section_ai,
    Res.string.settings_section_sync,
    Res.string.settings_section_shortcuts
)

/**
 * Settings, until they are built: the sections they will have, dimmed on the left, and "Coming soon" with what they
 * will let the user choose. Esc, a click outside or ✕ closes it ([onDismiss]).
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    NotekeaDialog(
        onDismiss = onDismiss,
        modifier = Modifier.size(width = DIALOG_WIDTH_DP.dp, height = DIALOG_HEIGHT_DP.dp)
    ) { SettingsContent(onDismiss = onDismiss) }
}

@Composable
private fun SettingsContent(
    onDismiss: () -> Unit
) {
    Row {
        SectionList()
        Box(modifier = Modifier.weight(weight = 1f).fillMaxHeight()) {
            NotekeaComingSoon(
                icon = NotekeaIcons.Settings,
                title = stringResource(resource = Res.string.settings_title),
                message = stringResource(resource = Res.string.settings_soon_message),
                badge = stringResource(resource = Res.string.settings_soon_badge),
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.align(alignment = Alignment.TopEnd).padding(all = NotekeaDimensions.SpacingS)) {
                NotekeaIconButton(
                    icon = NotekeaIcons.Close,
                    label = stringResource(resource = Res.string.settings_close),
                    onClick = onDismiss,
                    takesFocus = false
                )
            }
        }
    }
}

/** "Settings" and the sections to come, dimmed: they cannot be chosen yet. */
@Composable
private fun SectionList() {
    Column(
        modifier = Modifier
            .width(width = NAV_WIDTH_DP.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = NAV_TINT_ALPHA))
            .padding(all = NotekeaDimensions.SpacingM)
    ) {
        Text(
            text = stringResource(resource = Res.string.settings_title),
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        SECTIONS.forEach {
            Text(
                text = stringResource(resource = it),
                modifier = Modifier.padding(
                    horizontal = NotekeaDimensions.SpacingS,
                    vertical = NotekeaDimensions.SpacingXs
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SECTION_ALPHA)
            )
        }
    }
}

@Preview
@Composable
private fun SettingsDialogPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = DIALOG_WIDTH_DP.dp, height = DIALOG_HEIGHT_DP.dp)) {
            SettingsContent(onDismiss = {})
        }
    }
}
