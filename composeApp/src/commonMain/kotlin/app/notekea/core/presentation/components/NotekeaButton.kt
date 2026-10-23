package app.notekea.core.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Text button in the three Notekea weights with the 6 dp control radius; colors come from semantic roles. */
@Composable
fun NotekeaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NotekeaButtonStyle = NotekeaButtonStyle.PRIMARY,
    enabled: Boolean = true
) {
    val label: @Composable () -> Unit = { Text(text = text, style = MaterialTheme.typography.labelLarge) }
    val shape = MaterialTheme.shapes.small
    when (style) {
        NotekeaButtonStyle.DESTRUCTIVE -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) { label() }

        NotekeaButtonStyle.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape
        ) { label() }

        NotekeaButtonStyle.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape
        ) { label() }
    }
}
