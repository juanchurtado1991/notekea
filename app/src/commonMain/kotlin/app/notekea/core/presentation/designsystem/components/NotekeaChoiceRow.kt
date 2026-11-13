package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaColors
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val TINT_ALPHA = 0.12f
private const val HOVER_ALPHA = 0.05f
private const val DIMMED_ALPHA = 0.5f
private const val BADGE_DP = 22
private const val ICON_DP = 16
private const val PREVIEW_WIDTH_DP = 420

/** What a [NotekeaChoiceRow] says: not chosen yet, the right one, the wrong one chosen, or one of the others. */
enum class ChoiceState {
    IDLE,
    CORRECT,
    WRONG,
    DIMMED;

    companion object {
        /** The state of the option at [index] of a question whose right one is at [correct], once [chosen]. */
        fun of(
            index: Int,
            correct: Int,
            chosen: Int?
        ): ChoiceState = when {
            chosen == null -> IDLE
            index == correct -> CORRECT
            index == chosen -> WRONG
            else -> DIMMED
        }
    }
}

/**
 * An option of a multiple choice question: its [letter] in a badge, its [text], and, once chosen, [state] says whether
 * it was right (green, a check) or wrong (red, a cross); the others dim. Only an [ChoiceState.IDLE] row takes clicks.
 */
@Composable
fun NotekeaChoiceRow(
    letter: String,
    text: String,
    state: ChoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val accent = when (state) {
        ChoiceState.CORRECT -> NotekeaColors.success
        ChoiceState.WRONG -> colors.error
        ChoiceState.IDLE, ChoiceState.DIMMED -> null
    }

    val fill = when {
        accent != null -> accent.copy(alpha = TINT_ALPHA)
        isHovered && state == ChoiceState.IDLE -> colors.onSurface.copy(alpha = HOVER_ALPHA)
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha = if (state == ChoiceState.DIMMED) DIMMED_ALPHA else 1f)
            .clip(shape = shape)
            .background(color = fill)
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = accent ?: colors.outlineVariant,
                shape = shape
            )
            .focusBorder(
                color = colors.primary,
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = state == ChoiceState.IDLE,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingS
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LetterBadge(
            letter = letter,
            accent = accent
        )

        Text(
            text = text,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )

        val icon = when (state) {
            ChoiceState.CORRECT -> NotekeaIcons.Checked
            ChoiceState.WRONG -> NotekeaIcons.Close
            ChoiceState.IDLE, ChoiceState.DIMMED -> null
        }

        if (icon != null && accent != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = ICON_DP.dp),
                tint = accent
            )
        }
    }
}

@Composable
private fun LetterBadge(
    letter: String,
    accent: Color?
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(size = BADGE_DP.dp)
            .background(
                color = accent ?: colors.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.labelMedium,
            color = if (accent != null) colors.surface else colors.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun NotekeaChoiceRowPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            listOf(
                "Line 15" to ChoiceState.DIMMED,
                "Line 28" to ChoiceState.CORRECT,
                "Line 12" to ChoiceState.WRONG,
                "Line 24" to ChoiceState.IDLE
            ).forEachIndexed { index, (text, state) ->
                NotekeaChoiceRow(
                    letter = "${'A' + index}",
                    text = text,
                    state = state,
                    onClick = {}
                )
            }
        }
    }
}
