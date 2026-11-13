package app.notekea.settings.presentation.files

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
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
                    FolderChoice(
                        label = stringResource(resource = Res.string.settings_attachments_folder),
                        folder = state.attachments,
                        folders = state.foldersWith(current = state.attachments),
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_ATTACHMENTS)
                    ) { onIntent(FilesSettingsIntent.OnAttachmentsChange(folder = it)) }
                },
                {
                    FolderChoice(
                        label = stringResource(resource = Res.string.settings_daily_notes_folder),
                        folder = state.dailyFolder,
                        folders = state.foldersWith(current = state.dailyFolder),
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_DAILY_FOLDER)
                    ) { onIntent(FilesSettingsIntent.OnDailyFolderChange(folder = it)) }
                },
                {
                    FormatChoice(
                        state = state,
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_DAILY_FORMAT)
                    ) { onIntent(FilesSettingsIntent.OnDailyFormatChange(format = it)) }
                },
                {
                    FolderChoice(
                        label = stringResource(resource = Res.string.settings_templates_folder),
                        folder = state.templatesFolder,
                        folders = state.foldersWith(current = state.templatesFolder),
                        shownAt = spotlight?.serialFor(place = SettingsPlace.FILES_TEMPLATES)
                    ) { onIntent(FilesSettingsIntent.OnTemplatesFolderChange(folder = it)) }
                }
            ),
            footnote = stringResource(resource = Res.string.settings_files_hint)
        )
    }
}

// A folder chosen among the vault's own, the vault's top folder first: nothing to type, nothing to misspell.
@Composable
private fun FolderChoice(
    label: String,
    folder: String,
    folders: List<String>,
    shownAt: Int?,
    onChoose: (String) -> Unit
) {
    val root = stringResource(resource = Res.string.settings_folder_root)

    val choices = (listOf("") + folders).map { path ->
        NotekeaMenuItem(
            label = path.ifEmpty { root },
            onClick = { onChoose(path) },
            icon = NotekeaIcons.Folder,
            isChecked = path == folder
        )
    }

    NotekeaSpotlight(shownAt = shownAt) {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = folder.ifEmpty { root },
                choices = choices,
                label = label
            )
        }
    }
}

// The daily note's name as today is written in each format ("13-11-2026"), not as the format's letters.
@Composable
private fun FormatChoice(
    state: FilesSettingsState,
    shownAt: Int?,
    onChoose: (String) -> Unit
) {
    val label = stringResource(resource = Res.string.settings_daily_notes_format)
    val current = state.dailyFormat.ifBlank { DateFormatChoice.FORMATS.first() }

    val choices = state.formats.map { choice ->
        NotekeaMenuItem(
            label = choice.example,
            onClick = { onChoose(choice.format) },
            isChecked = choice.format == current
        )
    }

    val shown = state
        .formats
        .firstOrNull {
            it.format == current
        }
        ?.example
        ?: current

    NotekeaSpotlight(shownAt = shownAt) {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = shown,
                choices = choices,
                label = label
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
                dailyFolder = "Journal/Daily",
                folders = listOf("attachments", "Journal", "Journal/Daily", "Templates"),
                formats = listOf(
                    DateFormatChoice(
                        format = "YYYY-MM-DD",
                        example = "2026-11-13"
                    )
                )
            ),
            onIntent = {},
            spotlight = null
        )
    }
}
