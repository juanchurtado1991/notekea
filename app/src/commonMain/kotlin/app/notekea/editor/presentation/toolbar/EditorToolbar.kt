package app.notekea.editor.presentation.toolbar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.platform.label
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.resources.Res
import app.notekea.resources.editor_find
import app.notekea.resources.editor_source
import app.notekea.resources.format_bold
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 760

/**
 * The editor toolbar, under the title, always one line: the formatting buttons in groups, each with a tooltip and its
 * shortcut, then Source (marked while [isSource]) and Find in note at the end. Groups that do not fit move into More
 * (⋯), the lists and the template first, then the links ([ToolbarFit]).
 */
@Composable
fun EditorToolbar(
    isSource: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.surface)) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().padding(top = NotekeaDimensions.SpacingS)
        ) {
            val fit = ToolbarFit.of(width = maxWidth)

            // The capsule and the end buttons float a little above the line under the bar.
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = NotekeaDimensions.SpacingXs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The buttons stay at the end, where they were beside the Edit and Preview tabs.
                Spacer(modifier = Modifier.weight(weight = 1f))

                FormattingGroups(
                    fit = fit,
                    platformConventions = platformConventions,
                    onIntent = onIntent
                )

                Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingM))

                EndButtons(
                    isSource = isSource,
                    platformConventions = platformConventions,
                    onIntent = onIntent
                )
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

/**
 * Source, marked while the note is shown as written ([isSource]), and Find in note, at the end; the side panel is
 * shown or hidden from the tool rail.
 */
@Composable
private fun EndButtons(
    isSource: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem

    ToolbarButton(
        icon = NotekeaIcons.Source,
        label = Res.string.editor_source,
        onClick = { onIntent(EditorIntent.OnSourceModeToggle) },
        shortcutHint = platformConventions
            .toggleSourceShortcut
            .label(operatingSystem = operatingSystem),
        tint = MaterialTheme
            .colorScheme
            .primary
            .takeIf { isSource }
    )

    ToolbarButton(
        icon = NotekeaIcons.Find,
        label = Res.string.editor_find,
        onClick = { onIntent(EditorIntent.OnFindOpen) },
        shortcutHint = platformConventions.findShortcut.label(operatingSystem = operatingSystem)
    )
}

@Composable
internal fun ToolbarButton(
    icon: ImageVector,
    label: StringResource,
    onClick: () -> Unit,
    shortcutHint: String? = null,
    tint: Color? = null
) {
    NotekeaIconButton(
        icon = icon,
        label = stringResource(resource = label),
        onClick = onClick,
        shortcutHint = shortcutHint,
        takesFocus = false,
        tint = tint
    )
}

@Preview
@Composable
private fun EditorToolbarPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            EditorToolbar(
                isSource = true,
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )

            EditorToolbar(
                isSource = false,
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
        ToolbarButton(
            icon = NotekeaIcons.Bold,
            label = Res.string.format_bold,
            onClick = {
            },
            shortcutHint = PREVIEW_HINT
        )
    }
}

private const val PREVIEW_HINT = "Ctrl+B"
