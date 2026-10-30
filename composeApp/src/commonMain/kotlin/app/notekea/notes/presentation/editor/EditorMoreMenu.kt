package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.markdown.CalloutKind
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaGridSizePicker
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.calloutIcon
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.FormatAction
import app.notekea.resources.Res
import app.notekea.resources.format_attach
import app.notekea.resources.format_attach_hint
import app.notekea.resources.format_bullet_list
import app.notekea.resources.format_callout
import app.notekea.resources.format_callout_folded
import app.notekea.resources.format_callout_folded_hint
import app.notekea.resources.format_callout_hint
import app.notekea.resources.format_checklist
import app.notekea.resources.format_code_block
import app.notekea.resources.format_code_plain
import app.notekea.resources.format_divider
import app.notekea.resources.format_more
import app.notekea.resources.format_note_link
import app.notekea.resources.format_numbered_list
import app.notekea.resources.format_quote
import app.notekea.resources.format_table
import app.notekea.resources.format_table_size
import app.notekea.resources.format_tag
import app.notekea.resources.format_template
import app.notekea.resources.format_web_link
import app.notekea.resources.properties_add
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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

/**
 * The buttons that do not fit ([fit]) first, then the buttons used less often: Web link (with its shortcut) and
 * Numbered list; Callout and Code block, whose types open beside the menu; Image or file; then Quote, Divider and
 * Table (its size picked on a grid beside the menu).
 */
@Composable
internal fun MoreMenu(
    actions: FormatActions,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    fit: ToolbarFit
) {
    val overflow =
        overflowItems(fit = fit, actions = actions, platformConventions = platformConventions, onIntent = onIntent)
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
private fun MoreMenuPreview() {
    NotekeaPreviewThemes {
        MoreMenu(
            actions = formatActions(),
            platformConventions = EditorPreviewData.platformConventions,
            onIntent = {},
            fit = ToolbarFit.TEXT_ONLY
        )
    }
}
