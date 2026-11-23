package app.notekea.core.presentation.designsystem.components.fields

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/** The keys that submit a field: Enter, on the keyboard and on the number pad. */
internal val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

/** Enter submits ([onSubmit]) and Escape cancels ([onCancel]); the field sees neither key, and every other. */
internal fun Modifier.submitOrCancelOnKeys(
    onSubmit: () -> Unit,
    onCancel: () -> Unit
): Modifier = onPreviewKeyEvent { event ->
    val isKeyDown = event.type == KeyEventType.KeyDown
    val isSubmit = isKeyDown && event.key in SUBMIT_KEYS
    val isCancel = isKeyDown && event.key == Key.Escape
    if (isSubmit) onSubmit()
    if (isCancel) onCancel()
    isSubmit || isCancel
}
