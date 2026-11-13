package app.notekea.settings.presentation.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.WebUpdateEvery
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.settings_web_update
import app.notekea.resources.settings_web_update_daily
import app.notekea.resources.settings_web_update_hint
import app.notekea.resources.settings_web_update_monthly
import app.notekea.resources.settings_web_update_never
import app.notekea.resources.settings_web_update_weekly
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val NAMES: Map<WebUpdateEvery, StringResource> = mapOf(
    WebUpdateEvery.NEVER to Res.string.settings_web_update_never,
    WebUpdateEvery.DAILY to Res.string.settings_web_update_daily,
    WebUpdateEvery.WEEKLY to Res.string.settings_web_update_weekly,
    WebUpdateEvery.MONTHLY to Res.string.settings_web_update_monthly
)

/**
 * Settings → General: how often the web sources are updated on their own (the developer's request of 2026-11-13),
 * never unless chosen; the update is the one the Web sources folder's menu offers.
 */
@Composable
internal fun WebUpdateSetting(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.settings_web_update)

    NotekeaSettingsGroup(
        rows = listOf({
            NotekeaSettingsRow(label = label) {
                NotekeaPopUpButton(
                    choice = stringResource(resource = NAMES.getValue(preferences.webUpdate)),
                    choices = WebUpdateEvery.entries.map { every ->
                        NotekeaMenuItem(
                            label = stringResource(resource = NAMES.getValue(every)),
                            onClick = {
                                onIntent(PreferencesIntent.OnWebUpdateChoose(every = every))
                            },
                            isChecked = every == preferences.webUpdate
                        )
                    },
                    label = label
                )
            }
        }),
        footnote = stringResource(resource = Res.string.settings_web_update_hint)
    )
}

@Preview
@Composable
private fun WebUpdateSettingPreview() {
    NotekeaPreviewThemes {
        WebUpdateSetting(
            preferences = AppPreferences(webUpdate = WebUpdateEvery.WEEKLY),
            onIntent = {}
        )
    }
}
