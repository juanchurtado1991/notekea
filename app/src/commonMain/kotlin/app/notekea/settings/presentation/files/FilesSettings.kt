package app.notekea.settings.presentation.files

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.fields.NotekeaTextField
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.settings_attachments_folder
import app.notekea.resources.settings_daily_notes_folder
import app.notekea.resources.settings_daily_notes_format
import app.notekea.resources.settings_files_failed
import app.notekea.resources.settings_files_hint
import app.notekea.resources.settings_files_no_vault
import app.notekea.resources.settings_folder_root
import app.notekea.resources.settings_section_files
import app.notekea.resources.settings_templates_folder
import org.jetbrains.compose.resources.stringResource

private const val FIELD_WIDTH_DP = 220
private const val DATE_FORMAT_HINT = "YYYY-MM-DD"

/**
 * Settings → Files (5.2): where the open vault's attachments, daily notes and templates go, and the daily note's date
 * format, as Obsidian's settings of the vault say; without a vault it says to open one.
 */
@Composable
internal fun FilesSettings(
    state: FilesSettingsState,
    onIntent: (FilesSettingsIntent) -> Unit,
    spotlight: SettingsSpotlight?,
    modifier: Modifier = Modifier
) {
    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_files),
        modifier = modifier
    ) {
        if (!state.hasVault) {
            Text(
                text = stringResource(resource = Res.string.settings_files_no_vault),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            return@NotekeaSettingsPage
        }

        if (state.hasFailed) {
            ErrorBanner(message = stringResource(resource = Res.string.settings_files_failed))
        }

        NotekeaSettingsGroup(
            rows = listOf(
                {
                    Field(
                        label = stringResource(resource = Res.string.settings_attachments_folder),
                        value = state.attachments,
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_ATTACHMENTS)
                    ) { onIntent(FilesSettingsIntent.OnAttachmentsChange(folder = it)) }
                },
                {
                    Field(
                        label = stringResource(resource = Res.string.settings_daily_notes_folder),
                        value = state.dailyFolder,
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_DAILY_FOLDER)
                    ) { onIntent(FilesSettingsIntent.OnDailyFolderChange(folder = it)) }
                },
                {
                    Field(
                        label = stringResource(resource = Res.string.settings_daily_notes_format),
                        value = state.dailyFormat,
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_DAILY_FORMAT),
                        hint = DATE_FORMAT_HINT
                    ) { onIntent(FilesSettingsIntent.OnDailyFormatChange(format = it)) }
                },
                {
                    Field(
                        label = stringResource(resource = Res.string.settings_templates_folder),
                        value = state.templatesFolder,
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_TEMPLATES)
                    ) { onIntent(FilesSettingsIntent.OnTemplatesFolderChange(folder = it)) }
                }
            ),
            footnote = stringResource(resource = Res.string.settings_files_hint)
        )
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    shownAt: Int?,
    hint: String? = null,
    onChange: (String) -> Unit
) {
    NotekeaSpotlight(shownAt = shownAt) {
        NotekeaSettingsRow(label = label) {
            NotekeaTextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.width(width = FIELD_WIDTH_DP.dp),
                placeholder = hint ?: stringResource(resource = Res.string.settings_folder_root)
            )
        }
    }
}

@Preview
@Composable
private fun FilesSettingsPreview() {
    NotekeaPreviewThemes {
        FilesSettings(
            state = FilesSettingsState(
                hasVault = true,
                attachments = "attachments",
                dailyFolder = "Journal/Daily"
            ),
            onIntent = {},
            spotlight = null
        )
    }
}
