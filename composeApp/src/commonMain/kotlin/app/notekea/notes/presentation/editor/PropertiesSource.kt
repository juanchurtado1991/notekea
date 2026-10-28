package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.properties_invalid
import org.jetbrains.compose.resources.stringResource

/**
 * The YAML as written, edited as text: typed, it replaces the frontmatter once typing pauses; left, at once. Unreadable
 * YAML says where it fails, and the warning goes as soon as the fix is saved. [onEditingChange] says whether it is
 * being typed in.
 */
@Composable
internal fun PropertiesSource(
    state: EditorState,
    onEditingChange: (Boolean) -> Unit,
    onIntent: (EditorIntent) -> Unit,
    invalidLine: Int?
) {
    val path = state.notePath ?: return
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        invalidLine?.let { ErrorBanner(message = stringResource(Res.string.properties_invalid, it)) }
        // A new field for each note, so what was typed in one note never goes to another.
        key(path) {
            PropertiesSourceField(
                source = state.propertiesSource,
                onEdit = { onIntent(PropertyIntent.OnSourceEdit(path = path, yaml = it)) },
                onCommit = { onIntent(PropertyIntent.OnSourceCommit(path = path, yaml = it)) },
                onFocusChange = onEditingChange
            )
        }
    }
}

/**
 * The YAML of the source view, edited as text: each change goes to [onEdit], and once the field is left it goes to
 * [onCommit] when it differs from [source]. What is typed is kept while the field is focused, even when the saved
 * [source] comes back written slightly differently; once the field is left, [source] is shown again.
 */
@Composable
private fun PropertiesSourceField(
    source: String,
    onEdit: (String) -> Unit,
    onCommit: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    var text by remember { mutableStateOf(value = source) }
    var isFocused by remember { mutableStateOf(value = false) }
    LaunchedEffect(source, isFocused) { if (!isFocused) text = source }
    Box(
        modifier = Modifier.onFocusChanged { focus ->
            if (focus.hasFocus == isFocused) return@onFocusChanged
            isFocused = focus.hasFocus
            onFocusChange(focus.hasFocus)
            if (!focus.hasFocus && text != source) onCommit(text)
        }
    ) {
        NotekeaTextField(value = text, onValueChange = {
            text = it
            onEdit(it)
        }, modifier = Modifier.fillMaxWidth(), singleLine = false)
    }
}

@Preview
@Composable
private fun PropertiesSourcePreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_SOURCE_WIDTH_DP.dp)) {
            PropertiesSource(
                state = EditorState(notePath = EditorPreviewData.path, propertiesSource = PREVIEW_SOURCE),
                onEditingChange = {},
                onIntent = {},
                invalidLine = 1
            )
        }
    }
}

private const val PREVIEW_SOURCE = "status: draft"
private const val PREVIEW_SOURCE_WIDTH_DP = 640
