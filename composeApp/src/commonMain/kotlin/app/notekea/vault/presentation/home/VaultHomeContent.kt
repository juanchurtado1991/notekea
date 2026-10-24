package app.notekea.vault.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.vault_home_message
import app.notekea.resources.vault_home_no_notes
import app.notekea.resources.vault_home_note_count
import app.notekea.resources.vault_home_switch
import app.notekea.vault.domain.OpenedVault
import app.notekea.vault.presentation.welcome.WelcomePreviewData
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 360

/**
 * The open vault: a header with its name and folder, and a reassuring summary of what was found. The note list
 * replaces the summary in `ROADMAP.md` 1.4. "Open another vault" returns to the welcome screen with the recent list;
 * the open-folder shortcut keeps working here and opens the folder picker directly.
 */
@Composable
fun VaultHomeContent(
    openedVault: OpenedVault,
    platformConventions: PlatformConventions,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        VaultHeader(
            openedVault = openedVault,
            platformConventions = platformConventions,
            onSwitchVault = onSwitchVault
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = NotekeaDimensions.BorderWidth)
                .background(color = MaterialTheme.colorScheme.outline)
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = noteCountText(noteCount = openedVault.noteCount),
                message = stringResource(resource = Res.string.vault_home_message)
            )
        }
    }
}

@Composable
private fun VaultHeader(
    openedVault: OpenedVault,
    platformConventions: PlatformConventions,
    onSwitchVault: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NotekeaDimensions.SpacingXl, vertical = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = openedVault.vault.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = displayPath(path = openedVault.vault.path, platformConventions = platformConventions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        NotekeaButton(
            text = stringResource(resource = Res.string.vault_home_switch),
            onClick = onSwitchVault,
            style = NotekeaButtonStyle.SECONDARY
        )
    }
}

@Composable
private fun noteCountText(
    noteCount: Int
): String = if (noteCount == 0) {
    stringResource(resource = Res.string.vault_home_no_notes)
} else {
    pluralStringResource(Res.plurals.vault_home_note_count, noteCount, noteCount)
}

@Preview
@Composable
private fun VaultHomeContentPreview() {
    NotekeaPreviewThemes {
        VaultHomeContent(
            openedVault = WelcomePreviewData.openedVault,
            platformConventions = WelcomePreviewData.platformConventions,
            onSwitchVault = {},
            modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

@Preview
@Composable
private fun VaultHomeContentEmptyVaultPreview() {
    NotekeaPreviewThemes {
        VaultHomeContent(
            openedVault = WelcomePreviewData.openedVault.copy(noteCount = 0),
            platformConventions = WelcomePreviewData.platformConventions,
            onSwitchVault = {},
            modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}
