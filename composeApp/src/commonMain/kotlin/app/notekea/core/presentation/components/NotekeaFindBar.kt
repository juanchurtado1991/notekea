package app.notekea.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.input.key.Key
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
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.find_bar_close
import app.notekea.resources.find_bar_next
import app.notekea.resources.find_bar_placeholder
import app.notekea.resources.find_bar_previous
import org.jetbrains.compose.resources.stringResource

private const val FIELD_WIDTH_DP = 240
private const val PREVIEW_COUNT = "2 of 5"
private const val PREVIEW_QUERY = "arepas"
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

/**
 * An inline bar that finds text in the current page (Find in note, `Mod+F`): a field that takes focus when the bar
 * appears, how many matches there are ([matchLabel], "2 of 5"), previous and next buttons and close. Enter goes to the
 * next match, Shift+Enter to the previous one and Esc closes, as in browsers and editors on every platform.
 */
@Composable
fun NotekeaFindBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchLabel: String,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) {
        Row(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingXs),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FindField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.focusRequester(focusRequester = focusRequester).onPreviewKeyEvent { event ->
                    handleFindKey(event = event, onNext = onNext, onPrevious = onPrevious, onClose = onClose)
                }
            )
            Text(
                text = matchLabel,
                modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingS),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NotekeaIconButton(
                icon = NotekeaIcons.FindPrevious,
                label = stringResource(resource = Res.string.find_bar_previous),
                onClick = onPrevious
            )
            NotekeaIconButton(
                icon = NotekeaIcons.FindNext,
                label = stringResource(resource = Res.string.find_bar_next),
                onClick = onNext
            )
            NotekeaIconButton(
                icon = NotekeaIcons.Close,
                label = stringResource(resource = Res.string.find_bar_close),
                onClick = onClose
            )
        }
    }
}

@Composable
private fun FindField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme
    val placeholder = stringResource(resource = Res.string.find_bar_placeholder)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.width(width = FIELD_WIDTH_DP.dp).semantics { contentDescription = placeholder },
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
        singleLine = true,
        cursorBrush = SolidColor(value = colors.primary),
        decorationBox = { field ->
            Box(
                modifier = Modifier
                    .height(height = NotekeaDimensions.CompactControlSize)
                    .border(
                        width = NotekeaDimensions.BorderWidth,
                        color = colors.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = NotekeaDimensions.SpacingS),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
                field()
            }
        }
    )
}

private fun handleFindKey(
    event: KeyEvent,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    when {
        event.key in SUBMIT_KEYS && event.isShiftPressed -> onPrevious()
        event.key in SUBMIT_KEYS -> onNext()
        event.key == Key.Escape -> onClose()
        else -> return false
    }
    return true
}

@Preview
@Composable
private fun NotekeaFindBarPreview() {
    NotekeaPreviewThemes {
        NotekeaFindBar(
            query = PREVIEW_QUERY,
            onQueryChange = {},
            matchLabel = PREVIEW_COUNT,
            onNext = {},
            onPrevious = {},
            onClose = {}
        )
    }
}
