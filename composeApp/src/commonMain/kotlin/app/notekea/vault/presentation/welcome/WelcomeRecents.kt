package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaScrollArea
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.welcome_recent_title
import org.jetbrains.compose.resources.stringResource

private const val RECENT_ROW_HEIGHT_DP = 52
private const val VISIBLE_RECENTS = 5

@Composable
internal fun RecentVaultsSection(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = stringResource(resource = Res.string.welcome_recent_title),
            modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val scrollState = rememberScrollState()
        NotekeaCard(modifier = Modifier.fillMaxWidth()) {
            NotekeaScrollArea(
                state = scrollState,
                modifier = Modifier.heightIn(max = (VISIBLE_RECENTS * RECENT_ROW_HEIGHT_DP).dp)
            ) {
                Column(modifier = Modifier.verticalScroll(state = scrollState)) {
                    RecentRows(state = state, onIntent = onIntent)
                }
            }
        }
    }
}

/** One row per recent vault, most recent first. */
@Composable
private fun RecentRows(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    state.recentVaults.forEach { recentVault ->
        val path = recentVault.vault.path
        key(path) {
            RecentVaultRow(
                recentVault = recentVault,
                platformConventions = state.platformConventions,
                onOpen = { onIntent(WelcomeIntent.OnRecentVaultClick(path = path)) },
                onLocate = { onIntent(WelcomeIntent.OnLocateVaultClick(missingPath = path)) },
                onRemove = { onIntent(WelcomeIntent.OnRemoveRecentVaultClick(path = path)) },
                enabled = !state.isBusy
            )
        }
    }
}

@Preview
@Composable
private fun RecentVaultsSectionPreview() {
    NotekeaPreviewThemes {
        RecentVaultsSection(state = WelcomePreviewData.state().copy(recentVaults = WelcomePreviewData.recentVaults)) {}
    }
}
