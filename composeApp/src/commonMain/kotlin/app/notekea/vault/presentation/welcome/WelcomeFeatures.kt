package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.welcome_feature_ai_text
import app.notekea.resources.welcome_feature_ai_title
import app.notekea.resources.welcome_feature_markdown_text
import app.notekea.resources.welcome_feature_markdown_title
import app.notekea.resources.welcome_feature_obsidian_text
import app.notekea.resources.welcome_feature_obsidian_title
import app.notekea.resources.welcome_feature_private_text
import app.notekea.resources.welcome_feature_private_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 420
private val FEATURES = listOf(
    Triple(
        NotekeaIcons.FeatureMarkdown,
        Res.string.welcome_feature_markdown_title,
        Res.string.welcome_feature_markdown_text
    ),
    Triple(
        NotekeaIcons.FeatureObsidian,
        Res.string.welcome_feature_obsidian_title,
        Res.string.welcome_feature_obsidian_text
    ),
    Triple(NotekeaIcons.FeatureAi, Res.string.welcome_feature_ai_title, Res.string.welcome_feature_ai_text),
    Triple(
        NotekeaIcons.FeaturePrivate,
        Res.string.welcome_feature_private_title,
        Res.string.welcome_feature_private_text
    )
)

/** What Notekea is, on a card beside the welcome screen's actions while there is no recent vault to show there. */
@Composable
internal fun WelcomeFeatures(
    modifier: Modifier = Modifier
) {
    NotekeaCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            FEATURES.forEach { (icon, title, text) -> Feature(icon = icon, title = title, text = text) }
        }
    }
}

@Composable
private fun Feature(
    icon: ImageVector,
    title: StringResource,
    text: StringResource
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize + NotekeaDimensions.SpacingXs),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = stringResource(resource = title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(resource = text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun WelcomeFeaturesPreview() {
    NotekeaPreviewThemes { WelcomeFeatures(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) }
}
