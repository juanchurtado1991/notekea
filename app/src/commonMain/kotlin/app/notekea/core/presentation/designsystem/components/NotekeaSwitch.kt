package app.notekea.core.presentation.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private const val PREVIEW_LABEL = "Open the last vault at startup"

/** An on/off setting, as macOS draws one beside its label: the theme's color when on; [label] is for screen readers. */
@Composable
fun NotekeaSwitch(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Switch(
        checked = isOn,
        onCheckedChange = onToggle,
        modifier = modifier.semantics { contentDescription = label },
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.primary,
            checkedThumbColor = colors.onPrimary,
            uncheckedTrackColor = colors.surfaceContainerHighest,
            uncheckedThumbColor = colors.outline,
            uncheckedBorderColor = colors.outlineVariant
        )
    )
}

@Preview
@Composable
private fun NotekeaSwitchPreview() {
    NotekeaPreviewThemes {
        NotekeaSwitch(
            isOn = true,
            onToggle = {},
            label = PREVIEW_LABEL
        )
    }
}
