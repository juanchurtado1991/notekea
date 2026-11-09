package app.notekea.connections.presentation

import androidx.compose.ui.semantics.CustomAccessibilityAction

/** A row's action for screen readers, named [label]. */
internal fun connectionAction(
    label: String,
    onAction: () -> Unit
) = CustomAccessibilityAction(label = label) {
    onAction()
    true
}
