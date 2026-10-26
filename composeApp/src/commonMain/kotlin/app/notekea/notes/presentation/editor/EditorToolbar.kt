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
import app.notekea.resources.format_checklist
import app.notekea.resources.format_code_block
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
import app.notekea.resources.format_tag
import app.notekea.resources.format_tag_new
import app.notekea.resources.format_web_link
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val ONE_LINE_MIN_WIDTH_DP = 680
private const val PREVIEW_WIDTH_DP = 760
private const val TAGS_IN_MENU = 8

/**
 * The editor toolbar (`ROADMAP.md` 1.17), under the title: Edit | Preview tabs (switched with their shortcut too), the
 * formatting buttons in groups (text, links and tags, lists, blocks), each with a plain-language tooltip and its
 * shortcut, and Find in note at the end. Preview shows the note as it reads, so it keeps only the tabs and Find. The
 * buttons move to a line of their own above the tabs when the editor is too narrow for one line. [vaultTags] fill the
 * Tag menu, the most used first.
 */
@Composable
fun EditorToolbar(
    isReading: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    vaultTags: List<String> = emptyList()
) {
    val groups = @Composable {
        FormattingGroups(platformConventions = platformConventions, onIntent = onIntent, vaultTags = vaultTags)
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
        )
    )
}

/** Text, links and tags, lists and blocks, a little apart from each other; they wrap only in a very narrow editor. */
@Composable
private fun FormattingGroups(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    vaultTags: List<String>
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
            vaultTags = vaultTags,
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
        shortcutHint = shortcutHint
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

/** Link to a note (chosen in the quick switcher), Web link and Tag (a menu of the vault's tags and New tag). */
@Composable
private fun LinkGroup(
    actions: FormatActions,
    platformConventions: PlatformConventions,
    vaultTags: List<String>,
    onIntent: (EditorIntent) -> Unit
) {
    val os = platformConventions.operatingSystem
    val format = { action: FormatAction -> onIntent(EditorIntent.OnFormat(action = action)) }
    val tags = vaultTags.take(n = TAGS_IN_MENU).map { tag ->
        NotekeaMenuItem(label = tag, onClick = { format(actions.tag(name = tag)) }, icon = NotekeaIcons.Tag)
    }
    val newTag = NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_tag_new),
        onClick = { format(actions.tag(name = "")) },
        icon = NotekeaIcons.NewTag
    )
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
            onClick = { format(actions.webLink) },
            shortcutHint = platformConventions.webLinkShortcut.label(operatingSystem = os)
        )
        NotekeaMenuButton(
            icon = NotekeaIcons.Tag,
            label = stringResource(resource = Res.string.format_tag),
            sections = listOf(tags, listOf(newTag)).filter { it.isNotEmpty() }
        )
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

/** Callout (a menu of its types, Folded first so it is in view in a short window), Table and More (Quote, Code block, Divider). */
@Composable
private fun BlockGroup(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    var isFolded by remember { mutableStateOf(value = false) }
    val callouts = CalloutKind.entries.map { kind ->
        NotekeaMenuItem(
            label = actions.calloutTitles.getValue(kind),
            onClick = { format(actions.callout(kind = kind, isFolded = isFolded)) },
            icon = calloutIcon(kind = kind)
        )
    }
    val folded = NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_callout_folded),
        onClick = { isFolded = !isFolded },
        isChecked = isFolded,
        keepsMenuOpen = true
    )
    ToolbarGroup {
        NotekeaMenuButton(
            icon = NotekeaIcons.Callout,
            label = stringResource(resource = Res.string.format_callout),
            sections = listOf(listOf(folded), callouts)
        )
        ToolbarButton(
            icon = NotekeaIcons.Table,
            label = Res.string.format_table,
            onClick = { format(actions.table) }
        )
        MoreMenu(actions = actions, format = format)
    }
}

/** Quote, Code block and Divider, used less often than the other buttons. */
@Composable
private fun MoreMenu(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    NotekeaMenuButton(
        icon = NotekeaIcons.More,
        label = stringResource(resource = Res.string.format_more),
        sections = listOf(
            listOf(
                moreItem(icon = NotekeaIcons.Quote, label = Res.string.format_quote) { format(actions.quote) },
                moreItem(icon = NotekeaIcons.CodeBlock, label = Res.string.format_code_block) {
                    format(actions.codeBlock)
                },
                moreItem(icon = NotekeaIcons.Divider, label = Res.string.format_divider) {
                    format(FormatAction.Divider)
                }
            )
        )
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
                onIntent = {},
                vaultTags = listOf("recipe", "venezuelan")
            )
            EditorToolbar(
                isReading = true,
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}
