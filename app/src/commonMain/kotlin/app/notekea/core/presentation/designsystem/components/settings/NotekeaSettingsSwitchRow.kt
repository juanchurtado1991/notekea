package app.notekea.core.presentation.designsystem.components.settings

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.fields.NotekeaSwitch
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private const val PREVIEW_WIDTH_DP = 480
private const val PREVIEW_LABEL = "Open the last vault at startup"
private const val PREVIEW_DETAIL = "Or the welcome screen"

/**
 * A setting that is on or off: a [NotekeaSettingsRow] with its switch, named [label] for screen readers too. A new
 * [spotlitAt] shows the row to the person ([NotekeaSpotlight]); null never does.
 */
@Composable
fun NotekeaSettingsSwitchRow(
    label: String,
    isOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    spotlitAt: Int? = null
) {
    NotekeaSpotlight(
        shownAt = spotlitAt,
        modifier = modifier
    ) {
        NotekeaSettingsRow(
            label = label,
            detail = detail
        ) {
            NotekeaSwitch(
                isOn = isOn,
                onToggle = { onToggle() },
                label = label
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaSettingsSwitchRowPreview() {
    NotekeaPreviewThemes {
        NotekeaSettingsSwitchRow(
            label = PREVIEW_LABEL,
            isOn = true,
            onToggle = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            detail = PREVIEW_DETAIL
        )
    }
}
