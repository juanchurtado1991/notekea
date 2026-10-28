package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaCompactTabs
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.editor_edit_mode
import app.notekea.resources.editor_find
import app.notekea.resources.editor_preview_mode
import app.notekea.resources.format_bold
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 760

/**
 * The editor toolbar (`ROADMAP.md` 1.17), under the title, always one line: Edit | Preview tabs (switched with their
 * shortcut too), the formatting buttons in groups (text, links and tags, lists, blocks), each with a plain-language
 * tooltip and its shortcut, and at the end Find in note. Preview shows the note as it reads, so it keeps only the tabs
 * and Find. When the editor is too narrow for every group, the groups that do not fit move into More (⋯), the lists
 * and the template first, then the links ([ToolbarFit]). The Tag button opens the tag picker.
 */
@Composable
fun EditorToolbar(
    isReading: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.surface)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = NotekeaDimensions.SpacingS)) {
            val fit = ToolbarFit.of(width = maxWidth)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                ModeTabs(isReading = isReading, platformConventions = platformConventions, onIntent = onIntent)
                Spacer(modifier = Modifier.weight(weight = 1f))
                // The capsule and the end buttons float a little above the line under the bar.
                Row(
                    modifier = Modifier.padding(bottom = NotekeaDimensions.SpacingXs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isReading) {
                        FormattingGroups(fit = fit, platformConventions = platformConventions, onIntent = onIntent)
                        Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingM))
                    }
                    EndButtons(platformConventions = platformConventions, onIntent = onIntent)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = NotekeaDimensions.BorderWidth)
                .background(color = MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

/** Find in note, at the end in both modes; the side panel is shown or hidden from the tool rail. */
@Composable
private fun EndButtons(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem
    ToolbarButton(
        icon = NotekeaIcons.Find,
        label = Res.string.editor_find,
        onClick = { onIntent(EditorIntent.OnFindOpen) },
        shortcutHint = platformConventions.findShortcut.label(operatingSystem = operatingSystem)
    )
}

/** Edit | Preview, like GitHub's; the tooltip teaches the shortcut that switches them. */
@Composable
private fun ModeTabs(
    isReading: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    NotekeaCompactTabs(
        labels = listOf(
            stringResource(resource = Res.string.editor_edit_mode),
            stringResource(resource = Res.string.editor_preview_mode)
        ),
        selectedIndex = if (isReading) 1 else 0,
        onSelect = { index -> if ((index == 1) != isReading) onIntent(EditorIntent.OnReadModeToggle) },
        shortcutHint = platformConventions.toggleReadModeShortcut.label(
            operatingSystem = platformConventions.operatingSystem
        ),
        takesFocus = false
    )
}

@Composable
internal fun ToolbarButton(
    icon: ImageVector,
    label: StringResource,
    onClick: () -> Unit,
    shortcutHint: String? = null
) {
    NotekeaIconButton(
        icon = icon,
        label = stringResource(resource = label),
        onClick = onClick,
        shortcutHint = shortcutHint,
        takesFocus = false
    )
}

@Preview
@Composable
private fun EditorToolbarPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            EditorToolbar(
                isReading = false,
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
            EditorToolbar(
                isReading = true,
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}

@Preview
@Composable
private fun ToolbarButtonPreview() {
    NotekeaPreviewThemes {
        ToolbarButton(icon = NotekeaIcons.Bold, label = Res.string.format_bold, onClick = {
        }, shortcutHint = PREVIEW_HINT)
    }
}

private const val PREVIEW_HINT = "Ctrl+B"
