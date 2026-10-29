package app.notekea.core.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaDialog
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaKeycaps
import app.notekea.core.presentation.components.NotekeaLogo
import app.notekea.core.presentation.components.NotekeaScrollArea
import app.notekea.core.presentation.components.NotekeaWordmark
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.keys
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.about_close
import app.notekea.resources.about_copy_diagnostics
import app.notekea.resources.about_diagnostics_copied
import app.notekea.resources.about_github
import app.notekea.resources.about_shortcuts_title
import app.notekea.resources.about_tagline
import app.notekea.resources.about_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val DIALOG_HEIGHT_DP = 520
private const val DIALOG_WIDTH_DP = 780
private const val GROUP_LETTER_SPACING_SP = 0.8
private const val IDENTITY_TINT_ALPHA = 0.5f
private const val IDENTITY_WIDTH_DP = 260
private const val LOGO_SIZE_DP = 88
private const val REPOSITORY_URL = "https://github.com/juanchurtado1991/notekea"

/**
 * About Notekea: on the left the logo, the name, the version and the tagline, with links to the project on GitHub and
 * Copy diagnostics, for a tester's report; on the right every keyboard shortcut on this platform, as keycaps, in groups
 * from the most used ([AboutShortcuts]). Esc, a click outside or ✕ closes it ([onDismiss]).
 */
@Composable
fun AboutDialog(
    platformConventions: PlatformConventions,
    onDismiss: () -> Unit,
    viewModel: AboutViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    NotekeaDialog(
        onDismiss = onDismiss,
        modifier = Modifier.size(width = DIALOG_WIDTH_DP.dp, height = DIALOG_HEIGHT_DP.dp)
    ) {
        AboutContent(
            platformConventions = platformConventions,
            state = state,
            onIntent = viewModel::onIntent,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun AboutContent(
    platformConventions: PlatformConventions,
    state: AboutState,
    onIntent: (AboutIntent) -> Unit,
    onDismiss: () -> Unit
) {
    Row {
        Identity(state = state, onIntent = onIntent)
        Column(modifier = Modifier.weight(weight = 1f).padding(all = NotekeaDimensions.SpacingXl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(resource = Res.string.about_shortcuts_title),
                    modifier = Modifier.weight(weight = 1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                NotekeaIconButton(
                    icon = NotekeaIcons.Close,
                    label = stringResource(resource = Res.string.about_close),
                    onClick = onDismiss,
                    takesFocus = false
                )
            }
            ShortcutList(platformConventions = platformConventions)
        }
    }
}

/** The logo, the name, the version, the tagline, GitHub and Copy diagnostics, on a soft column. */
@Composable
private fun Identity(
    state: AboutState,
    onIntent: (AboutIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .width(width = IDENTITY_WIDTH_DP.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = IDENTITY_TINT_ALPHA))
            .padding(all = NotekeaDimensions.SpacingXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.CenterVertically
        )
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
        IdentityButtons(state = state, onIntent = onIntent)
    }
}

/** GitHub, and Copy diagnostics, which says once it has copied them. */
@Composable
private fun IdentityButtons(
    state: AboutState,
    onIntent: (AboutIntent) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    NotekeaButton(
        text = stringResource(resource = Res.string.about_github),
        onClick = { uriHandler.openUri(uri = REPOSITORY_URL) },
        style = NotekeaButtonStyle.SECONDARY
    )
    NotekeaButton(
        text = stringResource(
            resource = if (state.isDiagnosticsCopied) {
                Res.string.about_diagnostics_copied
            } else {
                Res.string.about_copy_diagnostics
            }
        ),
        onClick = { onIntent(AboutIntent.OnCopyDiagnostics) },
        style = NotekeaButtonStyle.SECONDARY
    )
}

/** Every group of shortcuts, scrolling with a scrollbar when they do not fit. */
@Composable
private fun ShortcutList(
    platformConventions: PlatformConventions
) {
    val operatingSystem = platformConventions.operatingSystem
    val scrollState = rememberScrollState()
    NotekeaScrollArea(state = scrollState, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(
                state = scrollState
            ).padding(end = NotekeaDimensions.SpacingM),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            AboutShortcuts.groups(conventions = platformConventions).forEach { group ->
                Text(
                    text = stringResource(resource = group.title).uppercase(),
                    modifier = Modifier.padding(top = NotekeaDimensions.SpacingS),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = GROUP_LETTER_SPACING_SP.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                group.shortcuts.forEach { ShortcutRow(shortcut = it, keys = it.shortcut.keys(operatingSystem)) }
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    shortcut: AboutShortcut,
    keys: List<String>
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(resource = shortcut.label),
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        NotekeaKeycaps(keys = keys)
    }
}

@Preview
@Composable
private fun AboutDialogPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = DIALOG_WIDTH_DP.dp, height = DIALOG_HEIGHT_DP.dp)) {
            AboutContent(
                platformConventions = PreviewPlatformConventions.linux,
                state = AboutState(),
                onIntent = {},
                onDismiss = {}
            )
        }
    }
}
