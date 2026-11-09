package app.notekea.ai.presentation

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
import app.notekea.core.presentation.designsystem.components.ErrorBanner
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.ai.domain.ModelError
import app.notekea.resources.Res
import app.notekea.resources.models_delete
import app.notekea.resources.models_download
import app.notekea.resources.models_download_size
import app.notekea.resources.models_download_size_with_search
import app.notekea.resources.models_error_checksum
import app.notekea.resources.models_error_disk
import app.notekea.resources.models_error_network
import app.notekea.resources.models_error_storage
import app.notekea.resources.models_in_use
import app.notekea.resources.models_retry
import app.notekea.resources.models_use
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_SEARCH_SHARE = 6
private const val PREVIEW_WIDTH_DP = 420

/**
 * What can be done with [row] in its [status]: download it (with its size, and the reranker's the first time), follow
 * its installation, try again after a failure, use it, or delete it. The [isFeatured] card's download is the primary
 * button; the other sizes' are quieter.
 */
@Composable
internal fun ModelActions(
    row: ModelRow,
    status: ModelStatus,
    isFeatured: Boolean,
    onIntent: (ModelManagerIntent) -> Unit,
    searchBytes: Long?,
    modifier: Modifier = Modifier
) {
    when (status) {
        is ModelStatus.Installing -> ModelProgress(
            size = row.size,
            status = status,
            onIntent = onIntent,
            modifier = modifier
        )

        is ModelStatus.Failed -> ErrorBanner(
            message = errorMessage(error = status.error),
            modifier = modifier,
            action = {
                NotekeaButton(
                    text = stringResource(resource = Res.string.models_retry),
                    onClick = { onIntent(ModelManagerIntent.OnInstall(size = row.size)) },
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        )

        else -> ActionRow(
            row = row,
            status = status,
            searchBytes = searchBytes,
            isFeatured = isFeatured,
            onIntent = onIntent,
            modifier = modifier
        )
    }
}

@Composable
private fun ActionRow(
    row: ModelRow,
    status: ModelStatus,
    isFeatured: Boolean,
    onIntent: (ModelManagerIntent) -> Unit,
    modifier: Modifier,
    searchBytes: Long?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = when (status) {
                ModelStatus.InUse -> stringResource(resource = Res.string.models_in_use)
                ModelStatus.Installed -> formatBytes(bytes = row.downloadBytes)
                else -> downloadText(downloadBytes = row.downloadBytes, searchBytes = searchBytes)
            },
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodySmall,
            color = if (status == ModelStatus.InUse) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        ActionButtons(row = row, status = status, isFeatured = isFeatured, onIntent = onIntent)
    }
}

/** Download, or Use and Delete; nothing while another size installs. */
@Composable
private fun ActionButtons(
    row: ModelRow,
    status: ModelStatus,
    isFeatured: Boolean,
    onIntent: (ModelManagerIntent) -> Unit
) {
    when (status) {
        ModelStatus.NotInstalled -> NotekeaButton(
            text = stringResource(resource = Res.string.models_download),
            onClick = { onIntent(ModelManagerIntent.OnInstall(size = row.size)) },
            style = if (isFeatured) NotekeaButtonStyle.PRIMARY else NotekeaButtonStyle.SECONDARY
        )

        ModelStatus.Installed -> NotekeaButton(
            text = stringResource(resource = Res.string.models_use),
            onClick = { onIntent(ModelManagerIntent.OnUse(size = row.size)) },
            style = NotekeaButtonStyle.SECONDARY
        )

        else -> Unit
    }
    if (status == ModelStatus.Installed || status == ModelStatus.InUse) {
        NotekeaButton(
            text = stringResource(resource = Res.string.models_delete),
            onClick = { onIntent(ModelManagerIntent.OnDeleteAsk(size = row.size)) },
            style = NotekeaButtonStyle.TEXT
        )
    }
}

@Composable
private fun downloadText(
    downloadBytes: Long,
    searchBytes: Long?
): String = if (searchBytes == null) {
    stringResource(resource = Res.string.models_download_size, formatBytes(bytes = downloadBytes))
} else {
    stringResource(
        resource = Res.string.models_download_size_with_search,
        formatBytes(bytes = downloadBytes),
        formatBytes(bytes = searchBytes)
    )
}

@Composable
private fun errorMessage(
    error: ModelError
): String = when (error) {
    ModelError.ChecksumMismatch -> stringResource(resource = Res.string.models_error_checksum)

    ModelError.Network -> stringResource(resource = Res.string.models_error_network)

    ModelError.Storage -> stringResource(resource = Res.string.models_error_storage)

    is ModelError.NotEnoughDisk -> stringResource(
        resource = Res.string.models_error_disk,
        formatBytes(bytes = error.neededBytes)
    )
}

@Preview
@Composable
private fun ModelActionsPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            val row = ModelManagerSamples.medium
            listOf(ModelStatus.NotInstalled, ModelStatus.Installed, ModelStatus.InUse, ModelStatus.Waiting).forEach {
                ModelActions(
                    row = row,
                    status = it,
                    isFeatured = true,
                    onIntent = {},
                    searchBytes = row.downloadBytes / PREVIEW_SEARCH_SHARE
                )
            }
            ModelActions(
                row = row,
                status = ModelStatus.Failed(error = ModelError.Network),
                isFeatured = true,
                onIntent = {},
                searchBytes = null
            )
        }
    }
}
