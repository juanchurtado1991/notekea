package app.notekea.editor.presentation.toolbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.presentation.designsystem.components.NotekeaMenuButton
import app.notekea.core.presentation.designsystem.components.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.NotekeaToolbarPill
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.GraphPalette
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.platform.label
import app.notekea.editor.domain.FormatAction
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.view.MoreMenu
import app.notekea.resources.Res
import app.notekea.resources.editor_toolbar_label
import app.notekea.resources.format_bold
import app.notekea.resources.format_bullet_list
import app.notekea.resources.format_checklist
import app.notekea.resources.format_heading
import app.notekea.resources.format_heading_large
import app.notekea.resources.format_heading_medium
import app.notekea.resources.format_heading_small
import app.notekea.resources.format_italic
import app.notekea.resources.format_note_link
import app.notekea.resources.format_tag
import app.notekea.resources.format_template
import org.jetbrains.compose.resources.stringResource

// Each group has a color of its own, the same in every theme: the graph's, mid tones that read on light and dark.
private val LINKS_COLOR get() = GraphPalette.colorAt(index = 2)
private val LISTS_COLOR get() = GraphPalette.colorAt(index = 0)
private val TEMPLATE_COLOR get() = GraphPalette.colorAt(index = 3)

/**
 * The formatting buttons in one capsule, in groups: Heading, Bold, Italic · Link to a note, Tag · Checklist, Bulleted
 * list · Insert template · More, which holds the rest (Web link, Numbered list, Callout, Code block, Image or file,
 * Quote, Divider, Table), so the bar stays short and calm. The groups [fit] leaves out open from the top of More.
 */
@Composable
internal fun FormattingGroups(
    fit: ToolbarFit,
    active: Set<ToolbarFormat>,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val format = { action: FormatAction -> onIntent(EditorIntent.OnFormat(action = action)) }

    val actions = formatActions()
    val label = stringResource(resource = Res.string.editor_toolbar_label)

    val text: @Composable RowScope.() -> Unit = {
        TextGroup(
            actions = actions,
            active = active,
            platformConventions = platformConventions,
            format = format
        )
    }

    val links: @Composable RowScope.() -> Unit = {
        LinkGroup(
            active = active,
            platformConventions = platformConventions,
            onIntent = onIntent
        )
    }

    val lists: @Composable RowScope.() -> Unit = {
        ListGroup(
            actions = actions,
            active = active,
            format = format
        )
    }

    val template: @Composable RowScope.() -> Unit = {
        TemplateButton(
            platformConventions = platformConventions,
            onIntent = onIntent
        )
    }

    val more: @Composable RowScope.() -> Unit = {
        MoreMenu(
            actions = actions,
            platformConventions = platformConventions,
            onIntent = onIntent,
            fit = fit
        )
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

/** Link to a note (chosen in the quick switcher) and Tag (chosen in the tag picker). */
@Composable
private fun LinkGroup(
    active: Set<ToolbarFormat>,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    ToolbarButton(
        icon = NotekeaIcons.NoteLink,
        label = Res.string.format_note_link,
        onClick = { onIntent(EditorIntent.OnNoteLinkRequest) },
        shortcutHint = platformConventions.shortcutLabel(
            shortcut = platformConventions.noteLinkShortcut
        ),
        tint = LINKS_COLOR,
        isMarked = ToolbarFormat.NOTE_LINK in active
    )

    ToolbarButton(
        icon = NotekeaIcons.Tag,
        label = Res.string.format_tag,
        onClick = { onIntent(EditorIntent.OnTagRequest) },
        tint = LINKS_COLOR,
        isMarked = ToolbarFormat.TAG in active
    )
}

/** Checklist and Bulleted list, the lists used most. */
@Composable
private fun ListGroup(
    actions: FormatActions,
    active: Set<ToolbarFormat>,
    format: (FormatAction) -> Unit
) {
    ToolbarButton(
        icon = NotekeaIcons.Checklist,
        label = Res.string.format_checklist,
        onClick = { format(actions.checklist) },
        tint = LISTS_COLOR,
        isMarked = ToolbarFormat.CHECKLIST in active
    )

    ToolbarButton(
        icon = NotekeaIcons.BulletList,
        label = Res.string.format_bullet_list,
        onClick = { format(actions.bulletList) },
        tint = LISTS_COLOR,
        isMarked = ToolbarFormat.BULLET_LIST in active
    )
}

/** Insert template, which opens the template picker. */
@Composable
private fun TemplateButton(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    ToolbarButton(
        icon = NotekeaIcons.Template,
        label = Res.string.format_template,
        onClick = { onIntent(EditorIntent.OnTemplateRequest) },
        shortcutHint = platformConventions.shortcutLabel(
            shortcut = platformConventions.templateShortcut
        ),
        tint = TEMPLATE_COLOR
    )
}

/** [shortcut] written the way this system writes it. */
internal fun PlatformConventions.shortcutLabel(shortcut: Shortcut): String = shortcut.label(
    operatingSystem = operatingSystem
)

/** Heading (a menu of three sizes), Bold and Italic. */
@Composable
private fun TextGroup(
    actions: FormatActions,
    active: Set<ToolbarFormat>,
    platformConventions: PlatformConventions,
    format: (FormatAction) -> Unit
) {
    val ink = MaterialTheme.colorScheme.onSurface

    val sizes = listOf(
        Res.string.format_heading_large,
        Res.string.format_heading_medium,
        Res.string.format_heading_small
    )

    NotekeaMenuButton(
        takesFocus = false,
        tint = ink,
        isMarked = ToolbarFormat.HEADING in active,
        icon = NotekeaIcons.Heading,
        label = stringResource(resource = Res.string.format_heading),
        sections = listOf(
            actions
                .headings
                .zip(sizes)
                .map { (heading, size) ->
                    NotekeaMenuItem(
                        label = stringResource(resource = size),
                        onClick = { format(heading) }
                    )
                }
        )
    )

    ToolbarButton(
        icon = NotekeaIcons.Bold,
        label = Res.string.format_bold,
        onClick = { format(actions.bold) },
        shortcutHint = platformConventions.shortcutLabel(
            shortcut = platformConventions.boldShortcut
        ),
        tint = ink,
        isMarked = ToolbarFormat.BOLD in active
    )

    ToolbarButton(
        icon = NotekeaIcons.Italic,
        label = Res.string.format_italic,
        onClick = { format(actions.italic) },
        shortcutHint = platformConventions.shortcutLabel(
            shortcut = platformConventions.italicShortcut
        ),
        tint = ink,
        isMarked = ToolbarFormat.ITALIC in active
    )
}

@Preview
@Composable
private fun FormattingGroupsPreview() {
    NotekeaPreviewThemes {
        FormattingGroups(
            fit = ToolbarFit.ALL,
            active = setOf(ToolbarFormat.BOLD),
            platformConventions = EditorPreviewData.platformConventions,
            onIntent = {
            }
        )
    }
}
