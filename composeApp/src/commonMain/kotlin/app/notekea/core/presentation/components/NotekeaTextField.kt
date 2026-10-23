package app.notekea.core.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Single- or multi-line text input with an optional placeholder, styled with the Notekea theme. */
@Composable
fun NotekeaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = placeholder?.let { text -> { Text(text = text, style = MaterialTheme.typography.bodyMedium) } },
        singleLine = singleLine,
        shape = MaterialTheme.shapes.small
    )
}
