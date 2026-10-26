package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PALETTE_WIDTH_DP = 560
private const val PALETTE_TOP_DP = 72
private const val PREVIEW_AREPAS = "Arepas"
private const val PREVIEW_FOLDER = "Recipes"
private const val PREVIEW_PLACEHOLDER = "Find a note"
private const val PREVIEW_PREPARE = "Prepare dough"
private const val PREVIEW_QUERY = "are"
private const val PREVIEW_HEIGHT_DP = 360
private const val PREVIEW_WIDTH_DP = 720
private const val SCRIM_ALPHA = 0.32f
private const val SHADOW_DP = 8

/**
 * A command palette over the whole window (the quick switcher, `Mod+P`): a dimmed backdrop that closes it when clicked,
 * and a 560 dp card near the top with an optional [header], a search field that takes focus when the palette appears,
 * and the results below ([content], usually [NotekeaPaletteItem]s). The keyboard never leaves the field: Up and Down
 * move the selection ([onMove] with -1 or 1), Enter confirms it, Esc closes and Tab calls [onTab] (switching what is
 * searched) when given. Draw it last in a full-size box so it covers the rest of the window.
 */
@Composable
fun NotekeaPalette(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onMove: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onTab: (() -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scrim(onDismiss = onDismiss)
        PaletteCard(modifier = Modifier.align(alignment = Alignment.TopCenter)) {
            header?.invoke()
            PaletteField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = placeholder,
                modifier = Modifier.onPreviewKeyEvent { event ->
                    handlePaletteKey(
                        event = event,
                        onMove = onMove,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss,
                        onTab = onTab
                    )
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = NotekeaDimensions.SpacingS),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            content()
        }
    }
}

/** The dimmed window behind the palette; clicking it closes the palette, as clicking outside a menu does. */
@Composable
private fun Scrim(
    onDismiss: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onDismiss)
    )
}

@Composable
private fun PaletteCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .padding(top = PALETTE_TOP_DP.dp, start = NotekeaDimensions.SpacingL, end = NotekeaDimensions.SpacingL)
            .width(width = PALETTE_WIDTH_DP.dp),
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline),
        shadowElevation = SHADOW_DP.dp
    ) {
        Column(modifier = Modifier.padding(all = NotekeaDimensions.SpacingS), content = content)
    }
}

@Composable
private fun PaletteField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester = focusRequester)
            .semantics { contentDescription = placeholder },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        singleLine = true,
        cursorBrush = SolidColor(value = colors.primary),
        decorationBox = { field ->
            Box(
                modifier = Modifier
                    .height(height = NotekeaDimensions.CompactControlSize + NotekeaDimensions.SpacingS)
                    .padding(horizontal = NotekeaDimensions.SpacingS),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant
                    )
                }
                field()
            }
        }
    )
}

private fun handlePaletteKey(
    event: KeyEvent,
    onMove: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onTab: (() -> Unit)?
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    val action = PaletteKeyAction.of(key = event.key, isShiftPressed = event.isShiftPressed, canSwitch = onTab != null)
    when (action) {
        PaletteKeyAction.MOVE_DOWN -> onMove(1)
        PaletteKeyAction.MOVE_UP -> onMove(-1)
        PaletteKeyAction.CONFIRM -> onConfirm()
        PaletteKeyAction.DISMISS -> onDismiss()
        PaletteKeyAction.SWITCH -> onTab?.invoke()
        null -> return false
    }
    return true
}

@Preview
@Composable
private fun NotekeaPalettePreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            NotekeaPalette(
                query = PREVIEW_QUERY,
                onQueryChange = {},
                placeholder = PREVIEW_PLACEHOLDER,
                onMove = {},
                onConfirm = {},
                onDismiss = {}
            ) {
                NotekeaPaletteItem(
                    title = PREVIEW_AREPAS,
                    onClick = {},
                    titleHighlights = listOf(0..2),
                    selected = true,
                    supportingText = PREVIEW_FOLDER
                )
                NotekeaPaletteItem(title = PREVIEW_PREPARE, onClick = {}, titleHighlights = listOf(3..5))
            }
        }
    }
}
