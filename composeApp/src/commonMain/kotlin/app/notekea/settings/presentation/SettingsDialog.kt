package app.notekea.settings.presentation

import androidx.compose.foundation.background
import app.notekea.settings.presentation.appearance.AppearanceSettings
import app.notekea.settings.presentation.appearance.AppearanceState
import app.notekea.settings.presentation.appearance.AppearanceViewModel
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.notekea.core.presentation.designsystem.components.focusBorder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.settings_close
import app.notekea.resources.settings_section_daily_notes
import app.notekea.resources.settings_section_editor
import app.notekea.resources.settings_section_files
import app.notekea.resources.settings_section_general
import app.notekea.resources.settings_section_shortcuts
import app.notekea.resources.settings_section_sync
import app.notekea.resources.settings_section_templates
import app.notekea.resources.settings_soon_badge
import app.notekea.resources.settings_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val DIALOG_HEIGHT_DP = 560
private const val DIALOG_WIDTH_DP = 760
private const val NAV_TINT_ALPHA = 0.4f
private const val NAV_WIDTH_DP = 200
private const val SECTION_ALPHA = 0.55f
private val SECTIONS_TO_COME = listOf(
    Res.string.settings_section_general,
    Res.string.settings_section_editor,
    Res.string.settings_section_files,
    Res.string.settings_section_daily_notes,
    Res.string.settings_section_templates,
    Res.string.settings_section_sync,
    Res.string.settings_section_shortcuts
)

/**
 * Settings: the sections on the left, Appearance (`ROADMAP.md` 2.12) and AI model (3.1) to choose from, the chosen one
 * on the right, and the sections to come dimmed under "Coming soon" (5.2). AI model belongs to a feature, so the app
 * gives it as [aiModelSection]. Esc, a click outside or ✕ closes it ([onDismiss]).
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    aiModelSection: @Composable (Modifier) -> Unit,
    viewModel: AppearanceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var section by remember { mutableStateOf(value = SettingsSection.APPEARANCE) }
    NotekeaDialog(
        onDismiss = onDismiss,
        modifier = Modifier.size(width = DIALOG_WIDTH_DP.dp, height = DIALOG_HEIGHT_DP.dp)
    ) {
        SettingsContent(
            section = section,
            onSectionChoose = { section = it },
            onDismiss = onDismiss
        ) {
            when (section) {
                SettingsSection.APPEARANCE -> AppearanceSettings(
                    state = state,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsSection.AI_MODEL -> aiModelSection(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun SettingsContent(
    section: SettingsSection,
    onSectionChoose: (SettingsSection) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Row {
        SectionList(section = section, onSectionChoose = onSectionChoose)
        Box(modifier = Modifier.weight(weight = 1f).fillMaxHeight()) {
            content()
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

/** "Settings", the sections that can be chosen, then the sections to come, dimmed under "Coming soon". */
@Composable
private fun SectionList(
    section: SettingsSection,
    onSectionChoose: (SettingsSection) -> Unit
) {
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
        SettingsSection.entries.forEach {
            SectionRow(
                name = it.label,
                isChosen = it == section,
                onClick = { onSectionChoose(it) }
            )
        }
        SectionsToCome()
    }
}

/** "Coming soon" and the sections still to come under it, dimmed: they cannot be chosen yet. */
@Composable
private fun SectionsToCome() {
    Text(
        text = stringResource(resource = Res.string.settings_soon_badge),
        modifier = Modifier.padding(
            start = NotekeaDimensions.SpacingS,
            top = NotekeaDimensions.SpacingL,
            bottom = NotekeaDimensions.SpacingXs
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    SECTIONS_TO_COME.forEach {
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

/** A section that can be chosen, highlighted as the selected row while it is shown on the right. */
@Composable
private fun SectionRow(
    name: StringResource,
    isChosen: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = NotekeaDimensions.ControlRadius)
    Text(
        text = stringResource(resource = name),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .focusBorder(color = MaterialTheme.colorScheme.primary, shape = shape)
            .background(color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS, vertical = NotekeaDimensions.SpacingXs),
        style = MaterialTheme.typography.bodyMedium,
        color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    )
}

@Preview
@Composable
private fun SettingsDialogPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = DIALOG_WIDTH_DP.dp, height = DIALOG_HEIGHT_DP.dp)) {
            SettingsContent(section = SettingsSection.APPEARANCE, onSectionChoose = {}, onDismiss = {}) {
                AppearanceSettings(state = AppearanceState(), onIntent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
