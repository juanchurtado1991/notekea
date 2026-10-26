package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.markdown.CalloutKind
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaCompactTabs
import app.notekea.core.presentation.components.NotekeaGridSizePicker
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.calloutIcon
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.FormatAction
import app.notekea.resources.Res
import app.notekea.resources.editor_edit_mode
import app.notekea.resources.editor_find
import app.notekea.resources.editor_preview_mode
import app.notekea.resources.editor_toolbar_label
import app.notekea.resources.format_bold
import app.notekea.resources.format_bullet_list
import app.notekea.resources.format_callout
import app.notekea.resources.format_callout_folded
import app.notekea.resources.format_callout_folded_hint
import app.notekea.resources.format_checklist
import app.notekea.resources.format_code_block
import app.notekea.resources.format_code_plain
import app.notekea.resources.format_divider
import app.notekea.resources.format_heading
import app.notekea.resources.format_heading_large
import app.notekea.resources.format_heading_medium
import app.notekea.resources.format_heading_small
import app.notekea.resources.format_italic
import app.notekea.resources.format_more
import app.notekea.resources.format_note_link
import app.notekea.resources.format_numbered_list
import app.notekea.resources.format_quote
import app.notekea.resources.format_table
import app.notekea.resources.format_table_size
import app.notekea.resources.format_tag
import app.notekea.resources.format_web_link
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val ONE_LINE_MIN_WIDTH_DP = 680
private const val PREVIEW_WIDTH_DP = 760

/** The languages of the Code block menu, by name and as written after the fence (the names Preview colors). */
private val CODE_LANGUAGES = listOf(
    "Python" to "python",
    "JavaScript" to "javascript",
    "TypeScript" to "typescript",
    "Java" to "java",
    "Kotlin" to "kotlin",
    "C#" to "csharp",
    "C++" to "cpp",
    "Go" to "go",
    "Rust" to "rust",
    "Shell" to "shell"
)

/**
 * The editor toolbar (`ROADMAP.md` 1.17), under the title: Edit | Preview tabs (switched with their shortcut too), the
 * formatting buttons in groups (text, links and tags, lists, blocks), each with a plain-language tooltip and its
 * shortcut, and Find in note at the end. Preview shows the note as it reads, so it keeps only the tabs and Find. The
 * buttons move to a line of their own above the tabs when the editor is too narrow for one line. the Tag button opens the
 * tag picker.
 */
@Composable
fun EditorToolbar(
    isReading: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = @Composable {
        FormattingGroups(platformConventions = platformConventions, onIntent = onIntent)
    }
    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.surface)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = NotekeaDimensions.SpacingS)) {
            // The groups sit beside the tabs when they fit, else on a line of their own above them.
            val isOneLine = isReading || maxWidth >= ONE_LINE_MIN_WIDTH_DP.dp
            Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
                if (!isOneLine) groups()
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    ModeTabs(isReading = isReading, platformConventions = platformConventions, onIntent = onIntent)
                    Spacer(modifier = Modifier.weight(weight = 1f))
                    if (isOneLine && !isReading) {
                        groups()
                        Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingM))
                    }
                    ToolbarButton(
                        icon = NotekeaIcons.Find,
                        label = Res.string.editor_find,
                        onClick = { onIntent(EditorIntent.OnFindOpen) },
                        shortcutHint = platformConventions.findShortcut.label(
                            operatingSystem = platformConventions.operatingSystem
                        )
                    )
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

/** Text, links and tags, lists and blocks, a little apart from each other; they wrap only in a very narrow editor. */
@Composable
private fun FormattingGroups(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val format = { action: FormatAction -> onIntent(EditorIntent.OnFormat(action = action)) }
    val actions = formatActions()
    val label = stringResource(resource = Res.string.editor_toolbar_label)
    FlowRow(
        modifier = Modifier.semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        TextGroup(actions = actions, platformConventions = platformConventions, format = format)
        LinkGroup(
            actions = actions,
            platformConventions = platformConventions,
            onIntent = onIntent
        )
        ListGroup(actions = actions, format = format)
        BlockGroup(actions = actions, format = format)
    }
}

@Composable
private fun ToolbarButton(
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

@Composable
private fun ToolbarGroup(
    content: @Composable () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) { content() }
}

/** Heading (a menu of three sizes), Bold and Italic. */
@Composable
private fun TextGroup(
    actions: FormatActions,
    platformConventions: PlatformConventions,
    format: (FormatAction) -> Unit
) {
    val os = platformConventions.operatingSystem
    val sizes =
        listOf(Res.string.format_heading_large, Res.string.format_heading_medium, Res.string.format_heading_small)
    ToolbarGroup {
        NotekeaMenuButton(
            takesFocus = false,
            icon = NotekeaIcons.Heading,
            label = stringResource(resource = Res.string.format_heading),
            sections = listOf(
                actions.headings.zip(sizes).map { (heading, size) ->
                    NotekeaMenuItem(label = stringResource(resource = size), onClick = { format(heading) })
                }
            )
        )
        ToolbarButton(
            icon = NotekeaIcons.Bold,
            label = Res.string.format_bold,
            onClick = { format(actions.bold) },
            shortcutHint = platformConventions.boldShortcut.label(operatingSystem = os)
        )
        ToolbarButton(
            icon = NotekeaIcons.Italic,
            label = Res.string.format_italic,
            onClick = { format(actions.italic) },
            shortcutHint = platformConventions.italicShortcut.label(operatingSystem = os)
        )
    }
}

/** Link to a note (chosen in the quick switcher), Web link and Tag (chosen in the tag picker). */
@Composable
private fun LinkGroup(
    actions: FormatActions,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val os = platformConventions.operatingSystem
    ToolbarGroup {
        ToolbarButton(
            icon = NotekeaIcons.NoteLink,
            label = Res.string.format_note_link,
            onClick = { onIntent(EditorIntent.OnNoteLinkRequest) },
            shortcutHint = platformConventions.noteLinkShortcut.label(operatingSystem = os)
        )
        ToolbarButton(
            icon = NotekeaIcons.WebLink,
            label = Res.string.format_web_link,
            onClick = { onIntent(EditorIntent.OnFormat(action = actions.webLink)) },
            shortcutHint = platformConventions.webLinkShortcut.label(operatingSystem = os)
        )
        ToolbarButton(icon = NotekeaIcons.Tag, label = Res.string.format_tag, onClick = {
            onIntent(EditorIntent.OnTagRequest)
        })
    }
}

@Composable
private fun ListGroup(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    ToolbarGroup {
        ToolbarButton(
            icon = NotekeaIcons.BulletList,
            label = Res.string.format_bullet_list,
            onClick = { format(actions.bulletList) }
        )
        ToolbarButton(
            icon = NotekeaIcons.NumberedList,
            label = Res.string.format_numbered_list,
            onClick = { format(actions.numberedList) }
        )
        ToolbarButton(
            icon = NotekeaIcons.Checklist,
            label = Res.string.format_checklist,
            onClick = { format(actions.checklist) }
        )
    }
}

/** Callout (a menu of its types), Code block (a menu of languages) and More (Quote, Divider, Table). */
@Composable
private fun BlockGroup(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    ToolbarGroup {
        CalloutMenu(actions = actions, format = format)
        CodeBlockMenu(actions = actions, format = format)
        MoreMenu(actions = actions, format = format)
    }
}

/** The callout types with their icons, "Start folded" first, explained, so it is in view in a short window. */
@Composable
private fun CalloutMenu(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    // Read when an item is clicked, never captured: the open menu may still hold the items of an earlier composition.
    val isFolded = remember { mutableStateOf(value = false) }
    val callouts = CalloutKind.entries.map { kind ->
        NotekeaMenuItem(
            label = actions.calloutTitles.getValue(kind),
            onClick = { format(actions.callout(kind = kind, isFolded = isFolded.value)) },
            icon = calloutIcon(kind = kind)
        )
    }
    val folded = NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_callout_folded),
        onClick = { isFolded.value = !isFolded.value },
        supportingText = stringResource(resource = Res.string.format_callout_folded_hint),
        isChecked = isFolded.value,
        keepsMenuOpen = true
    )
    NotekeaMenuButton(
        icon = NotekeaIcons.Callout,
        label = stringResource(resource = Res.string.format_callout),
        sections = listOf(listOf(folded), callouts),
        takesFocus = false
    )
}

/** Plain text, then the most used languages, which Preview colors; the language is written after the fence. */
@Composable
private fun CodeBlockMenu(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    val plain = NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_code_plain),
        onClick = { format(actions.codeBlock(language = "")) }
    )
    val languages = CODE_LANGUAGES.map { (name, fence) ->
        NotekeaMenuItem(label = name, onClick = { format(actions.codeBlock(language = fence)) })
    }
    NotekeaMenuButton(
        icon = NotekeaIcons.CodeBlock,
        label = stringResource(resource = Res.string.format_code_block),
        sections = listOf(listOf(plain), languages),
        takesFocus = false
    )
}

/** Quote, Divider and Table (its size picked on a grid beside the menu), used less often than the other buttons. */
@Composable
private fun MoreMenu(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    val table = NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_table),
        onClick = {},
        icon = NotekeaIcons.Table,
        submenu = { onChosen ->
            NotekeaGridSizePicker(
                columns = FormatActions.TABLE_MAX_COLUMNS,
                rows = FormatActions.TABLE_MAX_ROWS,
                label = { columns, rows -> stringResource(Res.string.format_table_size, columns, rows) },
                onPick = { columns, rows ->
                    onChosen()
                    format(actions.table(columns = columns, rows = rows))
                }
            )
        }
    )
    NotekeaMenuButton(
        icon = NotekeaIcons.More,
        label = stringResource(resource = Res.string.format_more),
        sections = listOf(
            listOf(
                moreItem(icon = NotekeaIcons.Quote, label = Res.string.format_quote) { format(actions.quote) },
                moreItem(icon = NotekeaIcons.Divider, label = Res.string.format_divider) {
                    format(FormatAction.Divider)
                },
                table
            )
        ),
        takesFocus = false
    )
}

@Composable
private fun moreItem(
    icon: ImageVector,
    label: StringResource,
    onClick: () -> Unit
): NotekeaMenuItem = NotekeaMenuItem(label = stringResource(resource = label), onClick = onClick, icon = icon)

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
