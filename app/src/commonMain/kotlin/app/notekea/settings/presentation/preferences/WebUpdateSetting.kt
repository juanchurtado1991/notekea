package app.notekea.settings.presentation.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.WebUpdateEvery
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.settings.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalWindowWebSources
import app.notekea.resources.Res
import app.notekea.resources.settings_web_group
import app.notekea.resources.settings_web_last
import app.notekea.resources.settings_web_last_days
import app.notekea.resources.settings_web_last_never
import app.notekea.resources.settings_web_last_today
import app.notekea.resources.settings_web_last_yesterday
import app.notekea.resources.settings_web_now
import app.notekea.resources.settings_web_update
import app.notekea.resources.settings_web_update_daily
import app.notekea.resources.settings_web_update_hint
import app.notekea.resources.settings_web_update_monthly
import app.notekea.resources.settings_web_update_never
import app.notekea.resources.settings_web_update_weekly
import app.notekea.resources.settings_web_updating
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val NAMES: Map<WebUpdateEvery, StringResource> = mapOf(
    WebUpdateEvery.NEVER to Res.string.settings_web_update_never,
    WebUpdateEvery.DAILY to Res.string.settings_web_update_daily,
    WebUpdateEvery.WEEKLY to Res.string.settings_web_update_weekly,
    WebUpdateEvery.MONTHLY to Res.string.settings_web_update_monthly
)

/**
 * Settings → General, first: the web sources (the developer's requests of 2026-11-13 and 2026-11-14). How often they
 * are updated on their own, never unless chosen; and, with a vault open, when they were last updated and Update now,
 * the update the Web sources folder's menu offers.
 */
@Composable
internal fun WebUpdateSetting(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.settings_web_update)
    val sources = LocalWindowWebSources.current
    val updateNow = sources.updateNow

    val frequency: @Composable () -> Unit = {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = stringResource(resource = NAMES.getValue(preferences.webUpdate)),
                choices = WebUpdateEvery.entries.map { every ->
                    NotekeaMenuItem(
                        label = stringResource(resource = NAMES.getValue(every)),
                        onClick = { onIntent(PreferencesIntent.OnWebUpdateChoose(every = every)) },
                        isChecked = every == preferences.webUpdate
                    )
                },
                label = label
            )
        }
    }

    val last: (@Composable () -> Unit)? = updateNow?.let { update ->
        {
            LastUpdate(
                days = sources.daysSinceUpdate,
                isUpdating = sources.isUpdating,
                onUpdate = update
            )
        }
    }

    NotekeaSettingsGroup(
        rows = listOfNotNull(frequency, last),
        header = stringResource(resource = Res.string.settings_web_group),
        footnote = stringResource(resource = Res.string.settings_web_update_hint)
    )
}

@Composable
private fun LastUpdate(
    isUpdating: Boolean,
    onUpdate: () -> Unit,
    days: Int?
) {
    val detail = when (days) {
        null -> stringResource(resource = Res.string.settings_web_last_never)
        0 -> stringResource(resource = Res.string.settings_web_last_today)
        1 -> stringResource(resource = Res.string.settings_web_last_yesterday)
        else -> stringResource(Res.string.settings_web_last_days, days)
    }

    val button = if (isUpdating) Res.string.settings_web_updating else Res.string.settings_web_now

    NotekeaSettingsRow(
        label = stringResource(resource = Res.string.settings_web_last),
        detail = detail
    ) {
        NotekeaButton(
            text = stringResource(resource = button),
            onClick = onUpdate,
            style = NotekeaButtonStyle.SECONDARY,
            enabled = !isUpdating
        )
    }
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
