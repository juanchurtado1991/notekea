package app.notekea.models.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaChip
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.models.domain.ModelSize
import app.notekea.resources.Res
import app.notekea.resources.models_hint_large
import app.notekea.resources.models_hint_medium
import app.notekea.resources.models_hint_small
import app.notekea.resources.models_recommended
import app.notekea.resources.models_size_large
import app.notekea.resources.models_size_medium
import app.notekea.resources.models_size_small
import app.notekea.resources.models_title
import app.notekea.resources.models_too_big
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 480

/**
 * One size of model: its name, "Recommended" when it is the size for this computer, what it is good for, a warning
 * when this computer lacks the memory it needs, and its [ModelActions]. The featured size is a card; the other sizes
 * are plain rows under it.
 */
@Composable
internal fun ModelCard(
    row: ModelRow,
    state: ModelManagerState,
    isFeatured: Boolean,
    onIntent: (ModelManagerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val body: @Composable () -> Unit = {
        Column(
            modifier = if (isFeatured) Modifier.padding(all = NotekeaDimensions.SpacingM) else Modifier,
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            ModelHeading(row = row, isRecommended = row.size == state.recommended)
            Text(
                text = stringResource(resource = hintOf(size = row.size)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!row.fits) {
                Text(
                    text = stringResource(resource = Res.string.models_too_big, row.minimumMemoryGb, state.memoryGb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            ModelActions(
                row = row,
                status = state.statusOf(size = row.size),
                isFeatured = isFeatured,
                onIntent = onIntent,
                searchBytes = state.rerankerBytes.takeUnless { state.isRerankerInstalled },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    if (isFeatured) {
        NotekeaCard(modifier = modifier) { body() }
    } else {
        Column(modifier = modifier.fillMaxWidth()) { body() }
    }
}

@Composable
private fun ModelHeading(
    row: ModelRow,
    isRecommended: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = stringResource(
                resource = Res.string.models_title,
                stringResource(resource = nameOf(size = row.size)),
                row.name
            ),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isRecommended) {
            NotekeaChip(
                label = stringResource(resource = Res.string.models_recommended),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

/** The name of [size] as the person reads it. */
internal fun nameOf(
    size: ModelSize
): StringResource = when (size) {
    ModelSize.SMALL -> Res.string.models_size_small
    ModelSize.MEDIUM -> Res.string.models_size_medium
    ModelSize.LARGE -> Res.string.models_size_large
}

private fun hintOf(
    size: ModelSize
): StringResource = when (size) {
    ModelSize.SMALL -> Res.string.models_hint_small
    ModelSize.MEDIUM -> Res.string.models_hint_medium
    ModelSize.LARGE -> Res.string.models_hint_large
}

@Preview
@Composable
private fun ModelCardPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            val state = ModelManagerSamples.downloading
            ModelCard(row = ModelManagerSamples.medium, state = state, isFeatured = true, onIntent = {})
            ModelCard(row = ModelManagerSamples.large, state = state, isFeatured = false, onIntent = {})
        }
    }
}
