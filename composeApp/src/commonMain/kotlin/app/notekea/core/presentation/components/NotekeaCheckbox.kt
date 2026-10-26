package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val CHECKED = "checked"
private const val CHECK_SIZE_RATIO = 0.75f
private const val CORNER_DP = 3
private const val DEFAULT_SIZE_DP = 16
private const val PREVIEW_LABEL = "Buy flour"
private const val UNCHECKED = "not checked"

/**
 * A small square checkbox: an outlined box when unchecked, filled with the primary color and a check mark when
 * [checked]. Clicking it calls [onClick]; it has the same focus border as every clickable component and is read by
 * screen readers as a checkbox named [label] with its state.
 */
@Composable
fun NotekeaCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    size: Int = DEFAULT_SIZE_DP
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(size = CORNER_DP.dp)
    Box(
        modifier = modifier
            .size(size = size.dp)
            .focusBorder(color = colors.primary, shape = shape)
            .clip(shape = shape)
            .background(color = if (checked) colors.primary else colors.surface)
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = if (checked) colors.primary else colors.outline,
                shape = shape
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics {
                contentDescription = label
                stateDescription = if (checked) CHECKED else UNCHECKED
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = NotekeaIcons.Checked,
                contentDescription = null,
                modifier = Modifier.size(size = (size * CHECK_SIZE_RATIO).dp),
                tint = colors.onPrimary
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaCheckboxPreview() {
    NotekeaPreviewThemes {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotekeaCheckbox(checked = false, onClick = {}, label = PREVIEW_LABEL)
            NotekeaCheckbox(checked = true, onClick = {}, label = PREVIEW_LABEL)
            NotekeaCheckbox(checked = true, onClick = {}, label = PREVIEW_LABEL, size = 20)
        }
    }
}
