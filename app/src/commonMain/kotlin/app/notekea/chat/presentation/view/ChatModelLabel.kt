package app.notekea.chat.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.llm.domain.ModelTier
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_model_large
import app.notekea.resources.chat_model_medium
import app.notekea.resources.chat_model_small
import org.jetbrains.compose.resources.stringResource

/** The model in use under the question field ("Medium"), as in Claude Code. */
@Composable
internal fun ChatModelLabel(
    tier: ModelTier,
    modifier: Modifier = Modifier
) {
    val name = when (tier) {
        ModelTier.SMALL -> Res.string.chat_model_small
        ModelTier.MEDIUM -> Res.string.chat_model_medium
        ModelTier.LARGE -> Res.string.chat_model_large
    }

    Text(
        text = stringResource(resource = name),
        modifier = modifier.padding(all = NotekeaDimensions.SpacingXs),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
private fun ChatModelLabelPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) {
            ChatModelLabel(tier = ModelTier.MEDIUM)
        }
    }
}
