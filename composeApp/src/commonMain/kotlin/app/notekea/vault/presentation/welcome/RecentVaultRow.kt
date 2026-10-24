package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaListItem
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.elapsed_days
import app.notekea.resources.elapsed_hours
import app.notekea.resources.elapsed_just_now
import app.notekea.resources.elapsed_minutes
import app.notekea.resources.elapsed_months
import app.notekea.resources.elapsed_weeks
import app.notekea.resources.elapsed_years
import app.notekea.resources.welcome_recent_locate
import app.notekea.resources.welcome_recent_missing
import app.notekea.resources.welcome_recent_open
import app.notekea.resources.welcome_recent_remove
import app.notekea.vault.domain.RecentVaultStatus
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 520

/**
 * A recent vault: its name, its folder as users write it ("~/Notes") and when it was last opened. A vault whose
 * folder is gone is muted and offers "Locate…" and "Remove" instead of failing when clicked.
 */
@Composable
internal fun RecentVaultRow(
    recentVault: RecentVaultStatus,
    platformConventions: PlatformConventions,
    onOpen: () -> Unit,
    onLocate: () -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean = true
) {
    val path = recentVault.vault.path
    NotekeaListItem(
        title = recentVault.vault.name,
        onClick = onOpen,
        supportingText = if (recentVault.isMissing) {
            stringResource(resource = Res.string.welcome_recent_missing)
        } else {
            displayPath(path = path, platformConventions = platformConventions)
        },
        enabled = enabled && !recentVault.isMissing,
        onClickLabel = stringResource(resource = Res.string.welcome_recent_open),
        trailingContent = {
            if (recentVault.isMissing) {
                MissingVaultActions(enabled = enabled, onLocate = onLocate, onRemove = onRemove)
            } else {
                Text(
                    text = elapsedTimeText(elapsed = recentVault.lastOpened),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun MissingVaultActions(
    enabled: Boolean,
    onLocate: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_recent_remove),
            onClick = onRemove,
            style = NotekeaButtonStyle.TEXT,
            enabled = enabled
        )
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_recent_locate),
            onClick = onLocate,
            style = NotekeaButtonStyle.SECONDARY,
            enabled = enabled
        )
    }
}

@Composable
private fun elapsedTimeText(
    elapsed: ElapsedTime
): String = when (elapsed) {
    is ElapsedTime.Days -> countText(resource = Res.plurals.elapsed_days, count = elapsed.count)
    is ElapsedTime.Hours -> countText(resource = Res.plurals.elapsed_hours, count = elapsed.count)
    ElapsedTime.JustNow -> stringResource(resource = Res.string.elapsed_just_now)
    is ElapsedTime.Minutes -> countText(resource = Res.plurals.elapsed_minutes, count = elapsed.count)
    is ElapsedTime.Months -> countText(resource = Res.plurals.elapsed_months, count = elapsed.count)
    is ElapsedTime.Weeks -> countText(resource = Res.plurals.elapsed_weeks, count = elapsed.count)
    is ElapsedTime.Years -> countText(resource = Res.plurals.elapsed_years, count = elapsed.count)
}

@Composable
private fun countText(
    resource: PluralStringResource,
    count: Int
): String = pluralStringResource(resource, count, count)

@Preview
@Composable
private fun RecentVaultRowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            WelcomePreviewData.recentVaults.forEach {
                RecentVaultRow(
                    recentVault = it,
                    platformConventions = WelcomePreviewData.platformConventions,
                    onOpen = {},
                    onLocate = {},
                    onRemove = {}
                )
            }
        }
    }
}
