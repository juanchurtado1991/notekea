package app.notekea.settings.presentation.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSwitch
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.settings_language
import app.notekea.resources.settings_language_english
import app.notekea.resources.settings_language_hint
import app.notekea.resources.settings_language_spanish
import app.notekea.resources.settings_language_system
import app.notekea.resources.settings_open_last_vault
import app.notekea.resources.settings_open_last_vault_hint
import app.notekea.resources.settings_section_general
import app.notekea.resources.settings_show_welcome
import app.notekea.resources.settings_show_welcome_button
import app.notekea.resources.settings_show_welcome_hint
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Settings → General: the language, the last vault at startup and the welcome shown again (5.2). */
@Composable
internal fun GeneralSettings(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?,
    modifier: Modifier = Modifier
) {
    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_general),
        modifier = modifier
    ) {
        WebUpdateSetting(
            preferences = preferences,
            onIntent = onIntent
        )

        NotekeaSettingsGroup(
            rows = listOf(
                {
                    Language(
                        preferences = preferences,
                        onIntent = onIntent,
                        spotlight = spotlight
                    )
                },
                {
                    OpenLastVault(
                        preferences = preferences,
                        onIntent = onIntent,
                        spotlight = spotlight
                    )
                }
            ),
            footnote = stringResource(resource = Res.string.settings_language_hint)
        )

        NotekeaSettingsGroup(
            rows = listOf({
                ShowWelcome(
                    onIntent = onIntent,
                    spotlight = spotlight
                )
            }),
            footnote = stringResource(resource = Res.string.settings_show_welcome_hint)
        )
    }
}

@Composable
private fun Language(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val label = stringResource(resource = Res.string.settings_language)

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.GENERAL_LANGUAGE)
    ) {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = stringResource(resource = nameOf(language = preferences.language)),
                choices = AppLanguage.entries.map {
                    NotekeaMenuItem(
                        label = stringResource(resource = nameOf(language = it)),
                        onClick = { onIntent(PreferencesIntent.OnLanguageChoose(language = it)) },
                        isChecked = it == preferences.language
                    )
                },
                label = label
            )
        }
    }
}

@Composable
private fun OpenLastVault(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val label = stringResource(resource = Res.string.settings_open_last_vault)

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.GENERAL_OPEN_LAST_VAULT)
    ) {
        NotekeaSettingsRow(
            label = label,
            detail = stringResource(resource = Res.string.settings_open_last_vault_hint)
        ) {
            NotekeaSwitch(
                isOn = preferences.opensLastVault,
                onToggle = { onIntent(PreferencesIntent.OnOpenLastVaultToggle) },
                label = label
            )
        }
    }
}

@Composable
private fun ShowWelcome(
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.GENERAL_SHOW_WELCOME)
    ) {
        NotekeaSettingsRow(label = stringResource(resource = Res.string.settings_show_welcome)) {
            NotekeaButton(
                text = stringResource(resource = Res.string.settings_show_welcome_button),
                onClick = { onIntent(PreferencesIntent.OnShowWelcomeAgain) },
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

private fun nameOf(language: AppLanguage): StringResource = when (language) {
    AppLanguage.SYSTEM -> Res.string.settings_language_system
    AppLanguage.ENGLISH -> Res.string.settings_language_english
    AppLanguage.SPANISH -> Res.string.settings_language_spanish
}

@Preview
@Composable
private fun GeneralSettingsPreview() {
    NotekeaPreviewThemes {
        GeneralSettings(
            preferences = AppPreferences(),
            onIntent = {},
            spotlight = null
        )
    }
}
