package app.notekea.settings.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaLogo
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.components.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.NotekeaWordmark
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.about_copy_diagnostics
import app.notekea.resources.about_diagnostics_copied
import app.notekea.resources.about_diagnostics_failed
import app.notekea.resources.about_github
import app.notekea.resources.about_tagline
import app.notekea.resources.about_version
import app.notekea.resources.settings_about_diagnostics
import app.notekea.resources.settings_about_diagnostics_hint
import app.notekea.resources.settings_about_licenses
import app.notekea.resources.settings_about_licenses_text
import app.notekea.resources.settings_about_privacy
import app.notekea.resources.settings_about_privacy_text
import app.notekea.resources.settings_section_about
import org.jetbrains.compose.resources.stringResource

private const val LOGO_SIZE_DP = 64
private const val REPOSITORY_URL = "https://github.com/juanchurtado1991/notekea"

/**
 * Settings → About (5.2): the logo, name, version and tagline, then Copy diagnostics for a report, and the privacy
 * promise and the licenses, with the project on GitHub.
 */
@Composable
internal fun AboutSettings(
    state: AboutState,
    onIntent: (AboutIntent) -> Unit,
    spotlight: SettingsSpotlight?,
    modifier: Modifier = Modifier
) {
    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_about),
        modifier = modifier
    ) {
        NotekeaSpotlight(
            shownAt = spotlight?.serialFor(place = SettingsPlace.ABOUT_VERSION)
        ) { Identity() }

        NotekeaSpotlight(
            shownAt = spotlight?.serialFor(place = SettingsPlace.ABOUT_DIAGNOSTICS)
        ) {
            NotekeaSettingsGroup(
                rows = listOf({
                    Diagnostics(
                        state = state,
                        onIntent = onIntent
                    )
                }),
                footnote = stringResource(resource = Res.string.settings_about_diagnostics_hint)
            )
        }

        NotekeaSpotlight(
            shownAt = spotlight?.serialFor(place = SettingsPlace.ABOUT_PRIVACY)
        ) { PrivacyAndLicenses() }
    }
}

@Composable
private fun Identity() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        NotekeaLogo(size = LOGO_SIZE_DP.dp)
        NotekeaWordmark()

        Text(
            text = stringResource(Res.string.about_version, LocalAppVersion.current),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(resource = Res.string.about_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Diagnostics(
    state: AboutState,
    onIntent: (AboutIntent) -> Unit
) {
    val label = when {
        state.hasCopyFailed -> Res.string.about_diagnostics_failed
        state.isDiagnosticsCopied -> Res.string.about_diagnostics_copied
        else -> Res.string.about_copy_diagnostics
    }

    NotekeaSettingsRow(label = stringResource(resource = Res.string.settings_about_diagnostics)) {
        NotekeaButton(
            text = stringResource(resource = label),
            onClick = { onIntent(AboutIntent.OnCopyDiagnostics) },
            style = NotekeaButtonStyle.SECONDARY
        )
    }
}

@Composable
private fun PrivacyAndLicenses() {
    val uriHandler = LocalUriHandler.current

    NotekeaSettingsGroup(
        rows = listOf(
            {
                NotekeaSettingsRow(
                    label = stringResource(resource = Res.string.settings_about_privacy),
                    detail = stringResource(resource = Res.string.settings_about_privacy_text)
                )
            },
            {
                NotekeaSettingsRow(
                    label = stringResource(resource = Res.string.settings_about_licenses),
                    detail = stringResource(resource = Res.string.settings_about_licenses_text)
                ) {
                    NotekeaButton(
                        text = stringResource(resource = Res.string.about_github),
                        onClick = { uriHandler.openUri(uri = REPOSITORY_URL) },
                        style = NotekeaButtonStyle.SECONDARY
                    )
                }
            }
        )
    )
}

@Preview
@Composable
private fun AboutSettingsPreview() {
    NotekeaPreviewThemes {
        AboutSettings(
            state = AboutState(),
            onIntent = {},
            spotlight = null
        )
    }
}
