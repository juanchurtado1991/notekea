package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import app.notekea.core.presentation.components.NotekeaScrollArea
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaLogo
import app.notekea.core.presentation.components.NotekeaProgressIndicator
import app.notekea.core.presentation.components.NotekeaWordmark
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.welcome_busy
import app.notekea.resources.welcome_create_vault
import app.notekea.resources.welcome_new_vault_default_name
import app.notekea.resources.welcome_open_folder
import app.notekea.resources.welcome_open_hint
import app.notekea.resources.welcome_recent_title
import app.notekea.resources.welcome_tagline
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.Vault
import org.jetbrains.compose.resources.stringResource

private const val COLUMN_GAP_DP = 64
private const val COLUMN_MAX_WIDTH_DP = 480
private const val HINT_HEIGHT_DP = 20
private const val PREVIEW_HEIGHT_DP = 640
private const val RECENT_ROW_HEIGHT_DP = 52
private const val TWO_COLUMNS_MIN_WIDTH_DP = 820
private const val VISIBLE_RECENTS = 5

/**
 * Stateless welcome flow: nothing but the wordmark while the last vault is restored, the open vault once there is
 * one (drawn by [openedVaultContent], which the app provides so this feature does not depend on the notes one), and
 * otherwise the welcome screen (`ROADMAP.md` 1.3, two columns since 1.14).
 */
@Composable
fun WelcomeContent(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    openedVaultContent: @Composable (Vault, onSwitchVault: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box {
            val openedVault = state.openedVault
            when {
                state.isLoading -> NotekeaWordmark(modifier = Modifier.align(alignment = Alignment.Center))

                openedVault != null -> openedVaultContent(
                    openedVault
                ) { onIntent(WelcomeIntent.OnSwitchVaultClick) }

                else -> WelcomeHome(state = state, onIntent = onIntent)
            }
        }
    }
}

/**
 * The welcome screen in two columns (one, stacked, in a narrow window): the logo, the actions and any error on the
 * start side; on the end side the recent vaults, or what Notekea is while there are none.
 */
@Composable
private fun WelcomeHome(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= TWO_COLUMNS_MIN_WIDTH_DP.dp
        val scrollState = rememberScrollState()
        val page = Modifier
            .fillMaxWidth()
            .heightIn(min = maxHeight)
            .verticalScroll(state = scrollState)
            .padding(horizontal = NotekeaDimensions.SpacingXxl, vertical = NotekeaDimensions.SpacingXxl)
        NotekeaScrollArea(state = scrollState, modifier = Modifier.fillMaxSize()) {
            if (isWide) {
                Row(
                    modifier = page.wrapContentWidth().widthIn(max = (COLUMN_MAX_WIDTH_DP * 2 + COLUMN_GAP_DP).dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = COLUMN_GAP_DP.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WelcomeStartColumn(state = state, onIntent = onIntent, modifier = Modifier.weight(weight = 1f))
                    WelcomeEndColumn(state = state, onIntent = onIntent, modifier = Modifier.weight(weight = 1f))
                }
            } else {
                Column(
                    modifier = page,
                    verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXl)
                ) {
                    WelcomeStartColumn(state = state, onIntent = onIntent)
                    WelcomeEndColumn(state = state, onIntent = onIntent)
                }
            }
        }
    }
}

/** The logo, the name and the tagline, any error, then Open folder and Create vault or the new vault's form. */
@Composable
private fun WelcomeStartColumn(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = COLUMN_MAX_WIDTH_DP.dp),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
    ) {
        WelcomeHeader()
        val showBanner = state.hasUnexpectedError || (state.error != null && state.newVaultForm == null)
        if (showBanner) {
            WelcomeErrorBanner(
                platformConventions = state.platformConventions,
                onDismiss = { onIntent(WelcomeIntent.OnErrorDismiss) },
                onLocate = { onIntent(WelcomeIntent.OnLocateVaultClick(missingPath = it)) },
                error = state.error.takeUnless { state.hasUnexpectedError },
                modifier = Modifier.fillMaxWidth()
            )
        }
        WelcomeStart(state = state, onIntent = onIntent)
    }
}

/** The recent vaults, or what Notekea is while there are none. */
@Composable
private fun WelcomeEndColumn(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.widthIn(max = COLUMN_MAX_WIDTH_DP.dp)) {
        if (state.recentVaults.isEmpty()) {
            WelcomeFeatures(modifier = Modifier.fillMaxWidth())
        } else {
            RecentVaultsSection(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun WelcomeHeader() {
    NotekeaLogo()
    NotekeaWordmark()
    Text(
        text = stringResource(resource = Res.string.welcome_tagline),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun WelcomeStart(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    val form = state.newVaultForm
    if (form != null) {
        NewVaultPanel(
            form = form,
            platformConventions = state.platformConventions,
            onNameChange = { onIntent(WelcomeIntent.OnNewVaultNameChange(name = it)) },
            onChangeLocation = { onIntent(WelcomeIntent.OnNewVaultLocationClick) },
            onCancel = { onIntent(WelcomeIntent.OnNewVaultCancel) },
            onConfirm = { onIntent(WelcomeIntent.OnNewVaultConfirm) },
            error = state.error,
            isBusy = state.isBusy
        )
        return
    }
    StartActions(state = state, onIntent = onIntent)
    StartHint(isBusy = state.isBusy)
}

@Composable
private fun StartActions(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    val conventions = state.platformConventions
    val suggestedName = stringResource(resource = Res.string.welcome_new_vault_default_name)
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_open_folder),
            onClick = { onIntent(WelcomeIntent.OnOpenFolderClick) },
            enabled = !state.isBusy,
            shortcutHint = conventions.openFolderShortcut.label(operatingSystem = conventions.operatingSystem)
        )
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_create_vault),
            onClick = { onIntent(WelcomeIntent.OnCreateVaultClick(suggestedName = suggestedName)) },
            style = NotekeaButtonStyle.SECONDARY,
            enabled = !state.isBusy
        )
    }
}

@Composable
private fun StartHint(
    isBusy: Boolean
) {
    Row(
        modifier = Modifier.height(height = HINT_HEIGHT_DP.dp),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val busyText = stringResource(resource = Res.string.welcome_busy)
        if (isBusy) NotekeaProgressIndicator(label = busyText)
        Text(
            text = if (isBusy) busyText else stringResource(resource = Res.string.welcome_open_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentVaultsSection(
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

@Composable
private fun WelcomeContentPreviewFrame(
    state: WelcomeState
) {
    NotekeaPreviewThemes {
        WelcomeContent(
            state = state,
            onIntent = {},
            openedVaultContent = { vault, _ ->
                Text(text = vault.name, modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl))
            },
            modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

@Preview
@Composable
private fun WelcomeContentLoadingPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state().copy(isLoading = true))
}

@Preview
@Composable
private fun WelcomeContentNoRecentsPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state())
}

@Preview
@Composable
private fun WelcomeContentRecentsPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state().copy(recentVaults = WelcomePreviewData.recentVaults))
}

@Preview
@Composable
private fun WelcomeContentNewVaultPreview() {
    WelcomeContentPreviewFrame(
        state = WelcomePreviewData.state().copy(
            recentVaults = WelcomePreviewData.recentVaults,
            newVaultForm = WelcomePreviewData.newVaultForm
        )
    )
}

@Preview
@Composable
private fun WelcomeContentPermissionErrorPreview() {
    WelcomeContentPreviewFrame(
        state = WelcomePreviewData.state().copy(
            recentVaults = WelcomePreviewData.recentVaults,
            error = VaultError.PermissionDenied(path = WelcomePreviewData.openedVault.path)
        )
    )
}

@Preview
@Composable
private fun WelcomeContentVaultOpenedPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state().copy(openedVault = WelcomePreviewData.openedVault))
}

@Preview
@Composable
private fun WelcomeContentBusyPreview() {
    WelcomeContentPreviewFrame(
        state = WelcomePreviewData.state().copy(recentVaults = WelcomePreviewData.recentVaults, isBusy = true)
    )
}
