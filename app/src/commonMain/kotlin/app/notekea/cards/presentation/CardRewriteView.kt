package app.notekea.cards.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.cards.domain.ProposedCard
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.study_keep_old
import app.notekea.resources.study_new_version
import app.notekea.resources.study_rewrite
import app.notekea.resources.study_rewrite_failed
import app.notekea.resources.study_rewriting
import app.notekea.resources.study_struggling
import app.notekea.resources.study_use_new
import org.jetbrains.compose.resources.stringResource

/**
 * In the sentence's box of a hard card (plan step 4): the offer to write it anew, then the new wording beside the
 * card shown, to use or to drop; nothing changes in the note until the new one is chosen.
 */
@Composable
internal fun CardRewrite(
    isRewriting: Boolean,
    isRewriteFailed: Boolean,
    onRewrite: () -> Unit,
    onUse: () -> Unit,
    onKeep: () -> Unit,
    rewrite: ProposedCard?
) {
    HorizontalDivider()

    when {
        rewrite != null -> Proposal(
            rewrite = rewrite,
            onUse = onUse,
            onKeep = onKeep
        )

        isRewriting -> Said(text = stringResource(resource = Res.string.study_rewriting))

        else -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val said = if (isRewriteFailed) {
                Res.string.study_rewrite_failed
            } else {
                Res.string.study_struggling
            }

            Text(
                text = stringResource(resource = said),
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isRewriteFailed) {
                NotekeaButton(
                    text = stringResource(resource = Res.string.study_rewrite),
                    onClick = onRewrite,
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        }
    }
}

@Composable
private fun Proposal(
    rewrite: ProposedCard,
    onUse: () -> Unit,
    onKeep: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Text(
            text = stringResource(resource = Res.string.study_new_version),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = rewrite.question,
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = rewrite.answer,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaButton(
                text = stringResource(resource = Res.string.study_use_new),
                onClick = onUse
            )

            NotekeaButton(
                text = stringResource(resource = Res.string.study_keep_old),
                onClick = onKeep,
                style = NotekeaButtonStyle.TEXT
            )
        }
    }
}

@Composable
private fun Said(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun CardRewritePreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)) {
            CardRewrite(
                isRewriting = false,
                isRewriteFailed = false,
                onRewrite = {},
                onUse = {},
                onKeep = {},
                rewrite = null
            )

            CardRewrite(
                isRewriting = false,
                isRewriteFailed = false,
                onRewrite = {},
                onUse = {},
                onKeep = {},
                rewrite = ProposedCard(
                    question = "Which tram goes up to Alfama?",
                    answer = "The 28",
                    sentence = "The 28 tram climbs from Baixa up to Alfama."
                )
            )
        }
    }
}
