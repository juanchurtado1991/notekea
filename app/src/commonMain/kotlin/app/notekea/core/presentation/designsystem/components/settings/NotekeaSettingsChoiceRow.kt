package app.notekea.core.presentation.designsystem.components.settings

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private const val PREVIEW_WIDTH_DP = 480
private const val PREVIEW_LABEL = "Language"
private const val PREVIEW_CHOICE = "Español"

/**
 * A setting with a few choices: a [NotekeaSettingsRow] with a pop-up button showing the current [choice] and opening
 * [choices], named [label] for screen readers too. A new [spotlitAt] shows the row to the person
 * ([NotekeaSpotlight]); null never does.
 */
@Composable
fun NotekeaSettingsChoiceRow(
    label: String,
    choice: String,
    choices: List<NotekeaMenuItem>,
    modifier: Modifier = Modifier,
    spotlitAt: Int? = null
) {
    NotekeaSpotlight(
        shownAt = spotlitAt,
        modifier = modifier
    ) {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = choice,
                choices = choices,
                label = label
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaSettingsChoiceRowPreview() {
    NotekeaPreviewThemes {
        NotekeaSettingsChoiceRow(
            label = PREVIEW_LABEL,
            choice = PREVIEW_CHOICE,
            choices = listOf(
                NotekeaMenuItem(
                    label = PREVIEW_CHOICE,
                    onClick = {},
                    isChecked = true
                )
            ),
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}
