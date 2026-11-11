package app.notekea.settings.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.resources.Res
import app.notekea.resources.settings_close
import app.notekea.settings.presentation.about.AboutSettings
import app.notekea.settings.presentation.appearance.AppearanceSettings
import app.notekea.settings.presentation.files.FilesSettings
import app.notekea.settings.presentation.help.SettingsHelpBar
import app.notekea.settings.presentation.help.SettingsHelpIntent
import app.notekea.settings.presentation.preferences.EditorSettings
import app.notekea.settings.presentation.preferences.GeneralSettings
import app.notekea.settings.presentation.shortcuts.ShortcutsSettings
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_HEIGHT_DP = 580
private const val DIALOG_WIDTH_DP = 780

// Ask about Settings, a card beside the dialog (the developer's review of 2026-11-11).
private const val HELP_WIDTH_DP = 340

/**
 * Settings, as macOS's System Settings (5.2, the developer's choice of 2026-11-11): the sections in a sidebar with
 * their icons, the chosen one beside it (AI model and Sync from the app: [aiModelSection], [syncSection]), and Ask
 * about Settings in a card, which opens the section of the setting it shows. Esc, a click outside or ✕ closes it.
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    platformConventions: PlatformConventions,
    aiModelSection: @Composable (Modifier, SettingsSpotlight?) -> Unit,
    syncSection: @Composable (Modifier, SettingsSpotlight?) -> Unit,
    vaultPath: String?,
    initialSection: SettingsSection = SettingsSection.GENERAL,
    viewModels: SettingsViewModels = SettingsViewModels.fromKoin(vaultPath = vaultPath)
) {
    val help by viewModels
        .help
        .state
        .collectAsState()

    var section by remember { mutableStateOf(value = initialSection) }
    val spotlight = help.spotlight

    // A setting the help shows opens its section first, as an intent opens its screen.
    LaunchedEffect(spotlight) {
        spotlight?.let { section = SettingsSection.of(topic = it.place.topic) }
    }

    NotekeaDialog(
        onDismiss = onDismiss,
        modifier = Modifier.size(
            width = DIALOG_WIDTH_DP.dp,
            height = DIALOG_HEIGHT_DP.dp
        ),
        side = {
            SettingsHelpBar(
                state = help,
                isSync = section == SettingsSection.SYNC,
                onDraftChange = {
                    viewModels.help.onIntent(SettingsHelpIntent.OnDraftChange(text = it))
                },
                onAsk = {
                    viewModels.help.onIntent(SettingsHelpIntent.OnAsk(topic = section.topic))
                },
                onStop = { viewModels.help.onIntent(SettingsHelpIntent.OnStop) },
                onShow = { viewModels.help.onIntent(SettingsHelpIntent.OnShow(place = it)) },
                modifier = Modifier.fillMaxSize()
            )
        },
        sideWidth = HELP_WIDTH_DP.dp
    ) {
        Row {
            SettingsSidebar(
                section = section,
                onSectionChoose = { section = it }
            )

            Box(modifier = Modifier.weight(weight = 1f).fillMaxHeight()) {
                SectionContent(
                    section = section,
                    viewModels = viewModels,
                    platformConventions = platformConventions,
                    spotlight = spotlight,
                    aiModelSection = aiModelSection,
                    syncSection = syncSection
                )

                val corner = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(all = NotekeaDimensions.SpacingS)

                Box(modifier = corner) {
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
}

@Composable
private fun SectionContent(
    section: SettingsSection,
    viewModels: SettingsViewModels,
    platformConventions: PlatformConventions,
    aiModelSection: @Composable (Modifier, SettingsSpotlight?) -> Unit,
    syncSection: @Composable (Modifier, SettingsSpotlight?) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val fill = Modifier.fillMaxSize()

    when (section) {
        SettingsSection.GENERAL -> {
            val preferences by viewModels
                .preferences
                .state
                .collectAsState()

            GeneralSettings(preferences, viewModels.preferences::onIntent, spotlight, fill)
        }

        SettingsSection.APPEARANCE -> {
            val appearance by viewModels
                .appearance
                .state
                .collectAsState()

            AppearanceSettings(appearance, viewModels.appearance::onIntent, fill, spotlight)
        }

        SettingsSection.EDITOR -> {
            val preferences by viewModels
                .preferences
                .state
                .collectAsState()

            EditorSettings(preferences, viewModels.preferences::onIntent, spotlight, fill)
        }

        SettingsSection.FILES -> {
            val files by viewModels
                .files
                .state
                .collectAsState()

            FilesSettings(files, viewModels.files::onIntent, spotlight, fill)
        }

        SettingsSection.AI_MODEL -> aiModelSection(fill, spotlight)

        SettingsSection.SYNC -> syncSection(fill, spotlight)

        SettingsSection.SHORTCUTS -> ShortcutsSettings(platformConventions, spotlight, fill)

        SettingsSection.ABOUT -> {
            val about by viewModels
                .about
                .state
                .collectAsState()

            AboutSettings(about, viewModels.about::onIntent, spotlight, fill)
        }
    }
}

@Preview
@Composable
private fun SettingsDialogPreview() {
    NotekeaPreviewThemes {
        SettingsSidebar(
            section = SettingsSection.GENERAL,
            onSectionChoose = {}
        )
    }
}
