package app.notekea.notes.presentation.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaCheckbox
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.resources.Res
import app.notekea.resources.properties_in_note
import app.notekea.resources.properties_no_note_message
import app.notekea.resources.properties_no_note_title
import app.notekea.resources.properties_none_message
import app.notekea.resources.properties_none_title
import org.jetbrains.compose.resources.stringResource

private const val KEY_WIDTH_DP = 100
private const val PREVIEW_HEIGHT_DP = 420
private const val PREVIEW_WIDTH_DP = 320

/**
 * The side panel's Properties tab (`ROADMAP.md` 1.14): the open note's properties ([state], null when the active tab
 * shows no note), edited as in the note's card, under "Show in the note", checked while they also show under the
 * note's title ([arePropertiesInNote]), and Show source. A note without properties says so above Add property.
 */
@Composable
internal fun PropertiesPanel(
    arePropertiesInNote: Boolean,
    onIntent: (EditorIntent) -> Unit,
    state: EditorState?,
    modifier: Modifier = Modifier
) {
    if (state?.notePath == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            EmptyState(
                title = stringResource(resource = Res.string.properties_no_note_title),
                message = stringResource(resource = Res.string.properties_no_note_message)
            )
        }
        return
    }
    var isEditingSource by remember(state.notePath) { mutableStateOf(value = false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(all = NotekeaDimensions.SpacingS)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            InNoteToggle(isChecked = arePropertiesInNote, onIntent = onIntent)
            Spacer(modifier = Modifier.weight(weight = 1f))
            SourceToggle(state = state, isEditingSource = isEditingSource, onIntent = onIntent)
        }
        val isEmpty = state.properties == FrontmatterContent.None && !state.isSourceShown && !isEditingSource
        if (isEmpty) {
            EmptyState(
                title = stringResource(resource = Res.string.properties_none_title),
                message = stringResource(resource = Res.string.properties_none_message),
                modifier = Modifier.fillMaxWidth()
            )
        }
        PropertiesForm(
            state = state,
            isEditingSource = isEditingSource,
            onEditingChange = { isEditingSource = it },
            onIntent = onIntent,
            keyWidth = KEY_WIDTH_DP.dp,
            chipColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/** "Show in the note" with its checkbox; a click on either moves the properties between the note and the panel. */
@Composable
private fun InNoteToggle(
    isChecked: Boolean,
    onIntent: (EditorIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.properties_in_note)
    val toggle = { onIntent(EditorIntent.OnPropertiesPlacementToggle) }
    Row(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.small)
            .focusProperties { canFocus = false }
            .clickable(role = Role.Checkbox, onClick = toggle)
            .padding(all = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        NotekeaCheckbox(checked = isChecked, onClick = toggle, label = label)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
private fun PropertiesPanelPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            PropertiesPanel(
                state = EditorState(notePath = EditorPreviewData.path, properties = EditorPreviewData.typedProperties),
                arePropertiesInNote = false,
                onIntent = {}
            )
        }
    }
}

@Preview
@Composable
private fun PropertiesPanelEmptyPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            PropertiesPanel(
                state = EditorState(
                    notePath = EditorPreviewData.path
                ),
                arePropertiesInNote = true,
                onIntent = {
                }
            )
        }
    }
}
