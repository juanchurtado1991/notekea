package app.notekea.ai.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.ai.domain.ModelSize
import app.notekea.core.presentation.designsystem.components.ErrorBanner
import app.notekea.core.presentation.designsystem.components.NotekeaConfirmDialog
import app.notekea.core.presentation.designsystem.components.NotekeaFold
import app.notekea.core.presentation.designsystem.components.NotekeaProgressIndicator
import app.notekea.core.presentation.designsystem.components.NotekeaScrollArea
import app.notekea.core.presentation.designsystem.components.NotekeaSectionHeader
import app.notekea.core.presentation.designsystem.components.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.formatBytes
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.models_cancel
import app.notekea.resources.models_checking
import app.notekea.resources.models_delete
import app.notekea.resources.models_delete_message
import app.notekea.resources.models_delete_title
import app.notekea.resources.models_fits
import app.notekea.resources.models_folder
import app.notekea.resources.models_intro
import app.notekea.resources.models_load_failed
import app.notekea.resources.models_other_sizes
import app.notekea.resources.settings_section_ai
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 560
private const val PREVIEW_WIDTH_DP = 560

/**
 * Settings → AI model: what the chat model is, the size for this computer as a card
 * with its download, the other sizes folded under "Other sizes", and where the models are kept. It scrolls when the
 * dialog is too short for it.
 */
@Composable
fun ModelManagerContent(
    state: ModelManagerState,
    onIntent: (ModelManagerIntent) -> Unit,
    modifier: Modifier = Modifier,
    spotlight: SettingsSpotlight? = null
) {
    val scrollState = rememberScrollState()

    // Other sizes shown by the help unfold first, as a tab opens before its setting is shown.
    LaunchedEffect(spotlight) {
        val isOtherSizes = spotlight?.place == SettingsPlace.AI_MODEL_OTHER_SIZES
        val isFolded = !state.isOtherSizesShown
        if (isOtherSizes && isFolded) onIntent(ModelManagerIntent.OnOtherSizesToggle)
    }

    NotekeaScrollArea(
        state = scrollState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
                .padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            Text(
                text = stringResource(resource = Res.string.settings_section_ai),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                state.isLoading -> Checking()

                state.hasLoadFailed -> ErrorBanner(
                    message = stringResource(resource = Res.string.models_load_failed)
                )

                else -> Models(
                    state = state,
                    onIntent = onIntent,
                    spotlight = spotlight
                )
            }
        }
    }

    state.pendingDelete?.let {
        DeleteDialog(
            size = it,
            state = state,
            onIntent = onIntent
        )
    }
}

@Composable
private fun Checking() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        val label = stringResource(resource = Res.string.models_checking)
        NotekeaProgressIndicator(label = label)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Models(
    state: ModelManagerState,
    onIntent: (ModelManagerIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val featured = state.models.firstOrNull {
        it.size == state.featured
    }

    val others = state.models.filterNot {
        it == featured
    }

    Text(
        text = stringResource(resource = Res.string.models_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    state
        .models
        .firstOrNull {
            it.size == state.recommended
        }
        ?.let {
            Text(
                text = stringResource(
                    resource = Res.string.models_fits,
                    stringResource(resource = nameOf(size = it.size)),
                    state.memoryGb
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    featured?.let {
        NotekeaSpotlight(
            shownAt = spotlight?.serialFor(place = SettingsPlace.AI_MODEL_SIZE)
        ) {
            ModelCard(
                row = it,
                state = state,
                isFeatured = true,
                onIntent = onIntent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.AI_MODEL_OTHER_SIZES)
    ) {
        OtherSizes(
            others = others,
            state = state,
            onIntent = onIntent
        )
    }

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.AI_MODEL_FOLDER)
    ) {
        Text(
            text = stringResource(resource = Res.string.models_folder, state.folder),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** "Other sizes" and, once opened, the sizes not featured, as plain rows. */
@Composable
private fun OtherSizes(
    others: List<ModelRow>,
    state: ModelManagerState,
    onIntent: (ModelManagerIntent) -> Unit
) {
    Column {
        NotekeaSectionHeader(
            title = stringResource(resource = Res.string.models_other_sizes),
            isOpen = state.isOtherSizesShown,
            onToggle = { onIntent(ModelManagerIntent.OnOtherSizesToggle) },
            count = others.size
        )

        NotekeaFold(isOpen = state.isOtherSizesShown) {
            Column(
                modifier = Modifier.padding(top = NotekeaDimensions.SpacingS),
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
            ) {
                others.forEach {
                    ModelCard(
                        row = it,
                        state = state,
                        isFeatured = false,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteDialog(
    size: ModelSize,
    state: ModelManagerState,
    onIntent: (ModelManagerIntent) -> Unit
) {
    val row = state.models.firstOrNull {
        it.size == size
    } ?: return

    NotekeaConfirmDialog(
        title = stringResource(
            resource = Res.string.models_delete_title,
            stringResource(resource = nameOf(size = size))
        ),
        message = stringResource(
            resource = Res.string.models_delete_message,
            formatBytes(bytes = state.freedBy(row = row))
        ),
        confirmLabel = stringResource(resource = Res.string.models_delete),
        cancelLabel = stringResource(resource = Res.string.models_cancel),
        buttonOrder = state.buttonOrder,
        onConfirm = { onIntent(ModelManagerIntent.OnDeleteConfirm) },
        onDismiss = { onIntent(ModelManagerIntent.OnDeleteDismiss) },
        isDestructive = true
    )
}

@Preview
@Composable
private fun ModelManagerContentPreview() {
    NotekeaPreviewThemes {
        listOf(
            ModelManagerState(),
            ModelManagerState(
                isLoading = false,
                hasLoadFailed = true
            ),
            ModelManagerSamples.notInstalled,
            ModelManagerSamples.downloading,
            ModelManagerSamples.inUse
        ).forEach {
            ModelManagerContent(
                state = it,
                onIntent = {
                },
                modifier = Modifier.size(
                    width = PREVIEW_WIDTH_DP.dp,
                    height = PREVIEW_HEIGHT_DP.dp
                )
            )
        }
    }
}
