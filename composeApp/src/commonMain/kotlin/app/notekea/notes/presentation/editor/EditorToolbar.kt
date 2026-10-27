package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.presentation.components.NotekeaCompactTabs
import app.notekea.core.presentation.components.NotekeaGridSizePicker
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaToolbarPill
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
import app.notekea.resources.format_attach
import app.notekea.resources.format_attach_hint
import app.notekea.resources.format_bold
import app.notekea.resources.format_bullet_list
import app.notekea.resources.format_callout
import app.notekea.resources.format_callout_folded
import app.notekea.resources.format_callout_folded_hint
import app.notekea.resources.format_callout_hint
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
import app.notekea.resources.format_template
import app.notekea.resources.properties_add
import app.notekea.resources.format_web_link
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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

/**
 * The formatting buttons in one capsule, in groups: Heading, Bold, Italic · Link to a note, Tag · Checklist, Bulleted
 * list · Insert template · More, which holds the rest (Web link, Numbered list, Callout, Code block, Image or file,
 * Quote, Divider, Table), so the bar stays short and calm. The groups [fit] leaves out open from the top of More.
 */
@Composable
private fun FormattingGroups(
    fit: ToolbarFit,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val format = { action: FormatAction -> onIntent(EditorIntent.OnFormat(action = action)) }
    val actions = formatActions()
    val label = stringResource(resource = Res.string.editor_toolbar_label)
    val overflow =
        overflowItems(fit = fit, actions = actions, platformConventions = platformConventions, onIntent = onIntent)
    val text: @Composable RowScope.() -> Unit =
        { TextGroup(actions = actions, platformConventions = platformConventions, format = format) }
    val links: @Composable RowScope.() -> Unit =
        { LinkGroup(platformConventions = platformConventions, onIntent = onIntent) }
    val lists: @Composable RowScope.() -> Unit = { ListGroup(actions = actions, format = format) }
    val template: @Composable RowScope.() -> Unit =
        { TemplateButton(platformConventions = platformConventions, onIntent = onIntent) }
    val more: @Composable RowScope.() -> Unit = {
        MoreMenu(actions = actions, platformConventions = platformConventions, onIntent = onIntent, overflow = overflow)
    }
    NotekeaToolbarPill(
        modifier = Modifier.semantics { contentDescription = label },
        groups = listOfNotNull(
            text,
            links.takeIf { fit.showsLinks },
            lists.takeIf { fit.showsLists },
            template.takeIf { fit.showsLists },
            more
        )
    )
}

/** The buttons [fit] leaves out of the capsule, as items for the top of More: the links, the lists and the template. */
@Composable
private fun overflowItems(
    fit: ToolbarFit,
    actions: FormatActions,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
): List<List<NotekeaMenuItem>> {
    val format = { action: FormatAction -> onIntent(EditorIntent.OnFormat(action = action)) }
    val links = listOf(
        moreItem(
            icon = NotekeaIcons.NoteLink,
            label = Res.string.format_note_link,
            shortcutHint = platformConventions.shortcutLabel(shortcut = platformConventions.noteLinkShortcut)
        ) { onIntent(EditorIntent.OnNoteLinkRequest) },
        moreItem(icon = NotekeaIcons.Tag, label = Res.string.format_tag) { onIntent(EditorIntent.OnTagRequest) }
    )
    val lists = listOf(
        moreItem(icon = NotekeaIcons.Checklist, label = Res.string.format_checklist) { format(actions.checklist) },
        moreItem(icon = NotekeaIcons.BulletList, label = Res.string.format_bullet_list) { format(actions.bulletList) },
        moreItem(
            icon = NotekeaIcons.Template,
            label = Res.string.format_template,
            shortcutHint = platformConventions.shortcutLabel(shortcut = platformConventions.templateShortcut)
        ) { onIntent(EditorIntent.OnTemplateRequest) }
    )
    return listOfNotNull(links.takeUnless { fit.showsLinks }, lists.takeUnless { fit.showsLists })
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

/** Link to a note (chosen in the quick switcher) and Tag (chosen in the tag picker). */
@Composable
private fun LinkGroup(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    ToolbarButton(
        icon = NotekeaIcons.NoteLink,
        label = Res.string.format_note_link,
        onClick = { onIntent(EditorIntent.OnNoteLinkRequest) },
        shortcutHint = platformConventions.shortcutLabel(shortcut = platformConventions.noteLinkShortcut)
    )
    ToolbarButton(icon = NotekeaIcons.Tag, label = Res.string.format_tag, onClick = {
        onIntent(EditorIntent.OnTagRequest)
    })
}

/** Checklist and Bulleted list, the lists used most. */
@Composable
private fun ListGroup(
    actions: FormatActions,
    format: (FormatAction) -> Unit
) {
    ToolbarButton(icon = NotekeaIcons.Checklist, label = Res.string.format_checklist, onClick = {
        format(actions.checklist)
    })
    ToolbarButton(icon = NotekeaIcons.BulletList, label = Res.string.format_bullet_list, onClick = {
        format(actions.bulletList)
    })
}

/** Insert template, which opens the template picker (`ROADMAP.md` 1.12). */
@Composable
private fun TemplateButton(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    ToolbarButton(
        icon = NotekeaIcons.Template,
        label = Res.string.format_template,
        onClick = { onIntent(EditorIntent.OnTemplateRequest) },
        shortcutHint = platformConventions.shortcutLabel(shortcut = platformConventions.templateShortcut)
    )
}

/** [shortcut] written the way this system writes it. */
private fun PlatformConventions.shortcutLabel(
    shortcut: Shortcut
): String = shortcut.label(operatingSystem = operatingSystem)

/** Heading (a menu of three sizes), Bold and Italic. */
@Composable
private fun TextGroup(
    actions: FormatActions,
    platformConventions: PlatformConventions,
    format: (FormatAction) -> Unit
) {
    val sizes =
        listOf(Res.string.format_heading_large, Res.string.format_heading_medium, Res.string.format_heading_small)
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
        shortcutHint = platformConventions.shortcutLabel(shortcut = platformConventions.boldShortcut)
    )
    ToolbarButton(
        icon = NotekeaIcons.Italic,
        label = Res.string.format_italic,
        onClick = { format(actions.italic) },
        shortcutHint = platformConventions.shortcutLabel(shortcut = platformConventions.italicShortcut)
    )
}

/**
 * The buttons that do not fit ([overflow]) first, then the buttons used less often: Web link (with its shortcut) and
 * Numbered list; Callout and Code block, whose types open beside the menu; Image or file; then Quote, Divider and
 * Table (its size picked on a grid beside the menu).
 */
@Composable
private fun MoreMenu(
    actions: FormatActions,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    overflow: List<List<NotekeaMenuItem>>
) {
    val shortcut = platformConventions.shortcutLabel(shortcut = platformConventions.webLinkShortcut)
    val format = { action: FormatAction -> onIntent(EditorIntent.OnFormat(action = action)) }
    NotekeaMenuButton(
        icon = NotekeaIcons.More,
        label = stringResource(resource = Res.string.format_more),
        sections = overflow + listOf(
            listOf(
                moreItem(icon = NotekeaIcons.WebLink, label = Res.string.format_web_link, shortcutHint = shortcut) {
                    format(actions.webLink)
                },
                moreItem(icon = NotekeaIcons.NumberedList, label = Res.string.format_numbered_list) {
                    format(actions.numberedList)
                }
            ),
            listOf(
                calloutItem(actions = actions, format = format),
                codeBlockItem(actions = actions, format = format),
                NotekeaMenuItem(
                    label = stringResource(resource = Res.string.format_attach),
                    onClick = { onIntent(EditorIntent.OnAttachmentPick) },
                    icon = NotekeaIcons.AttachFile,
                    supportingText = stringResource(resource = Res.string.format_attach_hint)
                )
            ),
            listOf(addPropertyItem(onIntent = onIntent)),
            listOf(
                moreItem(icon = NotekeaIcons.Quote, label = Res.string.format_quote) { format(actions.quote) },
                moreItem(icon = NotekeaIcons.Divider, label = Res.string.format_divider) {
                    format(FormatAction.Divider)
                },
                tableItem(actions = actions, format = format)
            )
        ),
        takesFocus = false
    )
}

/** Add property (`ROADMAP.md` 1.14): its types beside the menu, each adding a property of that type to the note. */
@Composable
private fun addPropertyItem(
    onIntent: (EditorIntent) -> Unit
): NotekeaMenuItem = NotekeaMenuItem(
    label = stringResource(resource = Res.string.properties_add),
    onClick = {},
    icon = NotekeaIcons.AddProperty,
    items = listOf(
        PropertyTypes.all.map { (type, label, icon) ->
            NotekeaMenuItem(
                label = stringResource(resource = label),
                onClick = { onIntent(PropertyIntent.OnAdd(type = type)) },
                icon = icon
            )
        }
    )
)

/** Callout: its types with their icons beside the menu, "Start folded" first, explained, so it is in view. */
@Composable
private fun calloutItem(
    actions: FormatActions,
    format: (FormatAction) -> Unit
): NotekeaMenuItem {
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
    return NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_callout),
        onClick = {},
        icon = NotekeaIcons.Callout,
        supportingText = stringResource(resource = Res.string.format_callout_hint),
        items = listOf(listOf(folded), callouts)
    )
}

/** Code block: plain text, then the most used languages, which Preview colors, beside the menu. */
@Composable
private fun codeBlockItem(
    actions: FormatActions,
    format: (FormatAction) -> Unit
): NotekeaMenuItem {
    val plain = NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_code_plain),
        onClick = { format(actions.codeBlock(language = "")) }
    )
    val languages = CODE_LANGUAGES.map { (name, fence) ->
        NotekeaMenuItem(label = name, onClick = { format(actions.codeBlock(language = fence)) })
    }
    return NotekeaMenuItem(
        label = stringResource(resource = Res.string.format_code_block),
        onClick = {},
        icon = NotekeaIcons.CodeBlock,
        items = listOf(listOf(plain), languages)
    )
}

/** Table: its size picked on a grid beside the menu. */
@Composable
private fun tableItem(
    actions: FormatActions,
    format: (FormatAction) -> Unit
): NotekeaMenuItem = NotekeaMenuItem(
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

@Composable
private fun moreItem(
    icon: ImageVector,
    label: StringResource,
    shortcutHint: String? = null,
    onClick: () -> Unit
): NotekeaMenuItem = NotekeaMenuItem(
    label = stringResource(resource = label),
    onClick = onClick,
    icon = icon,
    shortcutHint = shortcutHint
)

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
