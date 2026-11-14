package app.notekea.sync.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.sync_repair_banner
import app.notekea.resources.sync_repair_open
import org.jetbrains.compose.resources.stringResource

/**
 * Over the workspace while the vault is left in the middle of a merge (the app closed during a sync): it says
 * nothing syncs until it is repaired, and Repair opens the two ways to put it right ([onRepair]).
 */
@Composable
fun RepairBanner(
    isShown: Boolean,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isShown) return

    ErrorBanner(
        message = stringResource(resource = Res.string.sync_repair_banner),
        modifier = modifier,
        action = {
            NotekeaButton(
                text = stringResource(resource = Res.string.sync_repair_open),
                onClick = onRepair,
                style = NotekeaButtonStyle.TEXT
            )
        }
    )
}

@Preview
@Composable
private fun RepairBannerPreview() {
    NotekeaPreviewThemes {
        RepairBanner(
            isShown = true,
            onRepair = {}
        )
    }
}
